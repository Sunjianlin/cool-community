package com.cool.server.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * 日志工具类
 * 提供统一的日志记录方法，支持链路追踪
 */
@Slf4j
public class LogUtils {

    private static final String TRACE_ID = "traceId";
    private static final String USER_ID = "userId";

    /**
     * 生成并设置链路追踪ID
     */
    public static String generateTraceId() {
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        MDC.put(TRACE_ID, traceId);
        return traceId;
    }

    /**
     * 获取当前链路追踪ID
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    /**
     * 设置用户ID到MDC
     */
    public static void setUserId(Long userId) {
        if (userId != null) {
            MDC.put(USER_ID, String.valueOf(userId));
        }
    }

    /**
     * 获取当前用户ID
     */
    public static String getUserId() {
        return MDC.get(USER_ID);
    }

    /**
     * 清除MDC
     */
    public static void clear() {
        MDC.clear();
    }

    /**
     * 记录接口请求日志
     */
    public static void logRequest(String method, String uri, String ip, Object params) {
        log.info("[REQUEST] method={}, uri={}, ip={}, params={}", method, uri, ip, params);
    }

    /**
     * 记录接口响应日志
     */
    public static void logResponse(String method, String uri, long costTime, Object result) {
        log.info("[RESPONSE] method={}, uri={}, cost={}ms, result={}", method, uri, costTime, result);
    }

    /**
     * 记录业务操作日志
     */
    public static void logBusiness(String operation, String detail, Long userId) {
        log.info("[BUSINESS] operation={}, detail={}, userId={}", operation, detail, userId);
    }

    /**
     * 记录异常日志
     */
    public static void logError(String operation, String message, Throwable e) {
        log.error("[ERROR] operation={}, message={}", operation, message, e);
    }

    /**
     * 记录慢查询日志
     */
    public static void logSlowQuery(String sql, long costTime) {
        if (costTime > 1000) {
            log.warn("[SLOW_SQL] cost={}ms, sql={}", costTime, sql);
        }
    }

    /**
     * 记录缓存操作日志
     */
    public static void logCache(String operation, String key, boolean hit) {
        log.debug("[CACHE] operation={}, key={}, hit={}", operation, key, hit);
    }

    /**
     * 记录MQ消息日志
     */
    public static void logMQ(String queue, String operation, String messageId) {
        log.info("[MQ] queue={}, operation={}, messageId={}", queue, operation, messageId);
    }
}
