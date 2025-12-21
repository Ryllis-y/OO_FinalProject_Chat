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
 * 群聊禁言用户处理器
 */
@Component("MUTE_GROUP_USER")
public class Mute_GroupUserHandler extends BaseActionHandler {

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
            if (params == null || !params.has("groupId") || !params.has("targetUser") || !params.has("durationMinutes")) {
                sendError(session, "参数错误：缺少 groupId、targetUser 或 durationMinutes");
                return;
            }

            String groupId = params.get("groupId").asText();
            String targetUser = params.get("targetUser").asText();
            int durationMinutes = params.get("durationMinutes").asInt();

            if (durationMinutes <= 0) {
                sendError(session, "禁言时间必须大于0");
                return;
            }

            long durationMillis = durationMinutes * 60 * 1000L;

            // 调用 UserService 禁言
            boolean success = userService.muteGroupUser(operator, groupId, targetUser, durationMillis);

            if (success) {
                // 获取群组信息
                Group group = DataCenter.GROUPS.get(groupId);
                
                // 构建响应数据
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("groupId", groupId);
                responseData.put("targetUser", targetUser);
                responseData.put("durationMinutes", durationMinutes);
                responseData.put("message", "已禁言用户 " + targetUser + " " + durationMinutes + " 分钟");
                sendSuccess(session, responseData);
                
                // 广播禁言系统消息给所有群成员
                broadcastMuteNotice(groupId, group, operator, targetUser, durationMinutes);
                
                System.out.println("群聊禁言: " + operator + " 在群组 " + groupId + " 中禁言了 " + targetUser + " " + durationMinutes + " 分钟");
            } else {
                sendError(session, "禁言失败");
            }

        } catch (SecurityException e) {
            sendError(session, e.getMessage());
        } catch (Exception e) {
            sendError(session, "禁言失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 广播禁言系统消息给所有群成员
     */
    private void broadcastMuteNotice(String groupId, Group group, String operator, String targetUser, int durationMinutes) {
        try {
            Map<String, Object> noticeData = new HashMap<>();
            noticeData.put("groupId", groupId);
            noticeData.put("operator", operator);
            noticeData.put("targetUser", targetUser);
            noticeData.put("durationMinutes", durationMinutes);
            noticeData.put("message", operator + " 禁言了 " + targetUser + " " + durationMinutes + " 分钟");
            
            WsResponse notice = WsResponse.builder()
                    .type("GROUP_MUTE_NOTICE")
                    .data(noticeData)
                    .build();
            
            String json = objectMapper.writeValueAsString(notice);
            
            // 通知所有群成员
            for (String member : group.getMembers()) {
                WebSocketSession memberSession = DataCenter.ONLINE_USERS.get(member);
                if (memberSession != null && memberSession.isOpen()) {
                    try {
                        synchronized (memberSession) {
                            memberSession.sendMessage(new TextMessage(json));
                        }
                    } catch (Exception e) {
                        System.err.println("通知用户 " + member + " 禁言消息失败: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("广播禁言系统消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

