package com.cool.server.websocket;

import com.cool.common.enumeration.OnlineStatus;
import com.cool.server.context.BaseContext;
import com.cool.server.service.OnlineStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    
    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private static final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private OnlineStatusService onlineStatusService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从会话中获取用户ID（实际项目中需要从认证信息中获取）
        // 这里假设前端在连接时会传递用户ID参数
        String query = session.getUri().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("userId=")) {
                    Long userId = Long.parseLong(param.substring(7));
                    sessions.put(userId, session);
                    // 更新用户状态为在线
                    onlineStatusService.updateStatus(userId, OnlineStatus.ONLINE);
                    log.info("用户 {} WebSocket连接建立: {}", userId, session.getId());
                    break;
                }
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("收到消息: {}", payload);
        
        // 解析消息内容
        Map<String, Object> msgMap = objectMapper.readValue(payload, Map.class);
        Long targetUserId = Long.parseLong(msgMap.get("targetUserId").toString());
        String content = msgMap.get("content").toString();
        Long userId = Long.parseLong(msgMap.get("userId").toString());
        
        // 更新用户活跃时间
        onlineStatusService.handleHeartbeat(userId);
        
        // 构建回复消息
        Map<String, Object> response = new HashMap<>();
        response.put("type", "message");
        response.put("fromUserId", userId);
        response.put("content", content);
        response.put("time", System.currentTimeMillis());
        
        // 发送消息给目标用户
        WebSocketSession targetSession = sessions.get(targetUserId);
        if (targetSession != null && targetSession.isOpen()) {
            targetSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }
        
        // 发送确认消息给发送方
        Map<String, Object> confirmResponse = new HashMap<>();
        confirmResponse.put("type", "confirm");
        confirmResponse.put("status", "success");
        confirmResponse.put("messageId", msgMap.get("messageId"));
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(confirmResponse)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket连接关闭: {}, 状态: {}", session.getId(), status);
        // 移除会话并更新用户状态为离线
        sessions.entrySet().removeIf(entry -> {
            if (entry.getValue().getId().equals(session.getId())) {
                // 更新用户状态为离线
                onlineStatusService.handleOffline(entry.getKey());
                log.info("用户 {} WebSocket连接关闭", entry.getKey());
                return true;
            }
            return false;
        });
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: {}", exception.getMessage());
        if (session.isOpen()) {
            session.close();
        }
    }

    public void sendMessageToUser(Long userId, String message) throws IOException {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }

    public void broadcast(String message) throws IOException {
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        }
    }
}
