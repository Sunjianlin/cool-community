package com.cool.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationVO {
    
    private Long id;
    private Integer type;
    private String content;
    private Boolean isRead;
    private Long relatedId;
    private LocalDateTime createTime;
    private UserVO relatedUser;
}
