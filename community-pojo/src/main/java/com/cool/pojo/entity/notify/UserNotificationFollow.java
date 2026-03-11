package com.cool.pojo.entity.notify;

import lombok.Data;

@Data
public class UserNotificationFollow {

    private Long id;

    private Long notificationId;

    private Long followedUserId;

    private Long followerUserId;

    private String jumpUrl;
}
