package com.cool.server.task;

import com.cool.server.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 秒杀活动定时任务
 */
@Slf4j
@Component
@EnableScheduling
public class SeckillTask {
    
    @Autowired
    private SeckillService seckillService;
    
    // 每10秒检查并更新活动状态
    @Scheduled(cron = "0/10 * * * * *")
    public void updateActivityStatus() {
        try {
            seckillService.updateActivityStatus();
        } catch (Exception e) {
            log.error("更新活动状态失败: {}", e.getMessage(), e);
        }
    }
    
    // 每5分钟清理过期的Redis缓存
    @Scheduled(cron = "0 */5 * * * *")
    public void cleanExpiredCache() {
        // 清理逻辑可以在这里添加
        log.debug("清理过期缓存");
    }
}
