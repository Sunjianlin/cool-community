package com.cool.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Post extends BaseEntity {
    
    private Long id;
    private Long userId;
    private Long topicId;
    private String title;
    private String content;
    private String images;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectCount;
    private Integer viewCount;
    private Integer status;
    private Integer isTop;
    private Integer isEssence;
}
