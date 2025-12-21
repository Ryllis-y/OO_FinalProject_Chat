package com.example.chat.handler.action.impl;

import com.example.chat.common.packet.WsRequest;
import com.example.chat.common.model.Group;
import com.example.chat.common.packet.WsResponse;
import com.example.chat.handler.action.BaseActionHandler;
import com.example.chat.repository.DataCenter;
import com.example.chat.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * 踢出群成员处理器
 */
@Component("KICK_GROUP_USER")
public class Kick_GroupUserHandler extends BaseActionHandler {

    @Autowired
    private UserService userService;

    @Override
    public void handle(WebSocketSession session, WsRequest request) {
        try {
            String operator = getCurrentUser(session);
            if (operator == null) {
                sendError(session, "请先登录");
                return;
            }

            JsonNode params = request.getParams();
            if (params == null || !params.has("groupId") || !params.has("targetUser")) {
                sendError(session, "参数错误：缺少 groupId 或 targetUser");
                return;
            }

            String groupId = params.get("groupId").asText();
            String targetUser = params.get("targetUser").asText();

            // 在踢出之前获取群组信息（包含被踢用户）
            Group groupBeforeKick = DataCenter.GROUPS.get(groupId);
            if (groupBeforeKick == null) {
                sendError(session, "群组不存在");
                return;
            }

            // 调用 UserService 踢出成员
            boolean success = userService.kickGroupUser(operator, groupId, targetUser);

            if (success) {
                // 获取更新后的群组信息
                Group updatedGroup = DataCenter.GROUPS.get(groupId);
                
                // 构建响应数据
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("groupId", groupId);
                responseData.put("targetUser", targetUser);
                responseData.put("message", "已成功将用户踢出群组");

                sendSuccess(session, responseData);

                // 通知被踢出的用户
                notifyKickedUser(targetUser, updatedGroup);

                // 广播踢人系统消息给所有成员（使用踢出前的成员列表，确保被踢用户也能收到）
                broadcastKickNotice(groupId, groupBeforeKick, operator, targetUser);

                // 广播群组更新给其他成员
                broadcastGroupUpdate(groupId, updatedGroup, targetUser);

                System.out.println("群聊踢人: " + operator + " 在群组 " + updatedGroup.getGroupName() + " 中踢出了 " + targetUser);
            } else {
                sendError(session, "踢出失败");
            }

        } catch (SecurityException e) {
            sendError(session, e.getMessage());
        } catch (Exception e) {
            sendError(session, "踢出失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 通知被踢出的用户
     */
    private void notifyKickedUser(String targetUser, Group group) {
        org.springframework.web.socket.WebSocketSession targetSession = DataCenter.ONLINE_USERS.get(targetUser);
        if (targetSession != null && targetSession.isOpen()) {
            try {
                Map<String, Object> noticeData = new HashMap<>();
                noticeData.put("groupId", group.getGroupId());
                noticeData.put("groupName", group.getGroupName());
                noticeData.put("message", "您已被移出群组: " + group.getGroupName());

                WsResponse notice = WsResponse.builder()
                        .type("GROUP_KICKED")
                        .data(noticeData)
                        .build();

                String json = objectMapper.writeValueAsString(notice);
                targetSession.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                System.err.println("通知被踢用户失败: " + e.getMessage());
            }
        }
    }

    /**
     * 广播群组更新给其他成员
     */
    private void broadcastGroupUpdate(String groupId, Group group, String kickedUser) {
        try {
            WsResponse updateResponse = WsResponse.builder()
                    .type("GROUP_UPDATED")
                    .data(group)
                    .build();

            String json = objectMapper.writeValueAsString(updateResponse);

            // 通知所有其他成员（不包括被踢出的用户）
            for (String member : group.getMembers()) {
                if (!member.equals(kickedUser)) {
                    org.springframework.web.socket.WebSocketSession memberSession = DataCenter.ONLINE_USERS.get(member);
                    if (memberSession != null && memberSession.isOpen()) {
                        try {
                            synchronized (memberSession) {
                                memberSession.sendMessage(new TextMessage(json));
                            }
                        } catch (Exception e) {
                            System.err.println("通知用户 " + member + " 群组更新失败: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("广播群组更新失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 广播踢人系统消息给所有群成员（包括被踢出的用户，因为他还在群里的时候需要看到）
     */
    private void broadcastKickNotice(String groupId, Group group, String operator, String kickedUser) {
        try {
            Map<String, Object> noticeData = new HashMap<>();
            noticeData.put("groupId", groupId);
            noticeData.put("operator", operator);
            noticeData.put("targetUser", kickedUser);
            noticeData.put("message", operator + " 将 " + kickedUser + " 踢出了群聊");
            
            WsResponse notice = WsResponse.builder()
                    .type("GROUP_KICK_NOTICE")
                    .data(noticeData)
                    .build();
            
            String json = objectMapper.writeValueAsString(notice);
            
            // 通知所有群成员（包括被踢出的用户，因为此时他还在成员列表中）
            for (String member : group.getMembers()) {
                WebSocketSession memberSession = DataCenter.ONLINE_USERS.get(member);
                if (memberSession != null && memberSession.isOpen()) {
                    try {
                        synchronized (memberSession) {
                            memberSession.sendMessage(new TextMessage(json));
                        }
                    } catch (Exception e) {
                        System.err.println("通知用户 " + member + " 踢人消息失败: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("广播踢人系统消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

