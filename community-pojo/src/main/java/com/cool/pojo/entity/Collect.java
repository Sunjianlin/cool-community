package com.cool.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Collect extends BaseEntity {
    
    private Long id;
    private Long userId;
    private Long postId;
}
