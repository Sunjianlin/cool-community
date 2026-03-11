package com.cool.server.service.producer;

import com.cool.common.constant.RabbitMQNotifyConstants;
import com.cool.pojo.entity.notify.NotifyMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotifyProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendCommentNotify(Long receiverId, Long senderId, String content, Long postId, Long commentId, String commentType) {
        NotifyMessage message = NotifyMessage.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .notifyType("COMMENT")
                .content(content)
                .businessId(commentId)
                .businessType(commentType)
                .extra(postId.toString())
                .build();
        
        rabbitTemplate.convertAndSend(
                RabbitMQNotifyConstants.NOTIFY_EXCHANGE,
                RabbitMQNotifyConstants.COMMENT_ROUTING_KEY,
                message
        );
    }

    public void sendPostCommentNotify(Long receiverId, Long senderId, String senderName, Long postId, String postTitle) {
        String content = senderName + " 评论了你的帖子《" + postTitle + "》";
        NotifyMessage message = NotifyMessage.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .notifyType("COMMENT")
                .content(content)
                .businessId(postId)
                .businessType("POST_COMMENT")
                .build();
        
        rabbitTemplate.convertAndSend(
                RabbitMQNotifyConstants.NOTIFY_EXCHANGE,
                RabbitMQNotifyConstants.COMMENT_ROUTING_KEY,
                message
        );
    }

    public void sendCommentReplyNotify(Long receiverId, Long senderId, String senderName, Long postId, String postTitle, Long commentId) {
        String content = senderName + " 回复了你在《" + postTitle + "》中的评论";
        NotifyMessage message = NotifyMessage.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .notifyType("COMMENT")
                .content(content)
                .businessId(commentId)
                .businessType("COMMENT_REPLY")
                .extra(postId.toString())
                .build();
        
        rabbitTemplate.convertAndSend(
                RabbitMQNotifyConstants.NOTIFY_EXCHANGE,
                RabbitMQNotifyConstants.COMMENT_ROUTING_KEY,
                message
        );
    }

    public void sendPostLikeNotify(Long receiverId, Long senderId, String senderName, Long postId, String postTitle) {
        String content = senderName + " 赞了你的帖子《" + postTitle + "》";
        NotifyMessage message = NotifyMessage.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .notifyType("LIKE")
                .content(content)
                .businessId(postId)
                .businessType("POST_LIKE")
                .build();
        
        rabbitTemplate.convertAndSend(
                RabbitMQNotifyConstants.NOTIFY_EXCHANGE,
                RabbitMQNotifyConstants.LIKE_ROUTING_KEY,
                message
        );
    }

    public void sendCommentLikeNotify(Long receiverId, Long senderId, String senderName, Long commentId, Long postId) {
        String content = senderName + " 赞了你的评论";
        NotifyMessage message = NotifyMessage.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .notifyType("LIKE")
                .content(content)
                .businessId(commentId)
                .businessType("COMMENT_LIKE")
                .extra(postId.toString())
                .build();
        
        rabbitTemplate.convertAndSend(
                RabbitMQNotifyConstants.NOTIFY_EXCHANGE,
                RabbitMQNotifyConstants.LIKE_ROUTING_KEY,
                message
        );
    }

    public void sendFollowNotify(Long receiverId, Long senderId, String senderName) {
        String content = senderName + " 关注了你";
        NotifyMessage message = NotifyMessage.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .notifyType("FOLLOW")
                .content(content)
                .businessId(senderId)
                .businessType("USER_FOLLOW")
                .build();
        
        rabbitTemplate.convertAndSend(
                RabbitMQNotifyConstants.NOTIFY_EXCHANGE,
                RabbitMQNotifyConstants.FOLLOW_ROUTING_KEY,
                message
        );
    }

    public void sendPrivateNotify(Long receiverId, Long senderId, String senderName, String content, Long messageId) {
        String notifyContent = senderName + " 给你发送了一条私信";
        NotifyMessage message = NotifyMessage.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .notifyType("PRIVATE")
                .content(notifyContent)
                .businessId(messageId)
                .businessType("PRIVATE_MESSAGE")
                .extra(content)
                .build();
        
        rabbitTemplate.convertAndSend(
                RabbitMQNotifyConstants.NOTIFY_EXCHANGE,
                RabbitMQNotifyConstants.PRIVATE_ROUTING_KEY,
                message
        );
    }

    public void sendSystemNotify(Long receiverId, String title, String content, String systemType, Long relatedBusinessId) {
        NotifyMessage message = NotifyMessage.builder()
                .receiverId(receiverId)
                .senderId(0L)
                .notifyType("SYSTEM")
                .content(title + ": " + content)
                .businessId(relatedBusinessId)
                .businessType(systemType)
                .build();
        
        rabbitTemplate.convertAndSend(
                RabbitMQNotifyConstants.NOTIFY_EXCHANGE,
                RabbitMQNotifyConstants.SYSTEM_ROUTING_KEY,
                message
        );
    }

    public void sendBroadcastNotify(String title, String content, String systemType) {
        NotifyMessage message = NotifyMessage.builder()
                .receiverId(0L)
                .senderId(0L)
                .notifyType("SYSTEM")
                .content(title + ": " + content)
                .businessId(0L)
                .businessType(systemType)
                .build();
        
        rabbitTemplate.convertAndSend(
                RabbitMQNotifyConstants.NOTIFY_EXCHANGE,
                RabbitMQNotifyConstants.SYSTEM_ROUTING_KEY,
                message
        );
    }
}
