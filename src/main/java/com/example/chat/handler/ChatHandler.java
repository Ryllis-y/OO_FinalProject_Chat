package com.example.chat.handler;

import com.example.chat.common.model.User;
import com.example.chat.common.packet.WsRequest;
import com.example.chat.common.packet.WsResponse;
import com.example.chat.handler.action.ActionHandler;
import com.example.chat.repository.DataCenter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.Executor;

/**
 * 这是你的核心工作类：所有 WebSocket 消息都会流向这里
 */
@Component
public class ChatHandler extends TextWebSocketHandler {

    // 注入线程池
    @Autowired
    @Qualifier("chatExecutor")
    private Executor threadPool;

    // 3. 注入 JSON 工具
    @Autowired
    private ObjectMapper jsonMapper;
    
    // 4. 注入 Handler 注册表
    @Autowired
    private HandlerRegistry handlerRegistry;

    // --- 下面这三个方法是 WebSocket 的生命周期 ---

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("新用户连接: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("收到消息: " + payload);

        // 1. 解析 JSON
        WsRequest request;
        try {
            request = jsonMapper.readValue(payload, WsRequest.class);
        } catch (Exception e) {
            sendError(session, "JSON格式错误");
            return;
        }

        // 2. 检查 Action
        if (request.getAction() == null) {
            return;
        }

        // 3. 优先使用 HandlerRegistry 路由（新架构）
        ActionHandler handler = handlerRegistry.getHandler(request.getAction());
        if (handler != null) {
            // 对于可能耗时的操作，使用线程池异步处理
            if ("SEND_PRIVATE".equals(request.getAction()) || "SEND_GROUP".equals(request.getAction())) {
                threadPool.execute(() -> handler.handle(session, request));
            } else {
                handler.handle(session, request);
            }
            return;
        }

        // 4. 如果 HandlerRegistry 中没有找到处理器，返回错误
                sendError(session, "未知指令: " + request.getAction());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 从 session 属性里拿到用户名
        String username = (String) session.getAttributes().get("username");
        if (username != null) {
            DataCenter.ONLINE_USERS.remove(username);
            System.out.println("用户下线: " + username);
            // 广播在线用户列表更新给所有在线用户
            broadcastOnlineUsersUpdate();
        }
        }

    /**
     * 广播在线用户列表更新给所有在线用户
     */
    private void broadcastOnlineUsersUpdate() {
        try {
            // 获取所有在线用户
            java.util.List<User> onlineUserList = new java.util.ArrayList<>();
            for (String username : DataCenter.ONLINE_USERS.keySet()) {
                User u = DataCenter.USERS.get(username);
                if (u != null) {
                    onlineUserList.add(u);
                }
            }
            
            // 构建更新响应
            WsResponse updateResponse = WsResponse.builder()
                    .type("ONLINE_LIST")
                    .data(onlineUserList)
                    .build();
            
            String json = jsonMapper.writeValueAsString(updateResponse);
            
            // 广播给所有在线用户
            for (WebSocketSession onlineSession : DataCenter.ONLINE_USERS.values()) {
                if (onlineSession != null && onlineSession.isOpen()) {
                    try {
                        synchronized (onlineSession) {
                            onlineSession.sendMessage(new TextMessage(json));
                    }
                    } catch (Exception e) {
                        System.err.println("广播在线用户列表更新失败: " + e.getMessage());
                }
                }
            }
        } catch (Exception e) {
            System.err.println("广播在线用户列表更新时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * 核心辅助方法：给指定会话发送 JSON 数据
     * 加上 synchronized 是为了防止多线程同时写入导致数据错乱
     */
    private void sendJson(WebSocketSession session, Object response) {
        try {
            // 1. 把 Java 对象转成 JSON 字符串
            String json = jsonMapper.writeValueAsString(response);
            // 2. 发送
            if (session.isOpen()) {
                synchronized (session) {
                    session.sendMessage(new TextMessage(json));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 辅助方法：给前端发一个错误提示
     */
    private void sendError(WebSocketSession session, String errorMsg) {
        WsResponse response = WsResponse.error(errorMsg);
        sendJson(session, response);
    }
}