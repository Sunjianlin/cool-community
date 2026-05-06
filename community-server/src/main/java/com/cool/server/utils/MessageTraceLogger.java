package com.cool.server.utils;

import com.cool.common.constant.RabbitMQNotifyConstants;
import com.cool.pojo.entity.notify.NotifyMessage;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class MessageTraceLogger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String PREFIX = RabbitMQNotifyConstants.TRACE_LOG_PREFIX;

    public static void logProduced(String exchange, String routingKey, NotifyMessage message) {
        log.info("{} [PRODUCED] {} | Exchange: {} | RoutingKey: {} | MessageId: {} | ReceiverId: {} | SenderId: {} | Type: {}",
                PREFIX,
                LocalDateTime.now().format(FORMATTER),
                exchange,
                routingKey,
                message.getMessageId(),
                message.getReceiverId(),
                message.getSenderId(),
                message.getNotifyType());
    }

    public static void logReceived(String queue, NotifyMessage message, int retryCount) {
        log.info("{} [RECEIVED] {} | Queue: {} | MessageId: {} | ReceiverId: {} | SenderId: {} | Type: {} | RetryCount: {}",
                PREFIX,
                LocalDateTime.now().format(FORMATTER),
                queue,
                message.getMessageId(),
                message.getReceiverId(),
                message.getSenderId(),
                message.getNotifyType(),
                retryCount);
    }

    public static void logProcessing(String queue, NotifyMessage message) {
        log.info("{} [PROCESSING] {} | Queue: {} | MessageId: {} | Content: {}",
                PREFIX,
                LocalDateTime.now().format(FORMATTER),
                queue,
                message.getMessageId(),
                truncateContent(message.getContent()));
    }

    public static void logSuccess(String queue, NotifyMessage message, long processingTimeMs) {
        log.info("{} [SUCCESS] {} | Queue: {} | MessageId: {} | ProcessingTime: {}ms",
                PREFIX,
                LocalDateTime.now().format(FORMATTER),
                queue,
                message.getMessageId(),
                processingTimeMs);
    }

    public static void logRetry(String queue, NotifyMessage message, int retryCount, String reason) {
        log.warn("{} [RETRY] {} | Queue: {} | MessageId: {} | RetryCount: {}/{} | Reason: {}",
                PREFIX,
                LocalDateTime.now().format(FORMATTER),
                queue,
                message.getMessageId(),
                retryCount,
                RabbitMQNotifyConstants.MAX_RETRY_COUNT,
                reason);
    }

    public static void logDlq(String queue, NotifyMessage message, String reason) {
        log.error("{} [DLQ] {} | OriginalQueue: {} | MessageId: {} | ReceiverId: {} | Type: {} | Reason: {}",
                PREFIX,
                LocalDateTime.now().format(FORMATTER),
                queue,
                message.getMessageId(),
                message.getReceiverId(),
                message.getNotifyType(),
                reason);
    }

    public static void logFailed(String queue, NotifyMessage message, Exception e) {
        log.error("{} [FAILED] {} | Queue: {} | MessageId: {} | Error: {}",
                PREFIX,
                LocalDateTime.now().format(FORMATTER),
                queue,
                message.getMessageId(),
                e.getMessage(),
                e);
    }

    private static String truncateContent(String content) {
        if (content == null) {
            return "null";
        }
        return content.length() > 50 ? content.substring(0, 50) + "..." : content;
    }
}
