package com.cool.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 缓存注解
 * 用于标记需要缓存的方法，通过AOP实现方法级别的缓存
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cacheable {
    
    /**
     * 缓存key前缀
     */
    String prefix() default "cache:";
    
    /**
     * 缓存过期时间（秒）
     * 默认300秒（5分钟）
     */
    long ttl() default 300;
}
