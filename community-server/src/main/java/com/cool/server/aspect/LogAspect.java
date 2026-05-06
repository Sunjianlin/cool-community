package com.cool.server.aspect;

import com.cool.server.context.BaseContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class LogAspect {
    
    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final int MAX_PARAM_LENGTH = 500;
    private static final int MAX_RESPONSE_LENGTH = 1000;

    @Pointcut("execution(* com.cool.server.controller..*.*(..))")
    public void controllerPointcut() {
    }

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        String requestId = generateRequestId();
        String methodName = getMethodName(joinPoint);
        String requestPath = "";
        String httpMethod = "";
        String clientIp = "";
        Long userId = null;
        
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            requestPath = request.getRequestURL().toString();
            httpMethod = request.getMethod();
            clientIp = getClientIp(request);
            userId = BaseContext.getCurrentId();
        }
        
        String params = getParams(joinPoint);
        
        log.info("[{}] 请求开始 - 路径: {}, 方法: {}, 用户ID: {}, IP: {}, 参数: {}", 
                requestId, requestPath, methodName, userId, clientIp, truncate(params, MAX_PARAM_LENGTH));
        
        Object result = null;
        boolean success = true;
        String errorMsg = null;
        
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            success = false;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            
            if (success) {
                String responseStr = result != null ? truncate(objectMapper.writeValueAsString(result), MAX_RESPONSE_LENGTH) : "null";
                log.info("[{}] 请求成功 - 耗时: {}ms, 响应: {}", requestId, costTime, responseStr);
            } else {
                log.error("[{}] 请求失败 - 耗时: {}ms, 错误: {}", requestId, costTime, errorMsg);
            }
            
            if (costTime > 3000) {
                log.warn("[{}] 慢请求警告 - 耗时: {}ms, 路径: {}", requestId, costTime, requestPath);
            }
        }
    }
    
    private String generateRequestId() {
        return String.valueOf(System.nanoTime() % 1000000);
    }
    
    private String getMethodName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }
    
    private String getParams(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            
            if (paramNames == null || args == null || args.length == 0) {
                return "{}";
            }
            
            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < paramNames.length; i++) {
                Object arg = args[i];
                if (arg instanceof MultipartFile) {
                    MultipartFile file = (MultipartFile) arg;
                    params.put(paramNames[i], "File[" + file.getOriginalFilename() + "," + file.getSize() + "bytes]");
                } else if (arg instanceof HttpServletRequest) {
                    params.put(paramNames[i], "HttpServletRequest");
                } else {
                    params.put(paramNames[i], arg);
                }
            }
            
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            return "解析参数失败: " + e.getMessage();
        }
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return "null";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...(truncated)";
    }
}
