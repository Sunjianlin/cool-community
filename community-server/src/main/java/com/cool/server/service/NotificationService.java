package com.cool.server.service;

import com.cool.pojo.vo.NotificationVO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.dto.PageQueryDTO;

import java.util.List;

public interface NotificationService {
    
    int TYPE_FOLLOW = 0;
    int TYPE_LIKE = 1;
    int TYPE_COMMENT = 2;
    int TYPE_SYSTEM = 3;
    
    void createFollowNotification(Long fromUserId, Long toUserId);
    List<NotificationVO> getNotifications();
    void markAsRead(Long id);
    void markAllAsRead();
    Integer getUnreadCount();
}
