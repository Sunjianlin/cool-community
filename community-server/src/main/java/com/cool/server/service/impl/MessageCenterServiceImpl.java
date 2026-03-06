package com.cool.server.service.impl;

import com.cool.pojo.dto.NotificationSummaryDTO;
import com.cool.pojo.entity.*;
import com.cool.pojo.vo.*;
import com.cool.server.mapper.*;
import com.cool.server.service.MessageCenterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.cool.server.config.RabbitMQConfig.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageCenterServiceImpl implements MessageCenterService {

    private final CommentReplyNotificationMapper commentReplyNotificationMapper;
    private final PostCommentNotificationMapper postCommentNotificationMapper;
    private final LikeNotificationMapper likeNotificationMapper;
    private final FollowNotificationMapper followNotificationMapper;
    private final SystemNotificationMapper systemNotificationMapper;
    private final UserNotificationSummaryMapper userNotificationSummaryMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public NotificationSummaryDTO getNotificationSummary(Long userId) {
        UserNotificationSummary summary = userNotificationSummaryMapper.selectByUserId(userId);
        NotificationSummaryDTO dto = new NotificationSummaryDTO();
        
        if (summary == null) {
            userNotificationSummaryMapper.insertOrIgnore(userId);
            dto.setUnreadPrivate(0);
            dto.setUnreadComment(0);
            dto.setUnreadLike(0);
            dto.setUnreadFollow(0);
            dto.setUnreadSystem(0);
            dto.setTotalUnread(0);
        } else {
            int unreadPrivate = getUnreadPrivateCount(userId);
            dto.setUnreadPrivate(unreadPrivate);
            dto.setUnreadComment(summary.getUnreadComment());
            dto.setUnreadLike(summary.getUnreadLike());
            dto.setUnreadFollow(summary.getUnreadFollow());
            dto.setUnreadSystem(summary.getUnreadSystem());
            dto.setTotalUnread(unreadPrivate + summary.getUnreadComment() + summary.getUnreadLike() + 
                    summary.getUnreadFollow() + summary.getUnreadSystem());
        }
        
        return dto;
    }

    private int getUnreadPrivateCount(Long userId) {
        List<ChatSession> sessions = chatSessionMapper.selectByUserId(userId);
        
        int totalUnread = 0;
        for (ChatSession session : sessions) {
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
        List<CommentReplyNotification> list = commentReplyNotificationMapper.selectByUserIdWithPaging(userId, offset, pageSize);
        
        return list.stream()
                .map(this::convertToCommentReplyVO)
                .collect(Collectors.toList());
    }

    private CommentReplyNotificationVO convertToCommentReplyVO(CommentReplyNotification entity) {
        CommentReplyNotificationVO vo = new CommentReplyNotificationVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public List<PostCommentNotificationVO> getPostCommentNotifications(Long userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<PostCommentNotification> list = postCommentNotificationMapper.selectByUserIdWithPaging(userId, offset, pageSize);
        
        return list.stream()
                .map(this::convertToPostCommentVO)
                .collect(Collectors.toList());
    }

    private PostCommentNotificationVO convertToPostCommentVO(PostCommentNotification entity) {
        PostCommentNotificationVO vo = new PostCommentNotificationVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public List<LikeNotificationVO> getLikeNotifications(Long userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<LikeNotification> list = likeNotificationMapper.selectByUserIdWithPaging(userId, offset, pageSize);
        
        return list.stream()
                .map(this::convertToLikeVO)
                .collect(Collectors.toList());
    }

    private LikeNotificationVO convertToLikeVO(LikeNotification entity) {
        LikeNotificationVO vo = new LikeNotificationVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public List<FollowNotificationVO> getFollowNotifications(Long userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<FollowNotification> list = followNotificationMapper.selectByUserIdWithPaging(userId, offset, pageSize);
        
        return list.stream()
                .map(this::convertToFollowVO)
                .collect(Collectors.toList());
    }

    private FollowNotificationVO convertToFollowVO(FollowNotification entity) {
        FollowNotificationVO vo = new FollowNotificationVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public List<SystemNotificationVO> getSystemNotifications(Long userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<SystemNotification> list = systemNotificationMapper.selectByUserIdWithPaging(userId, offset, pageSize);
        
        return list.stream()
                .map(this::convertToSystemVO)
                .collect(Collectors.toList());
    }

    private SystemNotificationVO convertToSystemVO(SystemNotification entity) {
        SystemNotificationVO vo = new SystemNotificationVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional
    public void markCommentReplyAsRead(Long userId, Long notificationId) {
        commentReplyNotificationMapper.markAsRead(notificationId, userId);
        updateSummary(userId);
    }

    @Override
    @Transactional
    public void markAllCommentReplyAsRead(Long userId) {
        commentReplyNotificationMapper.markAllAsRead(userId);
        updateSummary(userId);
    }

    @Override
    @Transactional
    public void markPostCommentAsRead(Long userId, Long notificationId) {
        postCommentNotificationMapper.markAsRead(notificationId, userId);
        updateSummary(userId);
    }

    @Override
    @Transactional
    public void markAllPostCommentAsRead(Long userId) {
        postCommentNotificationMapper.markAllAsRead(userId);
        updateSummary(userId);
    }

    @Override
    @Transactional
    public void markLikeAsRead(Long userId, Long notificationId) {
        likeNotificationMapper.markAsRead(notificationId, userId);
        updateSummary(userId);
    }

    @Override
    @Transactional
    public void markAllLikeAsRead(Long userId) {
        likeNotificationMapper.markAllAsRead(userId);
        updateSummary(userId);
    }

    @Override
    @Transactional
    public void markFollowAsRead(Long userId, Long notificationId) {
        followNotificationMapper.markAsRead(notificationId, userId);
        updateSummary(userId);
    }

    @Override
    @Transactional
    public void markAllFollowAsRead(Long userId) {
        followNotificationMapper.markAllAsRead(userId);
        updateSummary(userId);
    }

    @Override
    @Transactional
    public void markSystemAsRead(Long userId, Long notificationId) {
        systemNotificationMapper.markAsRead(notificationId, userId);
        updateSummary(userId);
    }

    @Override
    @Transactional
    public void markAllSystemAsRead(Long userId) {
        systemNotificationMapper.markAllAsRead(userId);
        updateSummary(userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        markAllCommentReplyAsRead(userId);
        markAllPostCommentAsRead(userId);
        markAllLikeAsRead(userId);
        markAllFollowAsRead(userId);
        markAllSystemAsRead(userId);
        updateSummary(userId);
    }

    private void updateSummary(Long userId) {
        long unreadCommentReply = commentReplyNotificationMapper.countUnreadByUserId(userId);
        long unreadPostComment = postCommentNotificationMapper.countUnreadByUserId(userId);
        long unreadLike = likeNotificationMapper.countUnreadByUserId(userId);
        long unreadFollow = followNotificationMapper.countUnreadByUserId(userId);
        long unreadSystem = systemNotificationMapper.countUnreadByUserId(userId);
        int unreadPrivate = getUnreadPrivateCount(userId);

        UserNotificationSummary summary = new UserNotificationSummary();
        summary.setUserId(userId);
        summary.setUnreadPrivate(unreadPrivate);
        summary.setUnreadComment((int) (unreadCommentReply + unreadPostComment));
        summary.setUnreadLike((int) unreadLike);
        summary.setUnreadFollow((int) unreadFollow);
        summary.setUnreadSystem((int) unreadSystem);
        summary.setTotalUnread(unreadPrivate + (int) (unreadCommentReply + unreadPostComment) + 
                (int) unreadLike + (int) unreadFollow + (int) unreadSystem);

        userNotificationSummaryMapper.updateByUserId(summary);
    }

    @Override
    @Transactional
    public void sendCommentReplyNotification(Long userId, Long commentId, Long replyId, Long postId,
            String postTitle, Long commenterId, String commenterName, String commenterAvatar, String content) {
        CommentReplyNotification notification = new CommentReplyNotification();
        notification.setUserId(userId);
        notification.setCommentId(commentId);
        notification.setReplyId(replyId);
        notification.setPostId(postId);
        notification.setPostTitle(postTitle);
        notification.setCommenterId(commenterId);
        notification.setCommenterName(commenterName);
        notification.setCommenterAvatar(commenterAvatar);
        notification.setContent(content);
        notification.setIsRead(0);
        
        commentReplyNotificationMapper.insert(notification);
        userNotificationSummaryMapper.incrementComment(userId);
        
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, COMMENT_REPLY_ROUTING_KEY, notification);
        log.info("发送评论回复通知: userId={}, commenterId={}", userId, commenterId);
    }

    @Override
    @Transactional
    public void sendPostCommentNotification(Long userId, Long postId, String postTitle,
            Long commenterId, String commenterName, String commenterAvatar, String content) {
        PostCommentNotification notification = new PostCommentNotification();
        notification.setUserId(userId);
        notification.setPostId(postId);
        notification.setPostTitle(postTitle);
        notification.setCommenterId(commenterId);
        notification.setCommenterName(commenterName);
        notification.setCommenterAvatar(commenterAvatar);
        notification.setContent(content);
        notification.setIsRead(0);
        
        postCommentNotificationMapper.insert(notification);
        userNotificationSummaryMapper.incrementComment(userId);
        
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, POST_COMMENT_ROUTING_KEY, notification);
        log.info("发送帖子评论通知: userId={}, postId={}, commenterId={}", userId, postId, commenterId);
    }

    @Override
    @Transactional
    public void sendLikeNotification(Long userId, Long postId, String postTitle,
            Long likerId, String likerName, String likerAvatar) {
        LikeNotification notification = new LikeNotification();
        notification.setUserId(userId);
        notification.setPostId(postId);
        notification.setPostTitle(postTitle);
        notification.setLikerId(likerId);
        notification.setLikerName(likerName);
        notification.setLikerAvatar(likerAvatar);
        notification.setIsRead(0);
        
        likeNotificationMapper.insert(notification);
        userNotificationSummaryMapper.incrementLike(userId);
        
        log.info("发送点赞通知: userId={}, postId={}, likerId={}", userId, postId, likerId);
    }

    @Override
    @Transactional
    public void sendFollowNotification(Long userId, Long followerId, String followerName, String followerAvatar) {
        FollowNotification notification = new FollowNotification();
        notification.setUserId(userId);
        notification.setFollowerId(followerId);
        notification.setFollowerName(followerName);
        notification.setFollowerAvatar(followerAvatar);
        notification.setIsRead(0);
        
        followNotificationMapper.insert(notification);
        userNotificationSummaryMapper.incrementFollow(userId);
        
        log.info("发送关注通知: userId={}, followerId={}", userId, followerId);
    }

    @Override
    @Transactional
    public void sendSystemNotification(Long userId, String title, String content, Integer type) {
        SystemNotification notification = new SystemNotification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(0);
        
        systemNotificationMapper.insert(notification);
        userNotificationSummaryMapper.incrementSystem(userId);
        
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, SYSTEM_NOTIFICATION_ROUTING_KEY, notification);
        log.info("发送系统通知: userId={}, title={}", userId, title);
    }

    @Override
    @Transactional
    public void sendBroadcastNotification(String title, String content, Integer type) {
        SystemNotification notification = new SystemNotification();
        notification.setUserId(0L);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(0);
        
        systemNotificationMapper.insert(notification);
        
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, SYSTEM_NOTIFICATION_ROUTING_KEY, notification);
        log.info("发送广播通知: title={}", title);
    }
}
