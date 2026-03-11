package com.cool.server.service.consumer;

import com.cool.common.constant.RabbitMQNotifyConstants;
import com.cool.pojo.entity.notify.NotifyMessage;
import com.cool.server.service.notify.InnerMessageService;
import com.cool.server.service.notify.PushService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyConsumer {

    private final InnerMessageService innerMessageService;
    private final PushService pushService;

    @RabbitListener(queues = RabbitMQNotifyConstants.COMMENT_QUEUE)
    public void handleCommentNotify(NotifyMessage notifyMessage, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("收到评论通知: receiverId={}, senderId={}, content={}", 
                    notifyMessage.getReceiverId(), notifyMessage.getSenderId(), notifyMessage.getContent());
            
            innerMessageService.saveCommentNotify(notifyMessage);
            
            pushService.pushWebSocketMessage(notifyMessage.getReceiverId(), notifyMessage);
            
            channel.basicAck(deliveryTag, false);
            log.info("评论通知处理完成: messageId={}", notifyMessage.getMessageId());
        } catch (Exception e) {
            log.error("评论通知处理失败: messageId={}", notifyMessage.getMessageId(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    @RabbitListener(queues = RabbitMQNotifyConstants.LIKE_QUEUE)
    public void handleLikeNotify(NotifyMessage notifyMessage, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("收到点赞通知: receiverId={}, senderId={}, content={}", 
                    notifyMessage.getReceiverId(), notifyMessage.getSenderId(), notifyMessage.getContent());
            
            innerMessageService.saveLikeNotify(notifyMessage);
            
            pushService.pushWebSocketMessage(notifyMessage.getReceiverId(), notifyMessage);
            
            channel.basicAck(deliveryTag, false);
            log.info("点赞通知处理完成: messageId={}", notifyMessage.getMessageId());
        } catch (Exception e) {
            log.error("点赞通知处理失败: messageId={}", notifyMessage.getMessageId(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    @RabbitListener(queues = RabbitMQNotifyConstants.FOLLOW_QUEUE)
    public void handleFollowNotify(NotifyMessage notifyMessage, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("收到关注通知: receiverId={}, senderId={}, content={}", 
                    notifyMessage.getReceiverId(), notifyMessage.getSenderId(), notifyMessage.getContent());
            
            innerMessageService.saveFollowNotify(notifyMessage);
            
            pushService.pushWebSocketMessage(notifyMessage.getReceiverId(), notifyMessage);
            
            channel.basicAck(deliveryTag, false);
            log.info("关注通知处理完成: messageId={}", notifyMessage.getMessageId());
        } catch (Exception e) {
            log.error("关注通知处理失败: messageId={}", notifyMessage.getMessageId(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    @RabbitListener(queues = RabbitMQNotifyConstants.PRIVATE_QUEUE)
    public void handlePrivateNotify(NotifyMessage notifyMessage, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("收到私信通知: receiverId={}, senderId={}, content={}", 
                    notifyMessage.getReceiverId(), notifyMessage.getSenderId(), notifyMessage.getContent());
            
            innerMessageService.savePrivateNotify(notifyMessage);
            
            pushService.pushWebSocketMessage(notifyMessage.getReceiverId(), notifyMessage);
            
            channel.basicAck(deliveryTag, false);
            log.info("私信通知处理完成: messageId={}", notifyMessage.getMessageId());
        } catch (Exception e) {
            log.error("私信通知处理失败: messageId={}", notifyMessage.getMessageId(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    @RabbitListener(queues = RabbitMQNotifyConstants.SYSTEM_QUEUE)
    public void handleSystemNotify(NotifyMessage notifyMessage, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("收到系统通知: receiverId={}, content={}", 
                    notifyMessage.getReceiverId(), notifyMessage.getContent());
            
            innerMessageService.saveSystemNotify(notifyMessage);
            
            pushService.pushWebSocketMessage(notifyMessage.getReceiverId(), notifyMessage);
            
            channel.basicAck(deliveryTag, false);
            log.info("系统通知处理完成: messageId={}", notifyMessage.getMessageId());
        } catch (Exception e) {
            log.error("系统通知处理失败: messageId={}", notifyMessage.getMessageId(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
