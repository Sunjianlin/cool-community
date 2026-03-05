package com.cool.server.service.impl;

import com.cool.common.enumeration.SeckillResult;
import com.cool.pojo.entity.SeckillActivity;
import com.cool.pojo.entity.UserSeckillRecord;
import com.cool.server.mapper.SeckillActivityMapper;
import com.cool.server.mapper.UserSeckillRecordMapper;
import com.cool.server.service.SeckillService;
import com.cool.server.service.UserBackgroundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.cool.common.constant.RedisConstant.*;
import static java.time.LocalTime.now;

/**
 * 秒杀服务实现类
 */
@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {
    
    @Autowired
    private SeckillActivityMapper activityMapper;
    
    @Autowired
    private UserSeckillRecordMapper recordMapper;
    
    @Autowired
    private UserBackgroundService userBackgroundService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    @Qualifier("seckillAsyncExecutor")
    private ThreadPoolExecutor seckillAsyncExecutor;
    

    
    // Redis脚本：原子性检查库存和扣减（增强版，包含活动状态检查）
    private static final String STOCK_CHECK_SCRIPT = """
    local cjson = require 'cjson'
    local stockKey = KEYS[1]
    local userKey = KEYS[2]
    local activityKey = KEYS[3]
    
    -- 获取活动信息（JSON字符串）
    local activityData = redis.call('get', activityKey)
    if not activityData then
        return -3
    end
    
    -- 解析活动信息
    local activity = cjson.decode(activityData)
    local now = tonumber(redis.call('time')[1]) -- 当前时间戳（秒）
    local startTime = tonumber(activity.startTime)
    local endTime = tonumber(activity.endTime)
    local status = tonumber(activity.status)
    
    -- 校验活动状态
    if status ~= 1 then
        return -4 -- 活动未开始/已结束
    end
    -- 校验活动时间
    if now < startTime then
        return -5 -- 活动未开始
    end
    if now > endTime then
        return -6 -- 活动已结束
    end
    
    -- 获取库存
    local stock = tonumber(redis.call('get', stockKey))
    if not stock or stock <= 0 then
        return 0
    end
    
    -- 检查用户是否已参与
    if redis.call('exists', userKey) == 1 then
        return -1
    end
    
    -- 原子扣减库存
    redis.call('decr', stockKey)
    -- 标记用户已参与，过期时间与活动结束时间一致
    local ttl = redis.call('ttl', stockKey)
    if ttl > 0 then
        redis.call('set', userKey, '1', 'EX', ttl)
    else
        redis.call('set', userKey, '1', 'EX', 86400)
    end
    return 1
    """;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeckillResult doSeckill(Long userId, Long activityId) {
        if (userId == null || activityId == null) {
            log.error("秒杀失败：userId或activityId为空，userId={}, activityId={}", userId, activityId);
            return SeckillResult.SYSTEM_ERROR;
        }

        String stockKey = STOCK_KEY + activityId;
        String userKey = USER_KEY + activityId + ":" + userId;
        String activityKey = ACTIVITY_KEY + activityId;

        try {
            // 从Redis获取活动信息
            Object activityObj = redisTemplate.opsForValue().get(activityKey);
            if (activityObj == null) {
                log.info("活动不存在或未预热: {}", activityId);
                return SeckillResult.ACTIVITY_NOT_FOUND;
            }
            
            // 执行Lua脚本原子性操作
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(STOCK_CHECK_SCRIPT, Long.class);
            List<String> keys = Arrays.asList(stockKey, userKey, activityKey);
            Long result = redisTemplate.execute(script, keys);

            if (result == null) {
                log.error("秒杀失败：Lua脚本返回null，userId={}, activityId={}", userId, activityId);
                return SeckillResult.SYSTEM_ERROR;
            }
            if (result == -4) {
                log.info("活动状态异常: {}", activityId);
                return SeckillResult.ACTIVITY_STATUS_ERROR;
            } else if (result == -5) {
                log.info("活动未开始: {}", activityId);
                return SeckillResult.ACTIVITY_NOT_STARTED;
            } else if (result == -6) {
                log.info("活动已结束: {}", activityId);
                return SeckillResult.ACTIVITY_ENDED;
            }

            seckillAsyncExecutor.execute(() -> {
                try {
                    SeckillActivity activity = activityMapper.selectById(activityId);
                    if (activity != null) {
                        UserSeckillRecord record = new UserSeckillRecord();
                        record.setUserId(userId);
                        record.setActivityId(activityId);
                        record.setStatus(0);
                        record.setCreateTime(LocalDateTime.now());
                        recordMapper.insert(record);

                        userBackgroundService.addBackground(userId, activity.getBackgroundImage());
                        activityMapper.decreaseStock(activityId, 1);
                    }
                } catch (Exception e) {
                    log.error("秒杀异步落库失败: {}", e.getMessage(), e);
                    // 以后再说     ！！！！    补偿逻辑（比如记录到消息队列，后续重试）
                }
            });

            log.info("秒杀成功: userId={}, activityId={}", userId, activityId);
            return SeckillResult.SUCCESS;
        } catch (Exception e) {
            log.error("秒杀失败: {}", e.getMessage(), e);
            rollbackRedis(stockKey, userKey);
            throw new RuntimeException("秒杀业务异常", e);
        }
    }

    private void rollbackRedis(String stockKey, String userKey) {
        try {
            redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                // 1. 先检查用户是否已参与，避免误回滚
                if (connection.exists(userKey.getBytes(StandardCharsets.UTF_8))) {
                    connection.multi();
                    // 2. 原子回滚库存
                    connection.incr(stockKey.getBytes(StandardCharsets.UTF_8));
                    // 3. 删除用户参与记录
                    connection.del(userKey.getBytes(StandardCharsets.UTF_8));
                    // 4. 校验exec结果
                    List<Object> results = connection.exec();
                    if (results == null || results.size() != 2) {
                        log.error("回滚库存exec执行失败: stockKey={}, userKey={}", stockKey, userKey);
                        return false;
                    }
                    log.info("回滚库存成功: stockKey={}, userKey={}", stockKey, userKey);
                    return true;
                }
                return false;
            });
        } catch (Exception ex) {
            log.error("回滚库存失败: {}", ex.getMessage(), ex);
        }
    }
    
    @Override
    public SeckillActivity getActivityById(Long id) {
        return activityMapper.selectById(id);
    }
    
    @Override
    public SeckillActivity getNextDayActivity() {
        // 不再限定为次日活动，返回最近的一个未开始活动
        return activityMapper.selectNextActivity();
    }
    
    @Override
    public boolean hasNextDayActivity() {
        // 不再限定为次日活动，检查是否有未开始的活动
        SeckillActivity activity = activityMapper.selectNextActivity();
        return activity != null;
    }

    @Override
    public void initStock(Long activityId,int stock,long expireSeconds) {
        LocalDateTime now = LocalDateTime.now();

        if (expireSeconds <= 0) {
            log.warn("活动已结束，不初始化库存: {}", activityId);
            return;
        }
        String stockKey = STOCK_KEY + activityId;
        String activityKey = ACTIVITY_KEY + activityId;

        // 1. 查询完整活动信息
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.error("活动不存在，无法初始化库存: {}", activityId);
            return;
        }

        // 2. 存储库存（用String.valueOf保证Redis中是原生数值）
        redisTemplate.opsForValue().set(stockKey, String.valueOf(stock), expireSeconds, TimeUnit.SECONDS);
        // 3. 存储完整活动信息
        redisTemplate.opsForValue().set(activityKey, activity, expireSeconds, TimeUnit.SECONDS);

        log.info("初始化活动库存: activityId={}, 库存={}, 过期时间={}秒", activityId, stock, expireSeconds);
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
    @Transactional
    public void createActivity(SeckillActivity activity) {
        LocalDateTime now = LocalDateTime.now();
        activity.setCreateTime(now);
        activity.setUpdateTime(now);
        
        if (now.isBefore(activity.getStartTime())) {
            activity.setStatus(0);
        } else if (now.isAfter(activity.getEndTime())) {
            activity.setStatus(2);
        } else {
            activity.setStatus(1);
        }
        
        activityMapper.insert(activity);
        SeckillActivity seckillActivity = activityMapper.selectByName(activity.getActivityName());
        
        // 创建时预热库存到Redis（活动进行中或即将开始）
        long expireSeconds = Duration.between(now, activity.getEndTime()).getSeconds();
        if (seckillActivity.getStatus()==1) {
            initStock(seckillActivity.getId(),seckillActivity.getStock(), expireSeconds);
        }
        
        log.info("创建活动: activityId={}, 开始时间: {}, 结束时间: {}", 
                activity.getId(), activity.getStartTime(), activity.getEndTime());
    }
    
    @Override
    @Transactional
    public void updateActivity(SeckillActivity activity) {
        SeckillActivity existingActivity = activityMapper.selectById(activity.getId());
        if (existingActivity == null) {
            throw new RuntimeException("活动不存在");
        }
        
        LocalDateTime now = LocalDateTime.now();
        activity.setUpdateTime(now);
        
        // 活动结束后不可修改结束时间
        if (existingActivity.getStatus() == 2) {
            throw new RuntimeException("活动已结束，不可修改");
        }
        
        // 活动已开始后不可修改开始时间
        if (existingActivity.getStatus() == 1 && !activity.getStartTime().equals(existingActivity.getStartTime())) {
            throw new RuntimeException("活动已开始，不可修改开始时间");
        }
        
        // 根据时间更新状态
        if (now.isBefore(activity.getStartTime())) {
            activity.setStatus(0);
        } else if (now.isAfter(activity.getEndTime())) {
            activity.setStatus(2);
        } else {
            activity.setStatus(1);
        }
        
        activityMapper.update(activity);
        
        // 更新Redis中的库存和过期时间
        if (activity.getStatus() == 1) {
            String stockKey = STOCK_KEY + activity.getId();
            String activityKey = ACTIVITY_KEY + activity.getId();
            
            // 计算新的过期时间
            long newExpireSeconds = Duration.between(now, activity.getEndTime()).getSeconds();
            
            if (newExpireSeconds > 0) {
                // 更新库存和活动信息的过期时间
                redisTemplate.expire(stockKey, newExpireSeconds, TimeUnit.SECONDS);
                redisTemplate.expire(activityKey, newExpireSeconds, TimeUnit.SECONDS);
                log.info("更新活动缓存过期时间: activityId={}, 新过期时间={}秒", activity.getId(), newExpireSeconds);
            }
        } else {
            redisTemplate.delete(STOCK_KEY + activity.getId());
            redisTemplate.delete(ACTIVITY_KEY + activity.getId());
        }
        
        log.info("更新活动: activityId={}, 开始时间: {}, 结束时间: {}", 
                activity.getId(), activity.getStartTime(), activity.getEndTime());
    }

    @Override
    @Transactional
    public void deleteActivity(Long id) {
        activityMapper.delete(id);

        // 清除相关缓存
        String stockKey = STOCK_KEY + id;
        String activityKey = ACTIVITY_KEY + id;
        redisTemplate.delete(stockKey);
        redisTemplate.delete(activityKey);

        // 批量删除用户参与记录（scan替代keys）
        String userKeyPattern = USER_KEY + id + ":*";
        Set<String> userKeys = redisTemplate.keys(userKeyPattern);
        if (userKeys != null && !userKeys.isEmpty()) {
            redisTemplate.delete(userKeys);
        }

        log.info("删除活动: {}", id);
    }
    
    @Override
    public void updateActivityStatus() {
        List<SeckillActivity> activities = activityMapper.list();
        LocalDateTime now = LocalDateTime.now();
        
        for (SeckillActivity activity : activities) {
            if (activity.getStatus() == 0 && now.isAfter(activity.getStartTime())) {
                activity.setStatus(1);
                activity.setUpdateTime(now);
                activityMapper.update(activity);
                initStock(activity.getId(),activity.getStock(),Duration.between(now, activity.getEndTime()).getSeconds());
                log.info("活动开始: activityId={}", activity.getId());
            } else if (activity.getStatus() == 1 && now.isAfter(activity.getEndTime())) {
                activity.setStatus(2);
                activity.setUpdateTime(now);
                activityMapper.update(activity);
                redisTemplate.delete(STOCK_KEY + activity.getId());
                redisTemplate.delete(ACTIVITY_KEY + activity.getId());
                log.info("活动结束: activityId={}", activity.getId());
            }
        }
    }
}
