package com.cool.server.event.listener;

import com.cool.server.event.PostChangeEvent;
import com.cool.server.event.TopicChangeEvent;
import com.cool.server.event.UserChangeEvent;
import com.cool.server.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchSyncEventListener {
    
    private final SearchService searchService;
    private final StringRedisTemplate stringRedisTemplate;
    
    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private static final String SYNC_RETRY_PREFIX = "search:sync:retry:";
    
    @Async
    @EventListener
    public void handlePostChangeEvent(PostChangeEvent event) {
        Long postId = event.getPostId();
        PostChangeEvent.ChangeType changeType = event.getChangeType();
        
        log.info("收到帖子变更事件: postId={}, changeType={}", postId, changeType);
        
        if (changeType == PostChangeEvent.ChangeType.DELETE) {
            handlePostDelete(postId);
            return;
        }
        
        executeWithRetry(() -> searchService.syncPostToEs(postId), 
            "帖子同步", postId, changeType.name());
    }
    
    @Async
    @EventListener
    public void handleUserChangeEvent(UserChangeEvent event) {
        Long userId = event.getUserId();
        UserChangeEvent.ChangeType changeType = event.getChangeType();
        
        log.info("收到用户变更事件: userId={}, changeType={}", userId, changeType);
        
        if (changeType == UserChangeEvent.ChangeType.DELETE) {
            return;
        }
        
        executeWithRetry(() -> searchService.syncUserToEs(userId), 
            "用户同步", userId, changeType.name());
    }
    
    @Async
    @EventListener
    public void handleTopicChangeEvent(TopicChangeEvent event) {
        Long topicId = event.getTopicId();
        TopicChangeEvent.ChangeType changeType = event.getChangeType();
        
        log.info("收到话题变更事件: topicId={}, changeType={}", topicId, changeType);
        
        if (changeType == TopicChangeEvent.ChangeType.DELETE) {
            return;
        }
        
        executeWithRetry(() -> searchService.syncTopicToEs(topicId), 
            "话题同步", topicId, changeType.name());
    }
    
    private void handlePostDelete(Long postId) {
        log.info("处理帖子删除事件: postId={}", postId);
    }
    
    private void executeWithRetry(Runnable task, String taskName, Long id, String changeType) {
        String retryKey = SYNC_RETRY_PREFIX + taskName + ":" + id;
        int retryCount = getRetryCount(retryKey);
        
        if (retryCount >= MAX_RETRY_COUNT) {
            log.error("{}达到最大重试次数，放弃同步: id={}, changeType={}", taskName, id, changeType);
            clearRetryCount(retryKey);
            return;
        }
        
        try {
            task.run();
            clearRetryCount(retryKey);
            log.debug("{}成功: id={}", taskName, id);
        } catch (Exception e) {
            incrementRetryCount(retryKey);
            int currentRetry = getRetryCount(retryKey);
            log.error("{}失败，第{}次重试: id={}, error={}", taskName, currentRetry, id, e.getMessage());
            
            if (currentRetry < MAX_RETRY_COUNT) {
                scheduleRetry(task, taskName, id, changeType);
            } else {
                log.error("{}最终失败，需要人工介入: id={}, changeType={}", taskName, id, changeType);
                clearRetryCount(retryKey);
            }
        }
    }
    
    private void scheduleRetry(Runnable task, String taskName, Long id, String changeType) {
        try {
            Thread.sleep(RETRY_DELAY_MS);
            executeWithRetry(task, taskName, id, changeType);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("重试被中断: taskName={}, id={}", taskName, id);
        }
    }
    
    private int getRetryCount(String key) {
        String count = stringRedisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 0;
    }
    
    private void incrementRetryCount(String key) {
        stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.expire(key, 1, TimeUnit.HOURS);
    }
    
    private void clearRetryCount(String key) {
        stringRedisTemplate.delete(key);
    }
}
