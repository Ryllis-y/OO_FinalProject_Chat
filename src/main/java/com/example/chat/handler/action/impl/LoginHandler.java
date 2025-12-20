package com.example.chat.handler.action.impl;

import com.example.chat.common.packet.WsRequest;
import com.example.chat.common.packet.WsResponse;
import com.example.chat.common.model.User;
import com.example.chat.handler.action.BaseActionHandler;
import com.example.chat.repository.DataCenter;
import com.example.chat.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;

/**
 * 登录处理器
 * 功能：
 * 1. 新用户自动注册
 * 2. 老用户验证密码
 * 3. 管理员自动识别
 * 4. 多端顶号处理
 */
@Component
public class LoginHandler extends BaseActionHandler {
    
    @Autowired
    private UserService userService;
    
    @Override
    public void handle(WebSocketSession session, WsRequest request) {
        try {
            JsonNode params = request.getParams();
            
            // 1. 参数校验
            if (params == null || !params.has("username") || !params.has("password")) {
                sendError(session, "参数错误：缺少 username 或 password");
                return;
            }
            
            String username = params.get("username").asText();
            String password = params.get("password").asText();
            
            if (username == null || username.trim().isEmpty()) {
                sendError(session, "用户名不能为空");
                return;
            }
            
            if (password == null || password.trim().isEmpty()) {
                sendError(session, "密码不能为空");
                return;
            }
            
            // 2. 在调用UserService之前，先获取旧session（用于多端顶号判断）
            WebSocketSession oldSession = DataCenter.ONLINE_USERS.get(username);
            boolean isSameSession = oldSession != null && oldSession.getId().equals(session.getId());
            
            // 3. 处理多端顶号（如果旧session存在且不是同一个session）
            if (oldSession != null && oldSession.isOpen() && !isSameSession) {
                handleMultiDeviceLogin(username, oldSession);
            }
            
            // 4. 调用UserService登录（处理用户注册/验证密码，并绑定session）
            User user;
            try {
                user = userService.login(username, password, session);
            } catch (IllegalArgumentException e) {
                sendError(session, e.getMessage());
                return;
            }
            
            // 5. 在session中保存用户名，用于后续认证
            session.getAttributes().put("username", username);
            
            // 6. 构建登录成功响应
            WsResponse response = WsResponse.builder()
                    .type("LOGIN_RESP")
                    .data(user)  // User对象的password字段会自动被@JsonIgnore过滤
                    .build();
            
            sendResponse(session, response);
            
            System.out.println("用户登录成功: " + username + ", 角色: " + user.getRole());
            
        } catch (Exception e) {
            sendError(session, "登录失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理多端顶号通知
     * 向旧连接发送被顶号通知并关闭连接
     * @param username 用户名
     * @param oldSession 旧的WebSocket会话（将被关闭）
     */
    private void handleMultiDeviceLogin(String username, WebSocketSession oldSession) {
        try {
            // 发送被顶号通知
            WsResponse kickOutResponse = WsResponse.builder()
                    .type("SYS_NOTICE")
                    .code(400)
                    .msg("您的账号在另一地点登录，您已被强制下线")
                    .build();
            
            String json = objectMapper.writeValueAsString(kickOutResponse);
            oldSession.sendMessage(new TextMessage(json));
            oldSession.close();
            
            System.out.println("用户被顶号: " + username + ", 旧session: " + oldSession.getId());
            
        } catch (IOException e) {
            System.err.println("发送顶号通知失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
