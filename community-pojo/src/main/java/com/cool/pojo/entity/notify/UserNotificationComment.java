package com.cool.pojo.entity.notify;

import lombok.Data;

@Data
public class UserNotificationComment {

    private Long id;

    private Long notificationId;

    private Long postId;

    private Long commentId;

    private String commentType;

    private String jumpUrl;
}
