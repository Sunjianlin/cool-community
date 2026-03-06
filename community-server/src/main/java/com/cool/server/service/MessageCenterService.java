package com.cool.server.service;

import java.util.List;

import com.cool.pojo.vo.*;
import com.cool.pojo.dto.NotificationSummaryDTO;

public interface MessageCenterService {

    NotificationSummaryDTO getNotificationSummary(Long userId);

    List<CommentReplyNotificationVO> getCommentReplyNotifications(Long userId, Integer page, Integer pageSize);

    List<PostCommentNotificationVO> getPostCommentNotifications(Long userId, Integer page, Integer pageSize);

    List<LikeNotificationVO> getLikeNotifications(Long userId, Integer page, Integer pageSize);

    List<FollowNotificationVO> getFollowNotifications(Long userId, Integer page, Integer pageSize);

    List<SystemNotificationVO> getSystemNotifications(Long userId, Integer page, Integer pageSize);

    void markCommentReplyAsRead(Long userId, Long notificationId);

    void markAllCommentReplyAsRead(Long userId);

    void markPostCommentAsRead(Long userId, Long notificationId);

    void markAllPostCommentAsRead(Long userId);

    void markLikeAsRead(Long userId, Long notificationId);

    void markAllLikeAsRead(Long userId);

    void markFollowAsRead(Long userId, Long notificationId);

    void markAllFollowAsRead(Long userId);

    void markSystemAsRead(Long userId, Long notificationId);

    void markAllSystemAsRead(Long userId);

    void markAllAsRead(Long userId);

    void sendCommentReplyNotification(Long userId, Long commentId, Long replyId, Long postId, 
            String postTitle, Long commenterId, String commenterName, String commenterAvatar, String content);

    void sendPostCommentNotification(Long userId, Long postId, String postTitle,
            Long commenterId, String commenterName, String commenterAvatar, String content);

    void sendLikeNotification(Long userId, Long postId, String postTitle,
            Long likerId, String likerName, String likerAvatar);

    void sendFollowNotification(Long userId, Long followerId, String followerName, String followerAvatar);

    void sendSystemNotification(Long userId, String title, String content, Integer type);

    void sendBroadcastNotification(String title, String content, Integer type);
}
