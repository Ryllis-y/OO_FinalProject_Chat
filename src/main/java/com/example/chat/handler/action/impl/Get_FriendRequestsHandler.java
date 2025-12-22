package com.example.chat.handler.action.impl;

import com.example.chat.common.model.User;
import com.example.chat.common.packet.WsRequest;
import com.example.chat.handler.action.BaseActionHandler;
import com.example.chat.repository.DataCenter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取好友申请列表处理器
 */
@Component("GET_FRIEND_REQUESTS")
public class Get_FriendRequestsHandler extends BaseActionHandler {

    @Override
    public void handle(WebSocketSession session, WsRequest request) {
        try {
            String userId = getCurrentUser(session);
            if (userId == null) {
                sendError(session, "请先登录");
                return;
            }

            // 获取用户信息
            User user = DataCenter.USERS.get(userId);
            if (user == null) {
                sendError(session, "用户不存在");
                return;
            }

            // 获取好友申请列表
            List<String> requestsList = new ArrayList<>(user.getFriendRequests());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("friendRequests", requestsList);
            responseData.put("count", requestsList.size());

            sendSuccess(session, responseData);

            System.out.println("用户 " + userId + " 获取好友申请列表，共 " + requestsList.size() + " 条申请");

        } catch (Exception e) {
            sendError(session, "获取好友申请列表失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


