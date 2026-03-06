package com.cool.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SystemNotificationVO {

    private Long id;

    private String title;

    private String content;

    private Integer type;

    private Integer isRead;

    private LocalDateTime createTime;

    private String notificationType;

    public SystemNotificationVO() {
        this.notificationType = "system";
    }
}
