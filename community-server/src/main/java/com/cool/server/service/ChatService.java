package com.cool.server.service;

import com.cool.pojo.dto.MessageSendDTO;
import com.cool.pojo.vo.ChatSessionVO;
import com.cool.pojo.vo.ChatMessageVO;
import com.cool.pojo.vo.PageVO;
import java.util.List;

/**
 * 聊天服务接口
 */
public interface ChatService {
    /**
     * 获取用户的聊天列表
     * @return 聊天会话列表
     */
    List<ChatSessionVO> getChatList();
    
    /**
     * 获取两个用户之间的会话
     * @param targetUserId 目标用户ID
     * @return 会话信息
     */
    ChatSessionVO getOrCreateSession(Long targetUserId);
    
    /**
     * 获取会话的消息列表
     * @param sessionId 会话ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 消息列表
     */
    PageVO<ChatMessageVO> getSessionMessages(Long sessionId, Integer page, Integer pageSize);
    
    /**
     * 发送消息
     * @param dto 消息发送DTO
     * @return 消息ID
     */
    Long sendMessage(MessageSendDTO dto);
    
    /**
     * 标记会话消息为已读
     * @param sessionId 会话ID
     */
    void markSessionAsRead(Long sessionId);
    
    /**
     * 获取用户的未读消息总数
     * @return 未读消息数
     */
    Integer getUnreadCount();
    
    /**
     * 删除会话
     * @param sessionId 会话ID
     */
    void deleteSession(Long sessionId);
    
    /**
     * 关闭会话
     * @param sessionId 会话ID
     */
    void closeSession(Long sessionId);
}
