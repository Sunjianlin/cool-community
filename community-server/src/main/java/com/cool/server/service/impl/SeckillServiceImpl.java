package com.cool.server.service.impl;

import com.cool.pojo.entity.SeckillActivity;
import com.cool.pojo.entity.UserBackground;
import com.cool.pojo.entity.UserSeckillRecord;
import com.cool.server.mapper.SeckillActivityMapper;
import com.cool.server.mapper.UserBackgroundMapper;
import com.cool.server.mapper.UserSeckillRecordMapper;
import com.cool.server.service.SeckillService;
import com.cool.server.service.UserBackgroundService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀服务实现类
 */
@Service
public class SeckillServiceImpl implements SeckillService {
    
    private static final Logger log = LoggerFactory.getLogger(SeckillServiceImpl.class);
    
    @Autowired
    private SeckillActivityMapper activityMapper;
    
    @Autowired
    private UserSeckillRecordMapper recordMapper;
    
    @Autowired
    private UserBackgroundService userBackgroundService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // Redis键前缀
    private static final String STOCK_KEY = "seckill:stock:";
    private static final String USER_KEY = "seckill:user:";
    private static final String NEXT_DAY_ACTIVITY_KEY = "seckill:next_day";
    
    // 库存检查时间窗口（秒）
    private static final int STOCK_CHECK_WINDOW = 60;
    
    @Override
    @Transactional
    public boolean doSeckill(Long userId, Long activityId) {
        try {
            // 1. 检查活动状态
            SeckillActivity activity = activityMapper.selectById(activityId);
            if (activity == null || activity.getStatus() != 1) {
                log.info("活动不存在或未开始: {}", activityId);
                return false;
            }
            
            // 2. 检查用户是否已参与
            String userKey = USER_KEY + activityId + ":" + userId;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(userKey))) {
                log.info("用户已参与过活动: {}, {}", userId, activityId);
                return false;
            }
            
            // 3. 检查数据库中是否已参与
            Integer count = recordMapper.countByUserIdAndActivityId(userId, activityId);
            if (count != null && count > 0) {
                log.info("用户已参与过活动: {}, {}", userId, activityId);
                // 标记到Redis
                redisTemplate.opsForValue().set(userKey, "1", 24, TimeUnit.HOURS);
                return false;
            }
            
            // 4. 减库存
            String stockKey = STOCK_KEY + activityId;
            Long stock = redisTemplate.opsForValue().decrement(stockKey);
            if (stock < 0) {
                // 库存不足，回滚
                redisTemplate.opsForValue().increment(stockKey);
                log.info("库存不足: {}", activityId);
                return false;
            }
            
            // 5. 记录秒杀结果
            UserSeckillRecord record = new UserSeckillRecord();
            record.setUserId(userId);
            record.setActivityId(activityId);
            record.setStatus(0); // 成功
            record.setCreateTime(LocalDateTime.now());
            recordMapper.insert(record);
            
            // 6. 发放背景图
            userBackgroundService.addBackground(userId, activity.getBackgroundImage());
            
            // 7. 标记用户已参与
            redisTemplate.opsForValue().set(userKey, "1", 24, TimeUnit.HOURS);
            
            log.info("秒杀成功: {}, {}", userId, activityId);
            return true;
        } catch (Exception e) {
            // 异常处理，回滚库存
            String stockKey = STOCK_KEY + activityId;
            redisTemplate.opsForValue().increment(stockKey);
            log.error("秒杀失败: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public SeckillActivity getActivityById(Long id) {
        return activityMapper.selectById(id);
    }
    
    @Override
    public SeckillActivity getNextDayActivity() {
        return activityMapper.selectNextDayActivity();
    }
    
    @Override
    public boolean hasNextDayActivity() {
        // 先检查Redis缓存
        Object hasActivity = redisTemplate.opsForValue().get(NEXT_DAY_ACTIVITY_KEY);
        if (hasActivity != null) {
            return "1".equals(hasActivity.toString());
        }
        
        // 缓存未命中，查询数据库
        SeckillActivity activity = activityMapper.selectNextDayActivity();
        boolean result = activity != null;
        redisTemplate.opsForValue().set(NEXT_DAY_ACTIVITY_KEY, result ? "1" : "0", 1, TimeUnit.HOURS);
        return result;
    }
    
    @Override
    public void initStock(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity != null) {
            String stockKey = STOCK_KEY + activityId;
            redisTemplate.opsForValue().set(stockKey, activity.getStock(), STOCK_CHECK_WINDOW, TimeUnit.SECONDS);
            log.info("初始化活动库存: {}, 库存: {}", activityId, activity.getStock());
        }
    }
    
    @Override
    public List<SeckillActivity> getActivityList() {
        return activityMapper.list();
    }
    
    @Override
    public List<SeckillActivity> getAllActivities() {
        return activityMapper.list();
    }
    
    @Override
    public void createActivity(SeckillActivity activity) {
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());
        activity.setStatus(0); // 未开始
        activityMapper.insert(activity);
        
        // 清除次日活动缓存
        redisTemplate.delete(NEXT_DAY_ACTIVITY_KEY);
        log.info("创建活动: {}", activity.getId());
    }
    
    @Override
    public void updateActivity(SeckillActivity activity) {
        activity.setUpdateTime(LocalDateTime.now());
        activityMapper.update(activity);
        
        // 清除次日活动缓存
        redisTemplate.delete(NEXT_DAY_ACTIVITY_KEY);
        log.info("更新活动: {}", activity.getId());
    }
    
    @Override
    public void deleteActivity(Long id) {
        activityMapper.delete(id);
        
        // 清除相关缓存
        redisTemplate.delete(STOCK_KEY + id);
        redisTemplate.delete(NEXT_DAY_ACTIVITY_KEY);
        log.info("删除活动: {}", id);
    }
}
