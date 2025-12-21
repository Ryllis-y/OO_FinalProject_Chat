package com.example.chat.handler.action.impl;

import com.example.chat.common.packet.WsRequest;
import com.example.chat.common.model.Group;
import com.example.chat.common.model.Message;
import com.example.chat.handler.action.BaseActionHandler;
import com.example.chat.repository.DataCenter;
import com.example.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            
            // 为每个群组添加最后一条消息信息
            List<Map<String, Object>> groupsWithLastMsg = groups.stream().map(group -> {
                Map<String, Object> groupInfo = new HashMap<>();
                groupInfo.put("groupId", group.getGroupId());
                groupInfo.put("groupName", group.getGroupName());
                groupInfo.put("owner", group.getOwner());
                groupInfo.put("members", group.getMembers());
                groupInfo.put("admins", group.getAdmins());
                
                // 查找群组的最后一条消息
                Message lastMessage = findLastGroupMessage(group.getGroupId());
                if (lastMessage != null) {
                    groupInfo.put("lastMsg", lastMessage.getContent());
                    groupInfo.put("lastMsgTime", lastMessage.getTimestamp());
                } else {
                    groupInfo.put("lastMsg", "");
                    groupInfo.put("lastMsgTime", 0L);
                }
                
                return groupInfo;
            }).collect(Collectors.toList());
            
            // 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("groups", groupsWithLastMsg);
            responseData.put("count", groupsWithLastMsg.size());
            
            sendSuccess(session, responseData);
            
            System.out.println("用户 " + userId + " 获取群组列表，共 " + groupsWithLastMsg.size() + " 个群组");
            
        } catch (Exception e) {
            sendError(session, "获取群组列表失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 查找群组的最后一条消息
     */
    private Message findLastGroupMessage(String groupId) {
        return DataCenter.MSG_HISTORY.values().stream()
                .filter(msg -> msg.isGroup() && msg.getToUser().equals(groupId))
                .sorted((m1, m2) -> Long.compare(m2.getTimestamp(), m1.getTimestamp())) // 按时间倒序
                .findFirst()
                .orElse(null);
    }
}

