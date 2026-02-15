package com.cool.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Message extends BaseEntity {
    
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private Integer type;
    private String content;
    private Long relatedId;
    private Integer isRead;
}
