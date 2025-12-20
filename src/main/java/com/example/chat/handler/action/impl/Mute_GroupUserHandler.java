package com.example.chat.handler.action.impl;

import com.example.chat.common.packet.WsRequest;
import com.example.chat.handler.action.BaseActionHandler;
import com.example.chat.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

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
                // 构建响应数据
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("groupId", groupId);
                responseData.put("targetUser", targetUser);
                responseData.put("durationMinutes", durationMinutes);
                responseData.put("message", "已禁言用户 " + targetUser + " " + durationMinutes + " 分钟");
                sendSuccess(session, responseData);
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
}

