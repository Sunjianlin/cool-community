package com.cool.server.service.notify;

import com.cool.pojo.entity.notify.NotifyMessage;

public interface PushService {

    void pushAppMessage(Long receiverId, String content);

    void pushWebSocketMessage(Long receiverId, Object message);
}
