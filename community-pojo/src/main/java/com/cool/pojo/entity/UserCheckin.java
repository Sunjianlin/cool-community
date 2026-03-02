package com.cool.pojo.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户签到实体类
 */
@Data
public class UserCheckin extends BaseEntity {
    private Long id;
    private Long userId;
    private LocalDate checkinDate;
    private Integer pointsEarned;
    private LocalDateTime createdAt;
}
