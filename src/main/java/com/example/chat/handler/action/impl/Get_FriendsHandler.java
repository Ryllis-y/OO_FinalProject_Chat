package com.example.chat.handler.action.impl;

import com.example.chat.common.model.User;
import com.example.chat.common.model.Message;
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
 * 获取好友列表处理器
 */
@Component("GET_FRIENDS")
public class Get_FriendsHandler extends BaseActionHandler {

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

            // 获取好友列表（包含在线状态和最后一条消息）
            List<Map<String, Object>> friendsList = new ArrayList<>();
            for (String friendId : user.getFriends()) {
                User friend = DataCenter.USERS.get(friendId);
                if (friend != null) {
                    Map<String, Object> friendInfo = new HashMap<>();
                    friendInfo.put("userId", friend.getUserId());
                    friendInfo.put("username", friend.getUsername());
                    friendInfo.put("isOnline", DataCenter.ONLINE_USERS.containsKey(friendId));
                    
                    // 查找与这个好友的最后一条消息
                    Message lastMessage = findLastPrivateMessage(userId, friendId);
                    if (lastMessage != null) {
                        friendInfo.put("lastMsg", lastMessage.getContent());
                        friendInfo.put("lastMsgTime", lastMessage.getTimestamp());
                    } else {
                        friendInfo.put("lastMsg", "");
                        friendInfo.put("lastMsgTime", 0L);
                    }
                    
                    friendsList.add(friendInfo);
                }
            }

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("friends", friendsList);
            responseData.put("count", friendsList.size());

            sendSuccess(session, responseData);

            System.out.println("用户 " + userId + " 获取好友列表，共 " + friendsList.size() + " 个好友");

        } catch (Exception e) {
            sendError(session, "获取好友列表失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 查找两个用户之间的最后一条私聊消息
     */
    private Message findLastPrivateMessage(String user1, String user2) {
        return DataCenter.MSG_HISTORY.values().stream()
                .filter(msg -> !msg.isGroup())
                .filter(msg -> {
                    boolean isFrom1To2 = msg.getFromUser().equals(user1) && msg.getToUser().equals(user2);
                    boolean isFrom2To1 = msg.getFromUser().equals(user2) && msg.getToUser().equals(user1);
                    return isFrom1To2 || isFrom2To1;
                })
                .sorted((m1, m2) -> Long.compare(m2.getTimestamp(), m1.getTimestamp())) // 按时间倒序
                .findFirst()
                .orElse(null);
    }
}

