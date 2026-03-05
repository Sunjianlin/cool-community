package com.cool.common.constant;

public class RedisConstant {
    public static final String USER_TOKEN_KEY = "user:token:";
    public static final String USER_INFO_KEY = "user:info:";
    public static final String USER_LOGIN_COUNT_KEY = "user:login:count:";
    public static final String POST_LIKE_KEY = "post:like:";
    public static final String POST_COLLECT_KEY = "post:collect:";
    public static final String POST_VIEW_KEY = "post:view:";
    public static final String TOPIC_FOLLOW_KEY = "topic:follow:";
    public static final String USER_FOLLOW_KEY = "user:follow:";
    public static final String USER_FANS_KEY = "user:fans:";
    public static final String HOT_POST_KEY = "post:hot";
    public static final String VERIFY_CODE_KEY = "verify:code:";
    
    // 关注相关常量
    public static final String FOLLOW_KEY = "follow:";
    public static final String FOLLOW_COUNT_KEY = "follow:count:";
    public static final String FOLLOWER_COUNT_KEY = "follower:count:";
    
    public static final long TOKEN_EXPIRE_TIME = 3 * 60 * 60;
    public static final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60;
    public static final long VERIFY_CODE_EXPIRE_TIME = 5 * 60;

    // Redis键前缀
    public static final String STOCK_KEY = "seckill:stock:";
    public static final String USER_KEY = "seckill:user:";
    public static final String ACTIVITY_KEY = "seckill:activity:";
    public static final String LOCK_KEY = "seckill:lock:";
}
