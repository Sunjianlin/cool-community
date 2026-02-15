package com.cool.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductReview extends BaseEntity {
    
    private Long id;
    private Long productId;
    private Long userId;
    private String title;
    private String content;
    private String images;
    private Integer rating;
    private Integer likeCount;
    private Integer commentCount;
    private Integer status;
}
