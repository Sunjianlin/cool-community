package com.cool.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserNotificationSummary {

    private Long id;

    private Long userId;

    private Integer unreadPrivate;

    private Integer unreadComment;

    private Integer unreadLike;

    private Integer unreadFollow;

    private Integer unreadSystem;

    private Integer totalUnread;

    private LocalDateTime updateTime;
}
