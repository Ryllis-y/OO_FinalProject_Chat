package com.example.chat.handler.action.impl;

import com.example.chat.common.packet.WsRequest;
import com.example.chat.common.model.Message;
import com.example.chat.common.model.Group;
import com.example.chat.common.packet.WsResponse;
import com.example.chat.handler.action.BaseActionHandler;
import com.example.chat.repository.DataCenter;
import com.example.chat.service.MessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 发送群聊消息处理器
 */
@Component
public class Send_GroupHandler extends BaseActionHandler {
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public void handle(WebSocketSession session, WsRequest request) {
        try {
            String fromUser = getCurrentUser(session);
            if (fromUser == null) {
                sendError(session, "请先登录");
                return;
            }
            
            JsonNode params = request.getParams();
            if (params == null || !params.has("groupId") || !params.has("content")) {
                sendError(session, "参数错误：缺少 groupId 或 content");
                return;
            }
            
            String groupId = params.get("groupId").asText();
            String content = params.get("content").asText().trim();
            
            if (content.isEmpty()) {
                sendError(session, "消息内容不能为空");
                return;
            }
            
            // 检查群组是否存在
            Group group = DataCenter.GROUPS.get(groupId);
            if (group == null) {
                sendError(session, "群组不存在");
                return;
            }
            
            // 检查用户是否在群中
            if (!group.getMembers().contains(fromUser)) {
                sendError(session, "您不是该群组的成员");
                return;
            }
            
            // 提取 @ 列表
            List<String> atUsers = new ArrayList<>();
            if (params.has("atUsers") && params.get("atUsers").isArray()) {
                for (JsonNode atUser : params.get("atUsers")) {
                    atUsers.add(atUser.asText());
                }
            }
            
            // 创建并保存消息
            Message message = messageService.processAndSaveMsg(fromUser, groupId, content, true, atUsers);
            
            // 发送给所有在线群成员
            sendToGroupMembers(group, message, fromUser);
            
            // 发送回执给发送者
            sendSuccess(session, message);
            
            System.out.println("群聊消息: " + fromUser + " -> 群组[" + group.getGroupName() + "]: " + content);
            
        } catch (Exception e) {
            sendError(session, "发送失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 发送消息给群组所有在线成员
     */
    private void sendToGroupMembers(Group group, Message message, String sender) {
        try {
            WsResponse response = WsResponse.builder()
                    .type("EVENT_CHAT_MSG")
                    .data(message)
                    .build();
            String json = objectMapper.writeValueAsString(response);
            TextMessage textMessage = new TextMessage(json);
            
            // 遍历群成员，发送给所有在线成员
            Set<String> sentTo = ConcurrentHashMap.newKeySet();
            for (String member : group.getMembers()) {
                WebSocketSession memberSession = DataCenter.ONLINE_USERS.get(member);
                if (memberSession != null && memberSession.isOpen()) {
                    try {
                        memberSession.sendMessage(textMessage);
                        sentTo.add(member);
                    } catch (Exception e) {
                        System.err.println("发送消息给群成员失败: " + member + ", " + e.getMessage());
                    }
                }
            }
            
            System.out.println("群消息已发送给 " + sentTo.size() + " 个在线成员");
            
        } catch (Exception e) {
            System.err.println("发送群消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

