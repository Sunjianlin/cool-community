package com.cool.server.service.impl;

import com.cool.pojo.entity.Notification;
import com.cool.pojo.vo.NotificationVO;
import com.cool.pojo.vo.UserVO;
import com.cool.server.context.BaseContext;
import com.cool.server.mapper.NotificationMapper;
import com.cool.server.mapper.UserMapper;
import com.cool.server.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    
    @Autowired
    private NotificationMapper notificationMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public void createFollowNotification(Long fromUserId, Long toUserId) {
        if (fromUserId.equals(toUserId)) {
            return;
        }
        
        com.cool.pojo.entity.User fromUserEntity = userMapper.getById(fromUserId);
        if (fromUserEntity == null) {
            return;
        }
        
        UserVO fromUser = new UserVO();
        fromUser.setId(fromUserEntity.getId());
        fromUser.setUsername(fromUserEntity.getUsername());
        fromUser.setNickname(fromUserEntity.getNickname());
        fromUser.setAvatar(fromUserEntity.getAvatar());
        
        Notification notification = new Notification();
        notification.setUserId(toUserId);
        notification.setType(TYPE_FOLLOW);
        notification.setContent(fromUser.getNickname() + " 关注了你");
        notification.setIsRead(false);
        notification.setRelatedId(fromUserId);
        
        notificationMapper.insert(notification);
        log.info("用户{}关注了用户{}，发送通知", fromUserId, toUserId);
    }
    
    @Override
    public List<NotificationVO> getNotifications() {
        Long userId = BaseContext.getCurrentId();
        List<Notification> notifications = notificationMapper.getByUserId(userId);
        
        return notifications.stream().map(notification -> {
            NotificationVO vo = new NotificationVO();
            vo.setId(notification.getId());
            vo.setType(notification.getType());
            vo.setContent(notification.getContent());
            vo.setIsRead(notification.getIsRead());
            vo.setRelatedId(notification.getRelatedId());
            vo.setCreateTime(notification.getCreateTime());
            
            if (notification.getType() == TYPE_FOLLOW) {
                com.cool.pojo.entity.User relatedUserEntity = userMapper.getById(notification.getRelatedId());
                if (relatedUserEntity != null) {
                    UserVO relatedUser = new UserVO();
                    relatedUser.setId(relatedUserEntity.getId());
                    relatedUser.setUsername(relatedUserEntity.getUsername());
                    relatedUser.setNickname(relatedUserEntity.getNickname());
                    relatedUser.setAvatar(relatedUserEntity.getAvatar());
                    vo.setRelatedUser(relatedUser);
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
    }
    
    @Override
    public void markAsRead(Long id) {
        notificationMapper.markAsRead(id);
    }
    
    @Override
    public void markAllAsRead() {
        Long userId = BaseContext.getCurrentId();
        notificationMapper.markAllAsRead(userId);
    }
    
    @Override
    public Integer getUnreadCount() {
        Long userId = BaseContext.getCurrentId();
        return notificationMapper.countUnread(userId);
    }
}
