package com.cool.server.service.consumer;

import com.cool.common.constant.RabbitMQNotifyConstants;
import com.cool.pojo.entity.notify.NotifyMessage;
import com.cool.server.service.notify.InnerMessageService;
import com.cool.server.service.notify.PushService;
import com.cool.server.utils.MessageTraceLogger;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyConsumer {

    private final InnerMessageService innerMessageService;
    private final PushService pushService;

    @RabbitListener(queues = RabbitMQNotifyConstants.COMMENT_QUEUE)
    public void handleCommentNotify(NotifyMessage notifyMessage, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        int retryCount = getRetryCount(message);
        
        MessageTraceLogger.logReceived(RabbitMQNotifyConstants.COMMENT_QUEUE, notifyMessage, retryCount);
        long startTime = System.currentTimeMillis();
        
        try {
            MessageTraceLogger.logProcessing(RabbitMQNotifyConstants.COMMENT_QUEUE, notifyMessage);
            
            innerMessageService.saveCommentNotify(notifyMessage);
            pushService.pushWebSocketMessage(notifyMessage.getReceiverId(), notifyMessage);
            
            channel.basicAck(deliveryTag, false);
            MessageTraceLogger.logSuccess(RabbitMQNotifyConstants.COMMENT_QUEUE, notifyMessage, 
                    System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            handleException(RabbitMQNotifyConstants.COMMENT_QUEUE, notifyMessage, channel, message, 
                    deliveryTag, retryCount, e);
        }
    }

    @RabbitListener(queues = RabbitMQNotifyConstants.LIKE_QUEUE)
    public void handleLikeNotify(NotifyMessage notifyMessage, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        int retryCount = getRetryCount(message);
        
        MessageTraceLogger.logReceived(RabbitMQNotifyConstants.LIKE_QUEUE, notifyMessage, retryCount);
        long startTime = System.currentTimeMillis();
        
        try {
            MessageTraceLogger.logProcessing(RabbitMQNotifyConstants.LIKE_QUEUE, notifyMessage);
            
            innerMessageService.saveLikeNotify(notifyMessage);
            pushService.pushWebSocketMessage(notifyMessage.getReceiverId(), notifyMessage);
            
            channel.basicAck(deliveryTag, false);
            MessageTraceLogger.logSuccess(RabbitMQNotifyConstants.LIKE_QUEUE, notifyMessage, 
                    System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            handleException(RabbitMQNotifyConstants.LIKE_QUEUE, notifyMessage, channel, message, 
                    deliveryTag, retryCount, e);
        }
    }

    @RabbitListener(queues = RabbitMQNotifyConstants.FOLLOW_QUEUE)
    public void handleFollowNotify(NotifyMessage notifyMessage, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        int retryCount = getRetryCount(message);
        
        MessageTraceLogger.logReceived(RabbitMQNotifyConstants.FOLLOW_QUEUE, notifyMessage, retryCount);
        long startTime = System.currentTimeMillis();
        
        try {
            MessageTraceLogger.logProcessing(RabbitMQNotifyConstants.FOLLOW_QUEUE, notifyMessage);
            
            innerMessageService.saveFollowNotify(notifyMessage);
            pushService.pushWebSocketMessage(notifyMessage.getReceiverId(), notifyMessage);
            
            channel.basicAck(deliveryTag, false);
            MessageTraceLogger.logSuccess(RabbitMQNotifyConstants.FOLLOW_QUEUE, notifyMessage, 
                    System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            handleException(RabbitMQNotifyConstants.FOLLOW_QUEUE, notifyMessage, channel, message, 
                    deliveryTag, retryCount, e);
        }
    }

    @RabbitListener(queues = RabbitMQNotifyConstants.PRIVATE_QUEUE)
    public void handlePrivateNotify(NotifyMessage notifyMessage, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        int retryCount = getRetryCount(message);
        
        MessageTraceLogger.logReceived(RabbitMQNotifyConstants.PRIVATE_QUEUE, notifyMessage, retryCount);
        long startTime = System.currentTimeMillis();
        
        try {
            MessageTraceLogger.logProcessing(RabbitMQNotifyConstants.PRIVATE_QUEUE, notifyMessage);
            
            innerMessageService.savePrivateNotify(notifyMessage);
            pushService.pushWebSocketMessage(notifyMessage.getReceiverId(), notifyMessage);
            
            channel.basicAck(deliveryTag, false);
            MessageTraceLogger.logSuccess(RabbitMQNotifyConstants.PRIVATE_QUEUE, notifyMessage, 
                    System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            handleException(RabbitMQNotifyConstants.PRIVATE_QUEUE, notifyMessage, channel, message, 
                    deliveryTag, retryCount, e);
        }
    }

    @RabbitListener(queues = RabbitMQNotifyConstants.SYSTEM_QUEUE)
    public void handleSystemNotify(NotifyMessage notifyMessage, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        int retryCount = getRetryCount(message);
        
        MessageTraceLogger.logReceived(RabbitMQNotifyConstants.SYSTEM_QUEUE, notifyMessage, retryCount);
        long startTime = System.currentTimeMillis();
        
        try {
            MessageTraceLogger.logProcessing(RabbitMQNotifyConstants.SYSTEM_QUEUE, notifyMessage);
            
            innerMessageService.saveSystemNotify(notifyMessage);
            pushService.pushWebSocketMessage(notifyMessage.getReceiverId(), notifyMessage);
            
            channel.basicAck(deliveryTag, false);
            MessageTraceLogger.logSuccess(RabbitMQNotifyConstants.SYSTEM_QUEUE, notifyMessage, 
                    System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            handleException(RabbitMQNotifyConstants.SYSTEM_QUEUE, notifyMessage, channel, message, 
                    deliveryTag, retryCount, e);
        }
    }

    @RabbitListener(queues = RabbitMQNotifyConstants.COMMENT_DLQ)
    public void handleCommentDlq(NotifyMessage notifyMessage, Channel channel, Message message) throws IOException {
        handleDeadLetterQueue(RabbitMQNotifyConstants.COMMENT_QUEUE, notifyMessage, channel, message);
    }

    @RabbitListener(queues = RabbitMQNotifyConstants.LIKE_DLQ)
    public void handleLikeDlq(NotifyMessage notifyMessage, Channel channel, Message message) throws IOException {
        handleDeadLetterQueue(RabbitMQNotifyConstants.LIKE_QUEUE, notifyMessage, channel, message);
    }

    @RabbitListener(queues = RabbitMQNotifyConstants.FOLLOW_DLQ)
    public void handleFollowDlq(NotifyMessage notifyMessage, Channel channel, Message message) throws IOException {
        handleDeadLetterQueue(RabbitMQNotifyConstants.FOLLOW_QUEUE, notifyMessage, channel, message);
    }

    @RabbitListener(queues = RabbitMQNotifyConstants.PRIVATE_DLQ)
    public void handlePrivateDlq(NotifyMessage notifyMessage, Channel channel, Message message) throws IOException {
        handleDeadLetterQueue(RabbitMQNotifyConstants.PRIVATE_QUEUE, notifyMessage, channel, message);
    }

    @RabbitListener(queues = RabbitMQNotifyConstants.SYSTEM_DLQ)
    public void handleSystemDlq(NotifyMessage notifyMessage, Channel channel, Message message) throws IOException {
        handleDeadLetterQueue(RabbitMQNotifyConstants.SYSTEM_QUEUE, notifyMessage, channel, message);
    }

    private int getRetryCount(Message message) {
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        if (headers != null && headers.containsKey(RabbitMQNotifyConstants.RETRY_COUNT_HEADER)) {
            Object retryCount = headers.get(RabbitMQNotifyConstants.RETRY_COUNT_HEADER);
            if (retryCount instanceof Integer) {
                return (Integer) retryCount;
            }
        }
        return 0;
    }

    private void handleException(String queue, NotifyMessage notifyMessage, Channel channel, 
            Message message, long deliveryTag, int retryCount, Exception e) throws IOException {
        
        MessageTraceLogger.logFailed(queue, notifyMessage, e);
        
        if (retryCount < RabbitMQNotifyConstants.MAX_RETRY_COUNT) {
            MessageTraceLogger.logRetry(queue, notifyMessage, retryCount + 1, e.getMessage());
            channel.basicNack(deliveryTag, false, true);
        } else {
            MessageTraceLogger.logDlq(queue, notifyMessage, 
                    "Max retry count exceeded: " + RabbitMQNotifyConstants.MAX_RETRY_COUNT);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void handleDeadLetterQueue(String originalQueue, NotifyMessage notifyMessage, 
            Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        log.warn("{} [DLQ-HANDLER] Processing dead letter message from queue: {}, messageId: {}", 
                RabbitMQNotifyConstants.TRACE_LOG_PREFIX, originalQueue, notifyMessage.getMessageId());
        
        try {
            innerMessageService.saveFailedNotify(notifyMessage, originalQueue);
            
            channel.basicAck(deliveryTag, false);
            log.info("{} [DLQ-HANDLER] Dead letter message saved for manual processing: messageId: {}", 
                    RabbitMQNotifyConstants.TRACE_LOG_PREFIX, notifyMessage.getMessageId());
        } catch (Exception e) {
            log.error("{} [DLQ-HANDLER] Failed to process dead letter message: messageId: {}", 
                    RabbitMQNotifyConstants.TRACE_LOG_PREFIX, notifyMessage.getMessageId(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
