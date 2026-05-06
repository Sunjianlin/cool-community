package com.cool.server.aspect;

import com.cool.server.annotation.Idempotent;
import com.cool.server.context.BaseContext;
import com.cool.common.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
public class IdempotentAspect {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Around("@annotation(com.cool.server.annotation.Idempotent)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Idempotent idempotent = method.getAnnotation(Idempotent.class);

        String key = generateKey(joinPoint, idempotent);
        
        boolean acquired = tryAcquire(key, idempotent.expireSeconds());
        
        if (!acquired) {
            log.warn("幂等性校验失败，重复请求: key={}, method={}", key, method.getName());
            throw new BusinessException(idempotent.message());
        }

        log.debug("幂等性校验通过: key={}", key);
        
        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (BusinessException e) {
            stringRedisTemplate.delete(key);
            throw e;
        } catch (Exception e) {
            stringRedisTemplate.delete(key);
            log.error("幂等性方法执行异常: key={}", key, e);
            throw e;
        }
    }

    private String generateKey(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        StringBuilder keyBuilder = new StringBuilder(idempotent.prefix());

        if (idempotent.headerKey()) {
            String headerKey = getHeaderKey();
            if (headerKey != null && !headerKey.isEmpty()) {
                return keyBuilder.append(headerKey).toString();
            }
        }

        Long userId = BaseContext.getCurrentId();
        keyBuilder.append(userId != null ? userId : "anonymous").append(":");

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        keyBuilder.append(methodName).append(":");

        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            keyBuilder.append(buildArgsHash(args));
        }

        return keyBuilder.toString();
    }
    
    private String getHeaderKey() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader("X-Idempotent-Key");
        }
        return null;
    }
    
    private String buildArgsHash(Object[] args) {
        StringBuilder argsBuilder = new StringBuilder();
        try {
            for (Object arg : args) {
                if (arg instanceof MultipartFile) {
                    MultipartFile file = (MultipartFile) arg;
                    argsBuilder.append("file:").append(file.getOriginalFilename()).append(":");
                } else if (arg != null) {
                    argsBuilder.append(objectMapper.writeValueAsString(arg)).append("&");
                }
            }
        } catch (Exception e) {
            log.warn("构建参数哈希失败", e);
            argsBuilder.append("error");
        }
        return String.valueOf(argsBuilder.toString().hashCode());
    }

    private boolean tryAcquire(String key, int expireSeconds) {
        try {
            Boolean success = stringRedisTemplate.opsForValue()
                    .setIfAbsent(key, "1", expireSeconds, TimeUnit.SECONDS);
            return Boolean.TRUE.equals(success);
        } catch (Exception e) {
            log.error("Redis操作异常，跳过幂等校验: key={}", key, e);
            return true;
        }
    }
}
