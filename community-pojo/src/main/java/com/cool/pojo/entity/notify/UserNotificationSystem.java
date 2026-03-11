package com.cool.pojo.entity.notify;

import lombok.Data;

@Data
public class UserNotificationSystem {

    private Long id;

    private Long notificationId;

    private String systemMsgType;

    private Long relatedBusinessId;

    private String jumpUrl;
}
