package com.cool.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LikeNotificationVO {

    private Long id;

    private Long postId;

    private String postTitle;

    private Long likerId;

    private String likerName;

    private String likerAvatar;

    private Integer isRead;

    private LocalDateTime createTime;

    private String type;

    public LikeNotificationVO() {
        this.type = "like";
    }
}
