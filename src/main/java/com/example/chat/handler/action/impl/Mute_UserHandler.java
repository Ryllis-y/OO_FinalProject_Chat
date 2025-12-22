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
 * 系统管理员全局禁言用户处理器
 */
@Component("MUTE_USER")
public class Mute_UserHandler extends BaseActionHandler {

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
                sendError(session, "无权限操作：只有系统管理员可以禁言用户");
                return;
            }

            JsonNode params = request.getParams();
            if (params == null || !params.has("targetUser") || !params.has("duration")) {
                sendError(session, "参数错误：缺少 targetUser 或 duration");
                return;
            }

            String targetUser = params.get("targetUser").asText();
            long durationMinutes = params.get("duration").asLong();

            // 不能禁言自己
            if (targetUser.equals(adminId)) {
                sendError(session, "不能禁言自己");
                return;
            }

            if (durationMinutes <= 0) {
                sendError(session, "禁言时间必须大于0");
                return;
            }

            // 转换为毫秒
            long durationMillis = durationMinutes * 60 * 1000;

            // 调用 UserService 禁言用户
            boolean success = userService.muteUser(adminId, targetUser, durationMillis);

            if (success) {
                // 构建响应数据
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("targetUser", targetUser);
                responseData.put("duration", durationMinutes);
                responseData.put("message", "已成功禁言用户 " + targetUser + " " + durationMinutes + " 分钟");

                sendSuccess(session, responseData);

                // 通知被禁言的用户
                notifyMutedUser(targetUser, adminId, durationMinutes);

                System.out.println("系统管理员禁言: " + adminId + " 禁言了 " + targetUser + " " + durationMinutes + " 分钟");
            } else {
                sendError(session, "禁言失败：用户不存在");
            }

        } catch (SecurityException e) {
            sendError(session, e.getMessage());
        } catch (Exception e) {
            sendError(session, "禁言失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 通知被禁言的用户
     */
    private void notifyMutedUser(String targetUser, String adminId, long durationMinutes) {
        WebSocketSession targetSession = DataCenter.ONLINE_USERS.get(targetUser);
        if (targetSession != null && targetSession.isOpen()) {
            try {
                Map<String, Object> noticeData = new HashMap<>();
                noticeData.put("operator", adminId);
                noticeData.put("targetUser", targetUser);
                noticeData.put("durationMinutes", durationMinutes);
                noticeData.put("message", adminId + " 禁言了您 " + durationMinutes + " 分钟");

                WsResponse notice = WsResponse.builder()
                        .type("USER_MUTE_NOTICE")
                        .data(noticeData)
                        .build();

                String json = objectMapper.writeValueAsString(notice);
                TextMessage message = new TextMessage(json);
                targetSession.sendMessage(message);
            } catch (Exception e) {
                System.err.println("通知被禁言用户失败: " + e.getMessage());
            }
        }
    }
}

