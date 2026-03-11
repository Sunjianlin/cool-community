package com.cool.pojo.entity.notify;

import lombok.Data;

@Data
public class UserNotificationLike {

    private Long id;

    private Long notificationId;

    private Long targetId;

    private String likeType;

    private String jumpUrl;
}
