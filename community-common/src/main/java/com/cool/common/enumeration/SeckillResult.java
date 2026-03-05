package com.cool.common.enumeration;

public enum SeckillResult {
    SUCCESS(200, "秒杀成功"),
    ACTIVITY_NOT_FOUND(404, "活动不存在"),
    ACTIVITY_NOT_STARTED(400, "活动未开始"),
    ACTIVITY_ENDED(400, "秒杀已结束"),
    ACTIVITY_OFFLINE(400, "秒杀已下架"),
    STOCK_INSUFFICIENT(400, "库存不足"),
    ALREADY_PARTICIPATED(400, "一人仅限一单"),
    SYSTEM_ERROR(500, "系统错误"),
    ACTIVITY_STATUS_ERROR(400, "活动状态异常" );

    private final int code;
    private final String message;

    SeckillResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public boolean isSuccess() { return this == SUCCESS; }
}