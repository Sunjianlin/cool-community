package com.cool.common.constant;

public class RabbitMQNotifyConstants {

    // 1. 交换机名称（直连交换机）
    public static final String NOTIFY_EXCHANGE = "community.notify.direct.exchange";

    // 2. 队列名称
    public static final String COMMENT_QUEUE = "community.notify.comment.queue";    // 评论通知队列
    public static final String LIKE_QUEUE = "community.notify.like.queue";          // 点赞通知队列
    public static final String FOLLOW_QUEUE = "community.notify.follow.queue";       // 关注通知队列
    public static final String PRIVATE_QUEUE = "community.notify.private.queue";     // 私信通知队列
    public static final String SYSTEM_QUEUE = "community.notify.system.queue";       // 系统通知队列

    // 3. 路由键（与队列一一对应）
    public static final String COMMENT_ROUTING_KEY = "community.notify.comment";
    public static final String LIKE_ROUTING_KEY = "community.notify.like";
    public static final String FOLLOW_ROUTING_KEY = "community.notify.follow";
    public static final String PRIVATE_ROUTING_KEY = "community.notify.private";
    public static final String SYSTEM_ROUTING_KEY = "community.notify.system";
}
