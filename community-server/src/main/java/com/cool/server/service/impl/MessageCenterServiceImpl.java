package com.cool.server.service.impl;

import com.cool.pojo.dto.NotificationSummaryDTO;
import com.cool.pojo.vo.*;
import com.cool.server.mapper.ChatSessionMapper;
import com.cool.server.mapper.notify.*;
import com.cool.server.service.MessageCenterService;
import com.cool.server.service.producer.NotifyProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageCenterServiceImpl implements MessageCenterService {

    private final UserNotificationCommentMapper userNotificationCommentMapper;
    private final UserNotificationLikeMapper userNotificationLikeMapper;
    private final UserNotificationFollowMapper userNotificationFollowMapper;
    private final UserNotificationSystemMapper userNotificationSystemMapper;
    private final UserNotificationMainMapper userNotificationMainMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final NotifyProducer notifyProducer;

    @Override
    public NotificationSummaryDTO getNotificationSummary(Long userId) {
        NotificationSummaryDTO dto = new NotificationSummaryDTO();
        
        int unreadPrivate = getUnreadPrivateCount(userId);
        long unreadCommentReply = userNotificationCommentMapper.countUnreadCommentReply(userId);
        long unreadPostComment = userNotificationCommentMapper.countUnreadPostComment(userId);
        long unreadLike = userNotificationLikeMapper.countUnread(userId);
        long unreadFollow = userNotificationFollowMapper.countUnread(userId);
        long unreadSystem = userNotificationSystemMapper.countUnread(userId);
        
        dto.setUnreadPrivate(unreadPrivate);
        dto.setUnreadComment((int) (unreadCommentReply + unreadPostComment));
        dto.setUnreadLike((int) unreadLike);
        dto.setUnreadFollow((int) unreadFollow);
        dto.setUnreadSystem((int) unreadSystem);
        dto.setTotalUnread(unreadPrivate + (int) (unreadCommentReply + unreadPostComment) + 
                (int) unreadLike + (int) unreadFollow + (int) unreadSystem);
        
        return dto;
    }

    private int getUnreadPrivateCount(Long userId) {
        List<com.cool.pojo.entity.ChatSession> sessions = chatSessionMapper.selectByUserId(userId);
        
        int totalUnread = 0;
        for (com.cool.pojo.entity.ChatSession session : sessions) {
            if (session.getUserId1().equals(userId)) {
                totalUnread += session.getUnreadCountUser1();
            } else {
                totalUnread += session.getUnreadCountUser2();
            }
        }
        return totalUnread;
    }

    @Override
    public List<CommentReplyNotificationVO> getCommentReplyNotifications(Long userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<CommentReplyNotificationVO> list = userNotificationCommentMapper.selectCommentReplyNotifications(userId, offset, pageSize);
        return list;
    }

    @Override
    public List<PostCommentNotificationVO> getPostCommentNotifications(Long userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<PostCommentNotificationVO> list = userNotificationCommentMapper.selectPostCommentNotifications(userId, offset, pageSize);
        return list;
    }

    @Override
    public List<LikeNotificationVO> getLikeNotifications(Long userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<LikeNotificationVO> list = userNotificationLikeMapper.selectLikeNotifications(userId, offset, pageSize);
        list.forEach(vo -> vo.setType("like"));
        return list;
    }

    @Override
    public List<FollowNotificationVO> getFollowNotifications(Long userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<FollowNotificationVO> list = userNotificationFollowMapper.selectFollowNotifications(userId, offset, pageSize);
        list.forEach(vo -> vo.setType("follow"));
        return list;
    }

    @Override
    public List<SystemNotificationVO> getSystemNotifications(Long userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<SystemNotificationVO> list = userNotificationSystemMapper.selectSystemNotifications(userId, offset, pageSize);
        list.forEach(vo -> vo.setNotificationType("system"));
        return list;
    }

    @Override
    @Transactional
    public void markCommentReplyAsRead(Long userId, Long notificationId) {
        userNotificationCommentMapper.markCommentReplyAsRead(notificationId, userId);
    }

    @Override
    @Transactional
    public void markAllCommentReplyAsRead(Long userId) {
        userNotificationCommentMapper.markAllCommentReplyAsRead(userId);
    }

    @Override
    @Transactional
    public void markPostCommentAsRead(Long userId, Long notificationId) {
        userNotificationCommentMapper.markPostCommentAsRead(notificationId, userId);
    }

    @Override
    @Transactional
    public void markAllPostCommentAsRead(Long userId) {
        userNotificationCommentMapper.markAllPostCommentAsRead(userId);
    }

    @Override
    @Transactional
    public void markLikeAsRead(Long userId, Long notificationId) {
        userNotificationLikeMapper.markAsRead(notificationId, userId);
    }

    @Override
    @Transactional
    public void markAllLikeAsRead(Long userId) {
        userNotificationLikeMapper.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public void markFollowAsRead(Long userId, Long notificationId) {
        userNotificationFollowMapper.markAsRead(notificationId, userId);
    }

    @Override
    @Transactional
    public void markAllFollowAsRead(Long userId) {
        userNotificationFollowMapper.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public void markSystemAsRead(Long userId, Long notificationId) {
        userNotificationSystemMapper.markAsRead(notificationId, userId);
    }

    @Override
    @Transactional
    public void markAllSystemAsRead(Long userId) {
        userNotificationSystemMapper.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        markAllCommentReplyAsRead(userId);
        markAllPostCommentAsRead(userId);
        markAllLikeAsRead(userId);
        markAllFollowAsRead(userId);
        markAllSystemAsRead(userId);
    }

    @Override
    public void sendCommentReplyNotification(Long userId, Long commentId, Long replyId, Long postId, 
            String postTitle, Long commenterId, String commenterName, String commenterAvatar, String content) {
        notifyProducer.sendCommentReplyNotify(userId, commenterId, commenterName, postId, postTitle, commentId);
        log.info("发送评论回复通知: userId={}, commenterId={}", userId, commenterId);
    }

    @Override
    public void sendPostCommentNotification(Long userId, Long postId, String postTitle,
            Long commenterId, String commenterName, String commenterAvatar, String content) {
        notifyProducer.sendPostCommentNotify(userId, commenterId, commenterName, postId, postTitle);
        log.info("发送帖子评论通知: userId={}, postId={}, commenterId={}", userId, postId, commenterId);
    }

    @Override
    public void sendLikeNotification(Long userId, Long postId, String postTitle,
            Long likerId, String likerName, String likerAvatar) {
        notifyProducer.sendPostLikeNotify(userId, likerId, likerName, postId, postTitle);
        log.info("发送点赞通知: userId={}, postId={}, likerId={}", userId, postId, likerId);
    }

    @Override
    public void sendFollowNotification(Long userId, Long followerId, String followerName, String followerAvatar) {
        notifyProducer.sendFollowNotify(userId, followerId, followerName);
        log.info("发送关注通知: userId={}, followerId={}", userId, followerId);
    }

    @Override
    public void sendSystemNotification(Long userId, String title, String content, Integer type) {
        notifyProducer.sendSystemNotify(userId, title, content, type.toString(), 0L);
        log.info("发送系统通知: userId={}, title={}", userId, title);
    }

    @Override
    public void sendBroadcastNotification(String title, String content, Integer type) {
        notifyProducer.sendBroadcastNotify(title, content, type.toString());
        log.info("发送广播通知: title={}", title);
    }
}
