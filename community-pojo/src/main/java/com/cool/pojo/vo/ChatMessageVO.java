package com.cool.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天消息VO
 */
@Data
public class ChatMessageVO {
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型：1-文本，2-图片，3-文件
     */
    private Integer type;
    
    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;
    
    /**
     * 消息状态：0-发送失败，1-发送成功
     */
    private Integer status;
    
    /**
     * 消息时间
     */
    private LocalDateTime createTime;
    
    /**
     * 是否是自己发送的消息
     */
    private Boolean isMine;
    
    /**
     * 发送者昵称
     */
    private String senderName;
    
    /**
     * 发送者头像
     */
    private String senderAvatar;
}
