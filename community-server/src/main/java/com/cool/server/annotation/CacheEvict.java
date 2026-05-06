package com.cool.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 缓存清除注解
 * 用于标记需要清除缓存的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheEvict {
    
    /**
     * 要清除的缓存key，支持SpEL表达式
     */
    String key();
    
    /**
     * 是否在方法执行前清除缓存
     * 默认false，方法执行后清除
     */
    boolean beforeInvocation() default false;
}
