package com.cool.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户积分实体类
 */
@Data
public class UserPoints extends BaseEntity {
    private Long id;
    private Long userId;
    private Integer points;
    private String type;
    private String description;
    private LocalDateTime createdAt;
}
