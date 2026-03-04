package com.cool.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分明细实体类
 */
@Data
public class PointsTransaction {
    private Long id;
    private Long userId;
    private Integer points;
    private Integer type;
    private LocalDateTime createAt;
}