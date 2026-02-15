package com.cool.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostVO {
    
    private Long id;
    private Long userId;
    private String username;
    private String userAvatar;
    private String userNickname;
    private Long topicId;
    private String topicName;
    private String title;
    private String content;
    private String images;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectCount;
    private Integer viewCount;
    private Integer status;
    private String statusName;
    private Integer isTop;
    private Integer isEssence;
    private Boolean isLiked;
    private Boolean isCollected;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    public String[] getImageArray() {
        if (images == null || images.isEmpty()) {
            return new String[0];
        }
        return images.split(",");
    }
}
