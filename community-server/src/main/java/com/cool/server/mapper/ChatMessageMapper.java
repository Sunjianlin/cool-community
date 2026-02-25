package com.cool.server.mapper;

import com.cool.pojo.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 聊天消息Mapper
 */
@Mapper
public interface ChatMessageMapper {
    /**
     * 插入消息
     * @param chatMessage 消息信息
     * @return 影响行数
     */
    int insert(ChatMessage chatMessage);
    
    /**
     * 更新消息
     * @param chatMessage 消息信息
     * @return 影响行数
     */
    int update(ChatMessage chatMessage);
    
    /**
     * 根据ID查询消息
     * @param id 消息ID
     * @return 消息信息
     */
    ChatMessage selectById(@Param("id") Long id);
    
    /**
     * 查询会话的消息列表
     * @param sessionId 会话ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 消息列表
     */
    List<ChatMessage> selectBySessionId(@Param("sessionId") Long sessionId, @Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 查询用户的未读消息
     * @param receiverId 接收者ID
     * @return 消息列表
     */
    List<ChatMessage> selectUnreadByReceiverId(@Param("receiverId") Long receiverId);
    
    /**
     * 标记消息为已读
     * @param sessionId 会话ID
     * @param receiverId 接收者ID
     * @return 影响行数
     */
    int markAsRead(@Param("sessionId") Long sessionId, @Param("receiverId") Long receiverId);
    
    /**
     * 统计会话的消息数
     * @param sessionId 会话ID
     * @return 消息数量
     */
    int countBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * 统计用户的未读消息数
     * @param receiverId 接收者ID
     * @return 未读消息数量
     */
    int countUnreadByReceiverId(@Param("receiverId") Long receiverId);
    
    /**
     * 删除会话的所有消息
     * @param sessionId 会话ID
     * @return 影响行数
     */
    int deleteBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * 删除消息
     * @param id 消息ID
     * @return 影响行数
     */
    int delete(@Param("id") Long id);
}
