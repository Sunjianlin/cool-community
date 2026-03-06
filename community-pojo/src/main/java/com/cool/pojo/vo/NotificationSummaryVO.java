package com.cool.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationSummaryVO {
    private Integer unreadPrivate;
    private Integer unreadCommentReply;
    private Integer unreadPostComment;
    private Integer unreadSystem;
    private Integer totalUnread;
}
