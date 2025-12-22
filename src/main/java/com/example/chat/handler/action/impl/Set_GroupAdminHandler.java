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
 * 设置群管理员处理器
 */
@Component("SET_GROUP_ADMIN")
public class Set_GroupAdminHandler extends BaseActionHandler {

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
            
            // 判断是设置还是取消管理员（通过action参数，默认为设置）
            boolean isSet = !params.has("action") || "set".equals(params.get("action").asText());

            boolean success;
            if (isSet) {
                // 设置管理员
                success = userService.setGroupAdmin(operator, groupId, targetUser);
            } else {
                // 取消管理员
                success = userService.removeGroupAdmin(operator, groupId, targetUser);
            }

            if (success) {
                // 获取更新后的群组信息
                Group updatedGroup = DataCenter.GROUPS.get(groupId);
                
                // 构建响应数据
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("groupId", groupId);
                responseData.put("targetUser", targetUser);
                responseData.put("action", isSet ? "set" : "remove");
                responseData.put("message", isSet ? "已成功设置 " + targetUser + " 为管理员" : "已成功取消 " + targetUser + " 的管理员权限");

                sendSuccess(session, responseData);

                // 广播群组更新给所有成员
                broadcastGroupUpdate(groupId, updatedGroup);

                System.out.println("群管理员操作: " + operator + " 在群组 " + updatedGroup.getGroupName() + 
                    (isSet ? " 中设置 " : " 中取消 ") + targetUser + " 为管理员");
            } else {
                sendError(session, isSet ? "设置管理员失败" : "取消管理员失败");
            }

        } catch (SecurityException e) {
            sendError(session, e.getMessage());
        } catch (Exception e) {
            sendError(session, "操作失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 广播群组更新给所有成员
     */
    private void broadcastGroupUpdate(String groupId, Group group) {
        try {
            WsResponse updateResponse = WsResponse.builder()
                    .type("GROUP_UPDATED")
                    .data(group)
                    .build();

            String json = objectMapper.writeValueAsString(updateResponse);

            // 通知所有成员
            for (String member : group.getMembers()) {
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
        } catch (Exception e) {
            System.err.println("广播群组更新失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


