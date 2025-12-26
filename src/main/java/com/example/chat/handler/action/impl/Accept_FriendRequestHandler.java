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
 * 接受好友申请处理器
 */
@Component("ACCEPT_FRIEND_REQUEST")
public class Accept_FriendRequestHandler extends BaseActionHandler {

    @Autowired
    private UserService userService;

    @Override
    public void handle(WebSocketSession session, WsRequest request) {
        try {
            String toUser = getCurrentUser(session);
            if (toUser == null) {
                sendError(session, "请先登录");
                return;
            }

            JsonNode params = request.getParams();
            if (params == null || !params.has("fromUser")) {
                sendError(session, "参数错误：缺少 fromUser");
                return;
            }

            String fromUser = params.get("fromUser").asText();

            // 调用 UserService 接受好友申请
            boolean success = userService.acceptFriendRequest(toUser, fromUser);

            if (success) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("fromUser", fromUser);
                responseData.put("message", "已接受 " + fromUser + " 的好友申请");
                sendSuccess(session, responseData);

                // 通知对方已接受好友申请
                notifyFriendAccepted(fromUser, toUser);

                System.out.println("用户 " + toUser + " 接受了 " + fromUser + " 的好友申请");
            } else {
                sendError(session, "接受好友申请失败：申请不存在或已处理");
            }

        } catch (Exception e) {
            sendError(session, "接受好友申请失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 通知对方已接受好友申请
     */
    private void notifyFriendAccepted(String fromUser, String toUser) {
        WebSocketSession targetSession = DataCenter.ONLINE_USERS.get(fromUser);
        if (targetSession != null && targetSession.isOpen()) {
            try {
                Map<String, Object> noticeData = new HashMap<>();
                noticeData.put("toUser", toUser);
                noticeData.put("message", toUser + " 接受了您的好友申请");

                WsResponse notice = WsResponse.builder()
                        .type("FRIEND_REQUEST_ACCEPTED")
                        .data(noticeData)
                        .build();

                String json = objectMapper.writeValueAsString(notice);
                targetSession.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                System.err.println("通知用户 " + fromUser + " 好友申请已接受失败: " + e.getMessage());
            }
        }
    }
}



