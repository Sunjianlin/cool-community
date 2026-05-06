package com.cool.server.aspect;

import com.cool.server.annotation.Cacheable;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Aspect
@Component
public class CacheAspect {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    
    private static final int MAX_FAILURE_COUNT = 5;
    private static final long CIRCUIT_BREAKER_RESET_MS = 30000;
    
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private volatile long lastFailureTime = 0;
    private volatile boolean circuitOpen = false;

    @Around("@annotation(com.cool.server.annotation.Cacheable)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Cacheable cacheable = method.getAnnotation(Cacheable.class);

        String cacheKey = generateCacheKey(joinPoint, cacheable);

        if (isCircuitOpen()) {
            log.warn("缓存熔断器开启，跳过缓存操作: key={}", cacheKey);
            return joinPoint.proceed();
        }

        try {
            String cachedValue = stringRedisTemplate.opsForValue().get(cacheKey);
            
            if (cachedValue != null) {
                log.debug("缓存命中: key={}", cacheKey);
                resetFailureCount();
                Class<?> returnType = method.getReturnType();
                return objectMapper.readValue(cachedValue, returnType);
            }

            log.debug("缓存未命中: key={}", cacheKey);
            
            Object result = joinPoint.proceed();
            
            if (result != null) {
                try {
                    String jsonValue = objectMapper.writeValueAsString(result);
                    stringRedisTemplate.opsForValue().set(
                        cacheKey, 
                        jsonValue, 
                        cacheable.ttl(), 
                        TimeUnit.SECONDS
                    );
                    log.debug("缓存已存储: key={}, ttl={}s", cacheKey, cacheable.ttl());
                    resetFailureCount();
                } catch (Exception e) {
                    log.warn("缓存写入失败，降级处理: key={}", cacheKey, e);
                    recordFailure();
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("缓存操作异常，降级处理: key={}", cacheKey, e);
            recordFailure();
            return joinPoint.proceed();
        }
    }

    private String generateCacheKey(ProceedingJoinPoint joinPoint, Cacheable cacheable) {
        StringBuilder keyBuilder = new StringBuilder(cacheable.prefix());
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        keyBuilder.append(signature.getMethod().getName()).append(":");
        
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            for (Object arg : args) {
                if (arg != null) {
                    keyBuilder.append(arg.hashCode()).append("_");
                }
            }
        }
        
        return keyBuilder.toString();
    }
    
    private boolean isCircuitOpen() {
        if (!circuitOpen) {
            return false;
        }
        
        if (System.currentTimeMillis() - lastFailureTime > CIRCUIT_BREAKER_RESET_MS) {
            log.info("缓存熔断器重置");
            circuitOpen = false;
            failureCount.set(0);
            return false;
        }
        
        return true;
    }
    
    private void recordFailure() {
        int count = failureCount.incrementAndGet();
        lastFailureTime = System.currentTimeMillis();
        
        if (count >= MAX_FAILURE_COUNT && !circuitOpen) {
            circuitOpen = true;
            log.warn("缓存熔断器开启，连续失败次数: {}", count);
        }
    }
    
    private void resetFailureCount() {
        if (failureCount.get() > 0) {
            failureCount.set(0);
        }
        if (circuitOpen) {
            circuitOpen = false;
            log.info("缓存熔断器关闭");
        }
    }
}
