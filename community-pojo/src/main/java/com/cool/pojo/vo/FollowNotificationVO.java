package com.cool.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowNotificationVO {

    private Long id;

    private Long followerId;

    private String followerName;

    private String followerAvatar;

    private Integer isRead;

    private LocalDateTime createTime;

    private String type;

    public FollowNotificationVO() {
        this.type = "follow";
    }
}
