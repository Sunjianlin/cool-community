package com.cool.server.service.notify.impl;

import com.cool.pojo.entity.notify.*;
import com.cool.server.mapper.notify.*;
import com.cool.server.service.notify.InnerMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InnerMessageServiceImpl implements InnerMessageService {

    private final UserNotificationMainMapper userNotificationMainMapper;
    private final UserNotificationCommentMapper userNotificationCommentMapper;
    private final UserNotificationLikeMapper userNotificationLikeMapper;
    private final UserNotificationFollowMapper userNotificationFollowMapper;
    private final UserNotificationSystemMapper userNotificationSystemMapper;

    @Override
    @Transactional
    public void saveCommentNotify(NotifyMessage message) {
        UserNotificationMain existing = userNotificationMainMapper.selectByMessageId(message.getMessageId());
        if (existing != null) {
            log.info("评论通知已存在，跳过: messageId={}", message.getMessageId());
            return;
        }

        UserNotificationMain main = new UserNotificationMain();
        main.setMessageId(message.getMessageId());
        main.setReceiverId(message.getReceiverId());
        main.setSenderId(message.getSenderId());
        main.setNotifyType("COMMENT");
        main.setContent(message.getContent());
        main.setReadStatus(0);
        
        userNotificationMainMapper.insert(main);

        String commentType = message.getBusinessType() != null ? message.getBusinessType() : "POST_COMMENT";
        Long postId;
        Long commentId;
        
        if ("COMMENT_REPLY".equals(commentType)) {
            commentId = message.getBusinessId();
            postId = message.getExtra() != null ? Long.parseLong(message.getExtra()) : message.getBusinessId();
        } else {
            postId = message.getBusinessId();
            commentId = message.getBusinessId();
        }

        UserNotificationComment comment = new UserNotificationComment();
        comment.setNotificationId(main.getId());
        comment.setPostId(postId);
        comment.setCommentId(commentId);
        comment.setCommentType(commentType);
        comment.setJumpUrl("/post/" + postId);
        
        userNotificationCommentMapper.insert(comment);
        
        log.info("保存评论通知成功: receiverId={}, senderId={}, commentType={}", message.getReceiverId(), message.getSenderId(), commentType);
    }

    @Override
    @Transactional
    public void saveLikeNotify(NotifyMessage message) {
        UserNotificationMain existing = userNotificationMainMapper.selectByMessageId(message.getMessageId());
        if (existing != null) {
            log.info("点赞通知已存在，跳过: messageId={}", message.getMessageId());
            return;
        }

        UserNotificationMain main = new UserNotificationMain();
        main.setMessageId(message.getMessageId());
        main.setReceiverId(message.getReceiverId());
        main.setSenderId(message.getSenderId());
        main.setNotifyType("LIKE");
        main.setContent(message.getContent());
        main.setReadStatus(0);
        
        userNotificationMainMapper.insert(main);

        String likeType = message.getBusinessType() != null ? message.getBusinessType() : "POST_LIKE";
        Long targetId = message.getBusinessId();
        String jumpUrl;
        
        if ("COMMENT_LIKE".equals(likeType) && message.getExtra() != null) {
            jumpUrl = "/post/" + message.getExtra();
        } else {
            jumpUrl = "/post/" + targetId;
        }

        UserNotificationLike like = new UserNotificationLike();
        like.setNotificationId(main.getId());
        like.setTargetId(targetId);
        like.setLikeType(likeType);
        like.setJumpUrl(jumpUrl);
        
        userNotificationLikeMapper.insert(like);
        
        log.info("保存点赞通知成功: receiverId={}, senderId={}, likeType={}", message.getReceiverId(), message.getSenderId(), likeType);
    }

    @Override
    @Transactional
    public void saveFollowNotify(NotifyMessage message) {
        UserNotificationMain existing = userNotificationMainMapper.selectByMessageId(message.getMessageId());
        if (existing != null) {
            log.info("关注通知已存在，跳过: messageId={}", message.getMessageId());
            return;
        }

        UserNotificationMain main = new UserNotificationMain();
        main.setMessageId(message.getMessageId());
        main.setReceiverId(message.getReceiverId());
        main.setSenderId(message.getSenderId());
        main.setNotifyType("FOLLOW");
        main.setContent(message.getContent());
        main.setReadStatus(0);
        
        userNotificationMainMapper.insert(main);

        UserNotificationFollow follow = new UserNotificationFollow();
        follow.setNotificationId(main.getId());
        follow.setFollowedUserId(message.getReceiverId());
        follow.setFollowerUserId(message.getSenderId());
        follow.setJumpUrl("/user/" + message.getSenderId());
        
        userNotificationFollowMapper.insert(follow);
        
        log.info("保存关注通知成功: receiverId={}, senderId={}", message.getReceiverId(), message.getSenderId());
    }

    @Override
    @Transactional
    public void savePrivateNotify(NotifyMessage message) {
        UserNotificationMain existing = userNotificationMainMapper.selectByMessageId(message.getMessageId());
        if (existing != null) {
            log.info("私信通知已存在，跳过: messageId={}", message.getMessageId());
            return;
        }

        UserNotificationMain main = new UserNotificationMain();
        main.setMessageId(message.getMessageId());
        main.setReceiverId(message.getReceiverId());
        main.setSenderId(message.getSenderId());
        main.setNotifyType("PRIVATE");
        main.setContent(message.getContent());
        main.setReadStatus(0);
        
        userNotificationMainMapper.insert(main);
        
        log.info("保存私信通知成功: receiverId={}, senderId={}", message.getReceiverId(), message.getSenderId());
    }

    @Override
    @Transactional
    public void saveSystemNotify(NotifyMessage message) {
        UserNotificationMain existing = userNotificationMainMapper.selectByMessageId(message.getMessageId());
        if (existing != null) {
            log.info("系统通知已存在，跳过: messageId={}", message.getMessageId());
            return;
        }

        UserNotificationMain main = new UserNotificationMain();
        main.setMessageId(message.getMessageId());
        main.setReceiverId(message.getReceiverId());
        main.setSenderId(0L);
        main.setNotifyType("SYSTEM");
        main.setContent(message.getContent());
        main.setReadStatus(0);
        
        userNotificationMainMapper.insert(main);

        UserNotificationSystem system = new UserNotificationSystem();
        system.setNotificationId(main.getId());
        system.setSystemMsgType(message.getBusinessType() != null ? message.getBusinessType() : "ANNOUNCEMENT");
        system.setRelatedBusinessId(message.getBusinessId() != null ? message.getBusinessId() : 0L);
        system.setJumpUrl(message.getExtra() != null ? message.getExtra() : "");
        
        userNotificationSystemMapper.insert(system);
        
        log.info("保存系统通知成功: receiverId={}", message.getReceiverId());
    }
}
