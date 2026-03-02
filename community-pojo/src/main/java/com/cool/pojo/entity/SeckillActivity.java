package com.cool.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 秒杀活动实体类
 */
@Data
public class SeckillActivity {
    /**
     * 活动ID
     */
    private Long id;
    
    /**
     * 活动名称
     */
    private String activityName;
    
    /**
     * 背景图URL
     */
    private String backgroundImage;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 库存数量
     */
    private Integer stock;
    
    /**
     * 状态：0-未开始, 1-进行中, 2-已结束, 3-已下架
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
