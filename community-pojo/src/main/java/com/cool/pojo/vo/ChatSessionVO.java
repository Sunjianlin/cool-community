package com.cool.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天会话VO
 */
@Data
public class ChatSessionVO {
    /**
     * 会话ID
     */
    private Long id;
    
    /**
     * 对方用户ID
     */
    private Long userId;
    
    /**
     * 对方用户昵称
     */
    private String name;
    
    /**
     * 对方用户头像
     */
    private String avatar;
    
    /**
     * 最后一条消息
     */
    private String lastMessage;
    
    /**
     * 最后一条消息时间
     */
    private LocalDateTime lastMessageTime;
    
    /**
     * 未读消息数
     */
    private Integer unreadCount;
    
    /**
     * 会话状态：0-已关闭，1-活跃
     */
    private Integer status;
    
    /**
     * 是否关注对方
     */
    private Boolean isFollowing;
}
