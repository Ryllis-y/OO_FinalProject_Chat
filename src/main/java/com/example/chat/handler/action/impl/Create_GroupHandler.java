package com.example.chat.handler.action.impl;

import com.example.chat.common.packet.WsRequest;
import com.example.chat.common.model.Group;
import com.example.chat.common.packet.WsResponse;
import com.example.chat.handler.action.BaseActionHandler;
import com.example.chat.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建群组处理器
 */
@Component("CREATE_GROUP")
public class Create_GroupHandler extends BaseActionHandler {
    
    @Autowired
    private UserService userService;
    
    @Override
    public void handle(WebSocketSession session, WsRequest request) {
        try {
            String owner = getCurrentUser(session);
            if (owner == null) {
                sendError(session, "请先登录");
                return;
            }
            
            JsonNode params = request.getParams();
            if (params == null || !params.has("groupName")) {
                sendError(session, "参数错误：缺少 groupName");
                return;
            }
            
            String groupName = params.get("groupName").asText().trim();
            
            if (groupName.isEmpty()) {
                sendError(session, "群组名称不能为空");
                return;
            }
            
            // 提取初始成员列表（可选）
            List<String> initialMembers = null;
            if (params.has("initialMembers") && params.get("initialMembers").isArray()) {
                initialMembers = new ArrayList<>();
                for (JsonNode member : params.get("initialMembers")) {
                    String memberName = member.asText().trim();
                    if (!memberName.isEmpty() && !memberName.equals(owner)) {
                        initialMembers.add(memberName);
                    }
                }
            }
            
            // 调用 UserService 创建群组
            Group group = userService.createGroup(groupName, owner, initialMembers);
            
            // 构建响应
            WsResponse response = WsResponse.builder()
                    .type("GROUP_CREATED")
                    .data(group)
                    .build();
            
            sendResponse(session, response);
            
            System.out.println("用户 " + owner + " 创建群组: " + groupName + " (ID: " + group.getGroupId() + ")");
            
        } catch (IllegalArgumentException e) {
            sendError(session, e.getMessage());
        } catch (Exception e) {
            sendError(session, "创建群组失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

