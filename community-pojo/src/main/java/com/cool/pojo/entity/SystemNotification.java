package com.cool.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SystemNotification {

    private Long id;

    private Long userId;

    private String title;

    private String content;

    private Integer type;

    private Integer isRead;

    private LocalDateTime createTime;
}
