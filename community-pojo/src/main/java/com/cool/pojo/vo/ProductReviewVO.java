package com.cool.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductReviewVO {
    
    private Long id;
    private Long productId;
    private String productName;
    private Long userId;
    private String username;
    private String userAvatar;
    private String userNickname;
    private String title;
    private String content;
    private String[] images;
    private Integer rating;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isLiked;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
