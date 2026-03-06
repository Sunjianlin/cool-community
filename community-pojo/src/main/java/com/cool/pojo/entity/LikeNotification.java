package com.cool.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LikeNotification {

    private Long id;

    private Long userId;

    private Long postId;

    private String postTitle;

    private Long likerId;

    private String likerName;

    private String likerAvatar;

    private Integer isRead;

    private LocalDateTime createTime;
}
