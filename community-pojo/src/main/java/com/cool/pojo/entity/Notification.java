package com.cool.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Notification extends BaseEntity {
    
    private Long id;
    private Long userId;
    private Integer type;
    private String content;
    private Boolean isRead;
    private Long relatedId;
}
