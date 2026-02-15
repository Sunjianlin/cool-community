package com.cool.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Topic extends BaseEntity {
    
    private Long id;
    private String name;
    private String description;
    private String icon;
    private String cover;
    private String category;
    private Integer followCount;
    private Integer postCount;
    private Integer status;
    private Integer isHot;
}
