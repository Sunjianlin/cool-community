package com.cool.pojo.document;

import lombok.Data;

import java.util.Date;

@Data
public class PostDocument {
    
    private Long id;
    
    private String title;
    
    private String titleSuggest;
    
    private String content;
    
    private Long userId;
    
    private String username;
    
    private String userNickname;
    
    private String userAvatar;
    
    private Long topicId;
    
    private String topicName;
    
    private Integer likeCount;
    
    private Integer commentCount;
    
    private Integer viewCount;
    
    private Integer status;
    
    private Boolean isTop;
    
    private Boolean isEssence;
    
    private Date createTime;
    
    private Date updateTime;
}
