package com.cool.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowNotification {

    private Long id;

    private Long userId;

    private Long followerId;

    private String followerName;

    private String followerAvatar;

    private Integer isRead;

    private LocalDateTime createTime;
}
