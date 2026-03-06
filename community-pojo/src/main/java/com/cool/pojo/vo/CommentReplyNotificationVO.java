package com.cool.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentReplyNotificationVO {

    private Long id;

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

    private String type;

    public CommentReplyNotificationVO() {
        this.type = "comment_reply";
    }
}
