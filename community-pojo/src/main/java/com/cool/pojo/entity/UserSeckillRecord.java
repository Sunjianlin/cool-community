package com.cool.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户秒杀记录表
 */
@Data
public class UserSeckillRecord {
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 活动ID
     */
    private Long activityId;
    
    /**
     * 状态：0-成功, 1-失败
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
