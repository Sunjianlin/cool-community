package com.cool.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageVO {
    
    private Long id;
    private Long fromUserId;
    private String fromUsername;
    private String fromUserAvatar;
    private Long toUserId;
    private String toUsername;
    private String toUserAvatar;
    private Integer type;
    private String content;
    private Long relatedId;
    private Integer isRead;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
