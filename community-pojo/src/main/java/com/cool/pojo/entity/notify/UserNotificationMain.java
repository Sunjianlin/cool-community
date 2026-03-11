package com.cool.pojo.entity.notify;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserNotificationMain {

    private Long id;

    private String messageId;

    private Long receiverId;

    private Long senderId;

    private String notifyType;

    private String content;

    private Integer readStatus;

    private Integer isDeleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
