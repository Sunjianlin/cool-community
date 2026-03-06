package com.cool.pojo.dto;

import lombok.Data;

@Data
public class NotificationSummaryDTO {

    private Integer unreadPrivate;

    private Integer unreadComment;

    private Integer unreadLike;

    private Integer unreadFollow;

    private Integer unreadSystem;

    private Integer totalUnread;
}
