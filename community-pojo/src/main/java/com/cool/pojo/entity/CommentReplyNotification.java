package com.cool.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentReplyNotification {

    private Long id;

    private Long userId;

    private Long commentId;

    private Long replyId;

    private Long postId;

    private String postTitle;

    private Long commenterId;

    private String commenterName;

    private String commenterAvatar;

    private String content;

    private Integer isRead;

    private LocalDateTime createTime;
}
