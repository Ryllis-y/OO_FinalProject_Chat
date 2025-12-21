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
 * 发送好友申请处理器
 */
@Component("SEND_FRIEND_REQUEST")
public class Send_FriendRequestHandler extends BaseActionHandler {

    @Autowired
    private UserService userService;

    @Override
    public void handle(WebSocketSession session, WsRequest request) {
        try {
            String fromUser = getCurrentUser(session);
            if (fromUser == null) {
                sendError(session, "请先登录");
                return;
            }

            JsonNode params = request.getParams();
            if (params == null || !params.has("toUser")) {
                sendError(session, "参数错误：缺少 toUser");
                return;
            }

            String toUser = params.get("toUser").asText();

            if (fromUser.equals(toUser)) {
                sendError(session, "不能添加自己为好友");
                return;
            }

            // 调用 UserService 发送好友申请
            boolean success = userService.sendFriendRequest(fromUser, toUser);

            if (success) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("toUser", toUser);
                responseData.put("message", "好友申请已发送");
                sendSuccess(session, responseData);

                // 通知目标用户有新好友申请
                notifyFriendRequest(toUser, fromUser);

                System.out.println("用户 " + fromUser + " 向 " + toUser + " 发送好友申请");
            } else {
                sendError(session, "发送好友申请失败：可能已经发送过申请或已经是好友");
            }

        } catch (Exception e) {
            sendError(session, "发送好友申请失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 通知目标用户有新好友申请
     */
    private void notifyFriendRequest(String toUser, String fromUser) {
        WebSocketSession targetSession = DataCenter.ONLINE_USERS.get(toUser);
        if (targetSession != null && targetSession.isOpen()) {
            try {
                Map<String, Object> noticeData = new HashMap<>();
                noticeData.put("fromUser", fromUser);
                noticeData.put("message", fromUser + " 想要添加您为好友");

                WsResponse notice = WsResponse.builder()
                        .type("FRIEND_REQUEST_NOTICE")
                        .data(noticeData)
                        .build();

                String json = objectMapper.writeValueAsString(notice);
                targetSession.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                System.err.println("通知用户 " + toUser + " 好友申请失败: " + e.getMessage());
            }
        }
    }
}

