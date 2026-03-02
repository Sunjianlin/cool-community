package com.cool.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户背景图表
 */
@Data
public class UserBackground {
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 背景图URL
     */
    private String backgroundImage;
    
    /**
     * 是否当前使用：0-否, 1-是
     */
    private Integer isCurrent;
    
    /**
     * 获取时间
     */
    private LocalDateTime acquireTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
