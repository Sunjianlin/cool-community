package com.cool.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentVO {
    
    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private String userAvatar;
    private String userNickname;
    private Long parentId;
    private Long replyUserId;
    private String replyUsername;
    private String content;
    private Integer likeCount;
    private Boolean isLiked;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
