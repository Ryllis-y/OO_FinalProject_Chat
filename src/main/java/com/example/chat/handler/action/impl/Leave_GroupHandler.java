package com.example.chat.handler.action.impl;

import com.example.chat.common.packet.WsRequest;
import com.example.chat.common.model.Group;
import com.example.chat.handler.action.BaseActionHandler;
import com.example.chat.repository.DataCenter;
import com.example.chat.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * 退出群组处理器
 */
@Component("LEAVE_GROUP")
public class Leave_GroupHandler extends BaseActionHandler {
    
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
            
            // 检查是否是群主（群主不能退群，需要解散群）
            if (group.getOwner().equals(userId)) {
                sendError(session, "群主不能退出群组，请使用解散群组功能");
                return;
            }
            
            // 调用 UserService 退出群组
            boolean success = userService.leaveGroup(groupId, userId);
            
            if (success) {
                // 获取更新后的群组信息
                Group updatedGroup = DataCenter.GROUPS.get(groupId);
                
                // 构建响应数据
                java.util.Map<String, Object> responseData = new java.util.HashMap<>();
                responseData.put("groupId", groupId);
                responseData.put("groupName", updatedGroup.getGroupName());
                responseData.put("message", "已成功退出群组");
                
                sendSuccess(session, responseData);
                System.out.println("用户 " + userId + " 退出群组: " + updatedGroup.getGroupName());
            } else {
                sendError(session, "退出群组失败：您可能不在该群组中");
            }
            
        } catch (Exception e) {
            sendError(session, "退出群组失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

