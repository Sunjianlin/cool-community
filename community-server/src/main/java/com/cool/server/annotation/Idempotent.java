package com.cool.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 幂等性注解
 * 用于标记需要幂等性校验的接口
 * 通过Redis存储请求ID，防止重复提交
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    
    /**
     * 幂等性key的前缀
     */
    String prefix() default "idempotent:";
    
    /**
     * 幂等性有效期（秒）
     * 默认5秒内不允许重复请求
     */
    int expireSeconds() default 5;
    
    /**
     * 是否在请求头中获取幂等性key
     * true: 从请求头 X-Idempotent-Key 获取
     * false: 自动生成（基于用户ID+请求路径+参数）
     */
    boolean headerKey() default false;
    
    /**
     * 提示消息
     */
    String message() default "请勿重复提交";
}
