package com.cool.common.exception;

public class BusinessException extends RuntimeException {
    private Integer code; // 自定义业务码（可选，也可固定400）

    // 构造器1：仅消息（默认400）
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    // 构造器2：自定义码+消息
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    // getter
    public Integer getCode() {
        return code;
    }
}
