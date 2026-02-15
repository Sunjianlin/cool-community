package com.cool.pojo.vo;

import lombok.Data;

@Data
public class TopicVO {
    
    private Long id;
    private String name;
    private String description;
    private String icon;
    private String cover;
    private String category;
    private Integer followCount;
    private Integer postCount;
    private Integer isHot;
    private Boolean isFollowed;
}
