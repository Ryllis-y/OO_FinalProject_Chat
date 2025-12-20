package com.example.chat.handler.action.impl;

import com.example.chat.common.packet.WsRequest;
import com.example.chat.common.model.Group;
import com.example.chat.handler.action.BaseActionHandler;
import com.example.chat.repository.DataCenter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取群组成员列表处理器
 */
@Component("GET_GROUP_MEMBERS")
public class Get_GroupMembersHandler extends BaseActionHandler {
    
    @Override
    public void handle(WebSocketSession session, WsRequest request) {
        try {
            String userId = getCurrentUser(session);
            if (userId == null) {
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
            
            // 检查用户是否在群中
            if (!group.getMembers().contains(userId)) {
                sendError(session, "您不是该群组的成员，无法查看成员列表");
                return;
            }
            
            // 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("groupId", groupId);
            responseData.put("groupName", group.getGroupName());
            responseData.put("owner", group.getOwner());
            responseData.put("members", group.getMembers());
            responseData.put("admins", group.getAdmins());
            responseData.put("memberCount", group.getMembers().size());
            
            // 使用 GROUP_MEMBERS_RESP 类型，而不是 SUCCESS
            com.example.chat.common.packet.WsResponse response = com.example.chat.common.packet.WsResponse.builder()
                    .type("GROUP_MEMBERS_RESP")
                    .data(responseData)
                    .build();
            sendResponse(session, response);
            
            System.out.println("用户 " + userId + " 获取群组 " + group.getGroupName() + " 的成员列表");
            
        } catch (Exception e) {
            sendError(session, "获取群成员列表失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

