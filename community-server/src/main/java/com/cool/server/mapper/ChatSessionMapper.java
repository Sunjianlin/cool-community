package com.cool.server.mapper;

import com.cool.pojo.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 聊天会话Mapper
 */
@Mapper
public interface ChatSessionMapper {
    /**
     * 创建会话
     * @param chatSession 会话信息
     * @return 影响行数
     */
    int insert(ChatSession chatSession);
    
    /**
     * 更新会话
     * @param chatSession 会话信息
     * @return 影响行数
     */
    int update(ChatSession chatSession);
    
    /**
     * 根据ID查询会话
     * @param id 会话ID
     * @return 会话信息
     */
    ChatSession selectById(@Param("id") Long id);
    
    /**
     * 查询两个用户之间的会话
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 会话信息
     */
    ChatSession selectByUserIds(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    /**
     * 查询用户的所有会话
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatSession> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 更新会话的最后消息
     * @param id 会话ID
     * @param lastMessage 最后消息
     * @param lastMessageTime 最后消息时间
     * @return 影响行数
     */
    int updateLastMessage(@Param("id") Long id, @Param("lastMessage") String lastMessage, @Param("lastMessageTime") java.time.LocalDateTime lastMessageTime);
    
    /**
     * 更新用户的未读消息数
     * @param id 会话ID
     * @param userId 用户ID
     * @param unreadCount 未读消息数
     * @return 影响行数
     */
    int updateUnreadCount(@Param("id") Long id, @Param("userId") Long userId, @Param("unreadCount") Integer unreadCount);
    
    /**
     * 更新会话状态
     * @param id 会话ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 删除会话
     * @param id 会话ID
     * @return 影响行数
     */
    int delete(@Param("id") Long id);
}
