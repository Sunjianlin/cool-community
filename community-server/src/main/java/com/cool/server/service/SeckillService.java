package com.cool.server.service;

import com.cool.pojo.entity.SeckillActivity;
import com.cool.pojo.entity.UserBackground;
import java.util.List;

/**
 * 秒杀服务接口
 */
public interface SeckillService {
    
    /**
     * 执行秒杀
     */
    boolean doSeckill(Long userId, Long activityId);
    
    /**
     * 获取活动详情
     */
    SeckillActivity getActivityById(Long id);
    
    /**
     * 获取次日活动
     */
    SeckillActivity getNextDayActivity();
    
    /**
     * 检查是否有次日活动
     */
    boolean hasNextDayActivity();
    
    /**
     * 初始化活动库存到Redis
     */
    void initStock(Long activityId);
    
    /**
     * 获取活动列表
     */
    List<SeckillActivity> getActivityList();
    
    /**
     * 获取所有活动
     */
    List<SeckillActivity> getAllActivities();
    
    /**
     * 创建活动
     */
    void createActivity(SeckillActivity activity);
    
    /**
     * 更新活动
     */
    void updateActivity(SeckillActivity activity);
    
    /**
     * 删除活动
     */
    void deleteActivity(Long id);
}
