package com.cool.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostCommentNotificationVO {

    private Long id;

    private Long postId;

    private String postTitle;

    private Long commenterId;

    private String commenterName;

    private String commenterAvatar;

    private String content;

    private Integer isRead;

    private LocalDateTime createTime;

    private String type;

    public PostCommentNotificationVO() {
        this.type = "post_comment";
    }
}
