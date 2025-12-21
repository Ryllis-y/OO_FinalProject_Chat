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
 * 删除好友处理器
 */
@Component("DELETE_FRIEND")
public class Delete_FriendHandler extends BaseActionHandler {

    @Autowired
    private UserService userService;

    @Override
    public void handle(WebSocketSession session, WsRequest request) {
        try {
            String userId = getCurrentUser(session);
            if (userId == null) {
                sendError(session, "请先登录");
                return;
            }

            JsonNode params = request.getParams();
            if (params == null || !params.has("friendId")) {
                sendError(session, "参数错误：缺少 friendId");
                return;
            }

            String friendId = params.get("friendId").asText();

            if (userId.equals(friendId)) {
                sendError(session, "不能删除自己");
                return;
            }

            // 调用 UserService 删除好友
            boolean success = userService.removeFriend(userId, friendId);

            if (success) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("friendId", friendId);
                responseData.put("message", "已删除好友");
                // 添加标志，告诉前端需要刷新好友列表
                responseData.put("refreshFriends", true);
                sendSuccess(session, responseData);

                // 通知删除方刷新好友列表（通过发送一个特殊的事件）
                notifyFriendListUpdated(userId);

                // 通知对方好友关系已解除
                notifyFriendRemoved(friendId, userId);

                System.out.println("用户 " + userId + " 删除了好友 " + friendId);
            } else {
                sendError(session, "删除好友失败：可能不是好友关系");
            }

        } catch (Exception e) {
            sendError(session, "删除好友失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 通知删除方好友列表已更新（需要刷新）
     */
    private void notifyFriendListUpdated(String userId) {
        WebSocketSession userSession = DataCenter.ONLINE_USERS.get(userId);
        if (userSession != null && userSession.isOpen()) {
            try {
                Map<String, Object> noticeData = new HashMap<>();
                noticeData.put("message", "好友列表已更新，请刷新");

                WsResponse notice = WsResponse.builder()
                        .type("FRIEND_LIST_UPDATED")
                        .data(noticeData)
                        .build();

                String json = objectMapper.writeValueAsString(notice);
                userSession.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                System.err.println("通知用户 " + userId + " 好友列表已更新失败: " + e.getMessage());
            }
        }
    }

    /**
     * 通知对方好友关系已解除
     */
    private void notifyFriendRemoved(String friendId, String userId) {
        WebSocketSession friendSession = DataCenter.ONLINE_USERS.get(friendId);
        if (friendSession != null && friendSession.isOpen()) {
            try {
                Map<String, Object> noticeData = new HashMap<>();
                noticeData.put("userId", userId);
                noticeData.put("message", userId + " 已解除与您的好友关系");

                WsResponse notice = WsResponse.builder()
                        .type("FRIEND_REMOVED")
                        .data(noticeData)
                        .build();

                String json = objectMapper.writeValueAsString(notice);
                friendSession.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                System.err.println("通知用户 " + friendId + " 好友关系已解除失败: " + e.getMessage());
            }
        }
    }
}

