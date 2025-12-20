package com.example.chat.handler.action.impl;

import com.example.chat.common.packet.WsRequest;
import com.example.chat.common.model.Group;
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
 * 解散群组处理器（仅群主可用）
 */
@Component("DISSOLVE_GROUP")
public class Dissolve_GroupHandler extends BaseActionHandler {

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
            if (params == null || !params.has("groupId")) {
                sendError(session, "参数错误：缺少 groupId");
                return;
            }

            String groupId = params.get("groupId").asText();

            if (groupId == null || groupId.trim().isEmpty()) {
                sendError(session, "群组ID不能为空");
                return;
            }

            // 检查群组是否存在
            Group group = DataCenter.GROUPS.get(groupId);
            if (group == null) {
                sendError(session, "群组不存在");
                return;
            }

            // 检查是否是群主
            if (!group.getOwner().equals(operator)) {
                sendError(session, "只有群主可以解散群组");
                return;
            }

            // 调用 UserService 解散群组
            boolean success = userService.dissolveGroup(groupId, operator);

            if (success) {
                // 构建响应数据
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("groupId", groupId);
                responseData.put("groupName", group.getGroupName());
                responseData.put("message", "群组已成功解散");

                sendSuccess(session, responseData);

                // 广播群组解散事件给所有群成员
                broadcastGroupDissolved(group, operator);

                System.out.println("群组已解散: " + group.getGroupName() + " (ID: " + groupId + "), 操作者: " + operator);
            } else {
                sendError(session, "解散群组失败");
            }

        } catch (Exception e) {
            sendError(session, "解散群组失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 广播群组解散事件给所有群成员
     */
    private void broadcastGroupDissolved(Group group, String operator) {
        try {
            // 构建解散事件响应
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("groupId", group.getGroupId());
            eventData.put("groupName", group.getGroupName());
            eventData.put("operator", operator);
            eventData.put("message", "群组已被群主解散");

            WsResponse event = WsResponse.builder()
                    .type("GROUP_DISSOLVED")
                    .data(eventData)
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);

            // 通知所有群成员（包括群主）
            for (String member : group.getMembers()) {
                WebSocketSession memberSession = DataCenter.ONLINE_USERS.get(member);
                if (memberSession != null && memberSession.isOpen()) {
                    try {
                        synchronized (memberSession) {
                            memberSession.sendMessage(new TextMessage(eventJson));
                        }
                        String groupName = group.getGroupName();
                        System.out.println("通知用户 " + member + " 群组已解散: " + (groupName != null ? groupName : ""));
                    } catch (Exception e) {
                        System.err.println("通知用户 " + member + " 群组解散失败: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("广播群组解散事件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

