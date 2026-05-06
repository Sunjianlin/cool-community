package com.cool.common.constant;

public class RabbitMQNotifyConstants {

    // 1. 交换机名称（直连交换机）
    public static final String NOTIFY_EXCHANGE = "community.notify.direct.exchange";

    // 2. 死信交换机名称
    public static final String DEAD_LETTER_EXCHANGE = "community.notify.dlx.exchange";

    // 3. 队列名称
    public static final String COMMENT_QUEUE = "community.notify.comment.queue";
    public static final String LIKE_QUEUE = "community.notify.like.queue";
    public static final String FOLLOW_QUEUE = "community.notify.follow.queue";
    public static final String PRIVATE_QUEUE = "community.notify.private.queue";
    public static final String SYSTEM_QUEUE = "community.notify.system.queue";

    // 4. 死信队列名称
    public static final String COMMENT_DLQ = "community.notify.comment.dlq";
    public static final String LIKE_DLQ = "community.notify.like.dlq";
    public static final String FOLLOW_DLQ = "community.notify.follow.dlq";
    public static final String PRIVATE_DLQ = "community.notify.private.dlq";
    public static final String SYSTEM_DLQ = "community.notify.system.dlq";

    // 5. 路由键（与队列一一对应）
    public static final String COMMENT_ROUTING_KEY = "community.notify.comment";
    public static final String LIKE_ROUTING_KEY = "community.notify.like";
    public static final String FOLLOW_ROUTING_KEY = "community.notify.follow";
    public static final String PRIVATE_ROUTING_KEY = "community.notify.private";
    public static final String SYSTEM_ROUTING_KEY = "community.notify.system";

    // 6. 死信路由键
    public static final String COMMENT_DLQ_ROUTING_KEY = "community.notify.comment.dlq";
    public static final String LIKE_DLQ_ROUTING_KEY = "community.notify.like.dlq";
    public static final String FOLLOW_DLQ_ROUTING_KEY = "community.notify.follow.dlq";
    public static final String PRIVATE_DLQ_ROUTING_KEY = "community.notify.private.dlq";
    public static final String SYSTEM_DLQ_ROUTING_KEY = "community.notify.system.dlq";

    // 7. 消息追踪日志前缀
    public static final String TRACE_LOG_PREFIX = "[MQ-TRACE]";

    // 8. 重试相关常量
    public static final int MAX_RETRY_COUNT = 3;
    public static final String RETRY_COUNT_HEADER = "x-retry-count";
}
