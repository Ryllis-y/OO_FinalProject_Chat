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
 * 拒绝好友申请处理器
 */
@Component("REJECT_FRIEND_REQUEST")
public class Reject_FriendRequestHandler extends BaseActionHandler {

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

            // 调用 UserService 拒绝好友申请
            boolean success = userService.rejectFriendRequest(toUser, fromUser);

            if (success) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("fromUser", fromUser);
                responseData.put("message", "已拒绝 " + fromUser + " 的好友申请");
                sendSuccess(session, responseData);

                System.out.println("用户 " + toUser + " 拒绝了 " + fromUser + " 的好友申请");
            } else {
                sendError(session, "拒绝好友申请失败：申请不存在");
            }

        } catch (Exception e) {
            sendError(session, "拒绝好友申请失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

