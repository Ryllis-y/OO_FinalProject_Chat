package com.example.chat.handler.action.impl;

import com.example.chat.common.packet.WsRequest;
import com.example.chat.common.model.Group;
import com.example.chat.handler.action.BaseActionHandler;
import com.example.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 获取用户所在群组列表处理器
 */
@Component("GET_GROUPS")
public class Get_GroupsHandler extends BaseActionHandler {
    
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
            
            // 获取用户所在的所有群组
            List<Group> groups = userService.getUserGroups(userId);
            
            // 构建响应数据（只返回必要的字段，避免数据过大）
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("groups", groups);
            responseData.put("count", groups.size());
            
            sendSuccess(session, responseData);
            
            System.out.println("用户 " + userId + " 获取群组列表，共 " + groups.size() + " 个群组");
            
        } catch (Exception e) {
            sendError(session, "获取群组列表失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

