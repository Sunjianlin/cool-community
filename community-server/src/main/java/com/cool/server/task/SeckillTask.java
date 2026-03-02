package com.cool.server.task;

import com.cool.pojo.entity.SeckillActivity;
import com.cool.server.mapper.SeckillActivityMapper;
import com.cool.server.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀活动定时任务
 */
@Component
@EnableScheduling
public class SeckillTask {
    
    private static final Logger log = LoggerFactory.getLogger(SeckillTask.class);
    
    @Autowired
    private SeckillActivityMapper activityMapper;
    
    @Autowired
    private SeckillService seckillService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String NEXT_DAY_ACTIVITY_KEY = "seckill:next_day";
    
    // 每天9:59:50初始化当天活动
    @Scheduled(cron = "50 59 9 * * *")
    public void initDailySeckill() {
        try {
            LocalDate today = LocalDate.now();
            List<SeckillActivity> activities = activityMapper.selectByDate(today);
            
            for (SeckillActivity activity : activities) {
                // 初始化库存到Redis
                seckillService.initStock(activity.getId());
                // 更新活动状态为进行中
                activity.setStatus(1);
                activity.setUpdateTime(LocalDateTime.now());
                activityMapper.update(activity);
                log.info("初始化当天秒杀活动: {}", activity.getId());
            }
        } catch (Exception e) {
            log.error("初始化当天秒杀活动失败: {}", e.getMessage());
        }
    }
    
    // 每天20:00:00检查次日活动
    @Scheduled(cron = "0 0 20 * * *")
    public void checkNextDayActivity() {
        try {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            List<SeckillActivity> activities = activityMapper.selectByDate(tomorrow);
            
            if (activities.isEmpty()) {
                // 没有次日活动，设置标志
                redisTemplate.opsForValue().set(NEXT_DAY_ACTIVITY_KEY, "0", 24, TimeUnit.HOURS);
                log.info("次日无秒杀活动");
            } else {
                // 有次日活动，设置标志
                redisTemplate.opsForValue().set(NEXT_DAY_ACTIVITY_KEY, "1", 24, TimeUnit.HOURS);
                log.info("次日有秒杀活动: {}", activities.size());
            }
        } catch (Exception e) {
            log.error("检查次日活动失败: {}", e.getMessage());
        }
    }
    
    // 每天23:59:50结束当天活动
    @Scheduled(cron = "50 59 23 * * *")
    public void endDailySeckill() {
        try {
            LocalDate today = LocalDate.now();
            List<SeckillActivity> activities = activityMapper.selectByDate(today);
            
            for (SeckillActivity activity : activities) {
                // 更新活动状态为已结束
                activity.setStatus(2);
                activity.setUpdateTime(LocalDateTime.now());
                activityMapper.update(activity);
                log.info("结束当天秒杀活动: {}", activity.getId());
            }
        } catch (Exception e) {
            log.error("结束当天秒杀活动失败: {}", e.getMessage());
        }
    }
}
