package com.cool.server.service.notify.impl;

import com.cool.server.service.notify.PushService;
import com.cool.server.websocket.ChatWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushServiceImpl implements PushService {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final ObjectMapper objectMapper;

    @Override
    public void pushAppMessage(Long receiverId, String content) {
        log.info("推送APP消息: receiverId={}, content={}", receiverId, content);
    }

    @Override
    public void pushWebSocketMessage(Long receiverId, Object message) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "notification");
            notification.put("data", message);
            notification.put("time", System.currentTimeMillis());
            
            String jsonMessage = objectMapper.writeValueAsString(notification);
            chatWebSocketHandler.sendMessageToUser(receiverId, jsonMessage);
            log.info("WebSocket推送成功: receiverId={}", receiverId);
        } catch (Exception e) {
            log.error("WebSocket推送失败: receiverId={}", receiverId, e);
        }
    }
}
