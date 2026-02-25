package com.cool.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天会话实体类
 */
@Data
public class ChatSession {
    /**
     * 会话ID
     */
    private Long id;
    
    /**
     * 用户1 ID
     */
    private Long userId1;
    
    /**
     * 用户2 ID
     */
    private Long userId2;
    
    /**
     * 最后一条消息
     */
    private String lastMessage;
    
    /**
     * 最后一条消息时间
     */
    private LocalDateTime lastMessageTime;
    
    /**
     * 用户1未读消息数
     */
    private Integer unreadCountUser1;
    
    /**
     * 用户2未读消息数
     */
    private Integer unreadCountUser2;
    
    /**
     * 会话状态：0-已关闭，1-活跃
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
