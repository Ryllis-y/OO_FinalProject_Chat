package com.example.chat.handler.action.impl;

import com.example.chat.common.packet.WsRequest;
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
 * 系统管理员踢出在线用户处理器
 */
@Component("KICK_USER")
public class Kick_UserHandler extends BaseActionHandler {

    @Autowired
    private UserService userService;

    @Override
    public void handle(WebSocketSession session, WsRequest request) {
        try {
            String adminId = getCurrentUser(session);
            if (adminId == null) {
                sendError(session, "请先登录");
                return;
            }

            // 检查是否是管理员
            if (!isAdmin(adminId)) {
                sendError(session, "无权限操作：只有系统管理员可以踢出用户");
                return;
            }

            JsonNode params = request.getParams();
            if (params == null || !params.has("targetUser")) {
                sendError(session, "参数错误：缺少 targetUser");
                return;
            }

            String targetUser = params.get("targetUser").asText();

            // 不能踢出自己
            if (targetUser.equals(adminId)) {
                sendError(session, "不能踢出自己");
                return;
            }

            // 调用 UserService 踢出用户
            boolean success = userService.kickUser(adminId, targetUser);

            if (success) {
                // 构建响应数据
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("targetUser", targetUser);
                responseData.put("message", "已成功将用户踢下线");

                sendSuccess(session, responseData);

                // 广播在线用户列表更新
                broadcastOnlineUsersUpdate();

                System.out.println("系统管理员踢人: " + adminId + " 踢出了 " + targetUser);
            } else {
                sendError(session, "踢出失败：用户不在线");
            }

        } catch (SecurityException e) {
            sendError(session, e.getMessage());
        } catch (Exception e) {
            sendError(session, "踢出失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 广播在线用户列表更新给所有在线用户
     */
    private void broadcastOnlineUsersUpdate() {
        try {
            // 获取所有在线用户
            java.util.List<com.example.chat.common.model.User> onlineUserList = new java.util.ArrayList<>();
            for (String username : DataCenter.ONLINE_USERS.keySet()) {
                com.example.chat.common.model.User u = DataCenter.USERS.get(username);
                if (u != null) {
                    onlineUserList.add(u);
                }
            }

            // 构建更新响应
            WsResponse updateResponse = WsResponse.builder()
                    .type("ONLINE_LIST")
                    .data(onlineUserList)
                    .build();

                String json = objectMapper.writeValueAsString(updateResponse);
                TextMessage message = new TextMessage(json);

                // 广播给所有在线用户
                for (WebSocketSession onlineSession : DataCenter.ONLINE_USERS.values()) {
                    if (onlineSession != null && onlineSession.isOpen()) {
                        try {
                            synchronized (onlineSession) {
                                onlineSession.sendMessage(message);
                            }
                    } catch (Exception e) {
                        System.err.println("广播在线用户列表更新失败: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("广播在线用户列表更新时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

