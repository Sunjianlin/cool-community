package com.cool.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Like extends BaseEntity {
    
    private Long id;
    private Long userId;
    private Long relatedId;
    private Integer type;
}
