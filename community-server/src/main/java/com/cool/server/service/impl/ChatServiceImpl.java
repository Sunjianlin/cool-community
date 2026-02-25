package com.cool.server.service.impl;

import com.cool.pojo.dto.MessageSendDTO;
import com.cool.pojo.entity.ChatMessage;
import com.cool.pojo.entity.ChatSession;
import com.cool.pojo.vo.ChatMessageVO;
import com.cool.pojo.vo.ChatSessionVO;
import com.cool.pojo.vo.PageVO;
import com.cool.server.context.BaseContext;
import com.cool.server.mapper.ChatMessageMapper;
import com.cool.server.mapper.ChatSessionMapper;
import com.cool.server.mapper.UserMapper;
import com.cool.server.service.ChatService;
import com.cool.server.websocket.ChatWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天服务实现类
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {
    
    @Autowired
    private ChatSessionMapper chatSessionMapper;
    
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;
    
    @Override
    public List<ChatSessionVO> getChatList() {
        Long userId = BaseContext.getCurrentId();
        List<ChatSession> sessions = chatSessionMapper.selectByUserId(userId);
        List<ChatSessionVO> result = new ArrayList<>();
        
        for (ChatSession session : sessions) {
            ChatSessionVO vo = new ChatSessionVO();
            vo.setId(session.getId());
            
            // 确定对方用户ID
            Long otherUserId = session.getUserId1().equals(userId) ? session.getUserId2() : session.getUserId1();
            vo.setUserId(otherUserId);
            
            // 获取对方用户信息
            var user = userMapper.getById(otherUserId);
            if (user != null) {
                vo.setName(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }
            
            vo.setLastMessage(session.getLastMessage());
            vo.setLastMessageTime(session.getLastMessageTime());
            
            // 确定未读消息数
            if (session.getUserId1().equals(userId)) {
                vo.setUnreadCount(session.getUnreadCountUser1());
            } else {
                vo.setUnreadCount(session.getUnreadCountUser2());
            }
            
            vo.setStatus(session.getStatus());
            vo.setIsFollowing(false); // 暂时设置为false，后续可以通过其他方式实现
            
            result.add(vo);
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public ChatSessionVO getOrCreateSession(Long targetUserId) {
        Long userId = BaseContext.getCurrentId();
        
        // 查找现有会话
        ChatSession session = chatSessionMapper.selectByUserIds(userId, targetUserId);
        if (session == null) {
            session = chatSessionMapper.selectByUserIds(targetUserId, userId);
        }
        
        // 如果会话不存在，创建新会话
        if (session == null) {
            session = new ChatSession();
            session.setUserId1(userId);
            session.setUserId2(targetUserId);
            session.setUnreadCountUser1(0);
            session.setUnreadCountUser2(0);
            session.setStatus(1);
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());
            chatSessionMapper.insert(session);
        }
        
        // 构建VO
        ChatSessionVO vo = new ChatSessionVO();
        vo.setId(session.getId());
        vo.setUserId(targetUserId);
        
        // 获取对方用户信息
            var user = userMapper.getById(targetUserId);
            if (user != null) {
                vo.setName(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }
        
        vo.setLastMessage(session.getLastMessage());
        vo.setLastMessageTime(session.getLastMessageTime());
        
        // 确定未读消息数
        if (session.getUserId1().equals(userId)) {
            vo.setUnreadCount(session.getUnreadCountUser1());
        } else {
            vo.setUnreadCount(session.getUnreadCountUser2());
        }
        
        vo.setStatus(session.getStatus());
        vo.setIsFollowing(false); // 暂时设置为false，后续可以通过其他方式实现
        
        return vo;
    }
    
    @Override
    public PageVO<ChatMessageVO> getSessionMessages(Long sessionId, Integer page, Integer pageSize) {
        Long userId = BaseContext.getCurrentId();
        
        // 验证会话是否属于当前用户
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || (!session.getUserId1().equals(userId) && !session.getUserId2().equals(userId))) {
            throw new RuntimeException("会话不存在或无权访问");
        }
        
        // 计算偏移量
        int offset = (page - 1) * pageSize;
        
        // 查询消息
        List<ChatMessage> messages = chatMessageMapper.selectBySessionId(sessionId, offset, pageSize);
        int total = chatMessageMapper.countBySessionId(sessionId);
        
        // 构建VO列表
        List<ChatMessageVO> records = new ArrayList<>();
        for (ChatMessage message : messages) {
            ChatMessageVO vo = new ChatMessageVO();
            vo.setId(message.getId());
            vo.setSessionId(message.getSessionId());
            vo.setSenderId(message.getSenderId());
            vo.setReceiverId(message.getReceiverId());
            vo.setContent(message.getContent());
            vo.setType(message.getType());
            vo.setIsRead(message.getIsRead());
            vo.setStatus(message.getStatus());
            vo.setCreateTime(message.getCreateTime());
            vo.setIsMine(message.getSenderId().equals(userId));
            
            // 获取发送者信息
            var sender = userMapper.getById(message.getSenderId());
            if (sender != null) {
                vo.setSenderName(sender.getNickname());
                vo.setSenderAvatar(sender.getAvatar());
            }
            
            records.add(vo);
        }
        
        // 构建分页VO
        PageVO<ChatMessageVO> pageVO = PageVO.of(records, Long.valueOf(total), page, pageSize);
        
        // 标记消息为已读
        markSessionAsRead(sessionId);
        
        return pageVO;
    }
    
    @Override
    @Transactional
    public Long sendMessage(MessageSendDTO dto) {
        Long userId = BaseContext.getCurrentId();
        Long targetUserId = dto.getToUserId();
        String content = dto.getContent();
        
        // 获取或创建会话
        ChatSession session = chatSessionMapper.selectByUserIds(userId, targetUserId);
        if (session == null) {
            session = chatSessionMapper.selectByUserIds(targetUserId, userId);
        }
        
        if (session == null) {
            session = new ChatSession();
            session.setUserId1(userId);
            session.setUserId2(targetUserId);
            session.setUnreadCountUser1(0);
            session.setUnreadCountUser2(0);
            session.setStatus(1);
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());
            chatSessionMapper.insert(session);
        }
        
        // 创建消息
        ChatMessage message = new ChatMessage();
        message.setSessionId(session.getId());
        message.setSenderId(userId);
        message.setReceiverId(targetUserId);
        message.setContent(content);
        message.setType(1); // 默认文本消息
        message.setIsRead(0);
        message.setStatus(1);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        
        chatMessageMapper.insert(message);
        
        // 更新会话的最后消息
        session.setLastMessage(content);
        session.setLastMessageTime(LocalDateTime.now());
        
        // 更新未读消息数
        if (session.getUserId1().equals(userId)) {
            session.setUnreadCountUser2(session.getUnreadCountUser2() + 1);
        } else {
            session.setUnreadCountUser1(session.getUnreadCountUser1() + 1);
        }
        
        chatSessionMapper.update(session);
        
        // 尝试通过WebSocket发送消息
        try {
            // 构建WebSocket消息
            java.util.Map<String, Object> wsMessage = new java.util.HashMap<>();
            wsMessage.put("type", "message");
            wsMessage.put("fromUserId", userId);
            wsMessage.put("content", content);
            wsMessage.put("time", System.currentTimeMillis());
            wsMessage.put("messageId", message.getId());
            
            // 发送消息给目标用户
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            chatWebSocketHandler.sendMessageToUser(targetUserId, mapper.writeValueAsString(wsMessage));
        } catch (Exception e) {
            log.error("WebSocket消息发送失败: {}", e.getMessage());
        }
        
        return message.getId();
    }
    
    @Override
    @Transactional
    public void markSessionAsRead(Long sessionId) {
        Long userId = BaseContext.getCurrentId();
        ChatSession session = chatSessionMapper.selectById(sessionId);
        
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        
        // 标记消息为已读
        chatMessageMapper.markAsRead(sessionId, userId);
        
        // 更新会话的未读消息数
        if (session.getUserId1().equals(userId)) {
            session.setUnreadCountUser1(0);
        } else if (session.getUserId2().equals(userId)) {
            session.setUnreadCountUser2(0);
        }
        
        chatSessionMapper.update(session);
    }
    
    @Override
    public Integer getUnreadCount() {
        Long userId = BaseContext.getCurrentId();
        return chatMessageMapper.countUnreadByReceiverId(userId);
    }
    
    @Override
    @Transactional
    public void deleteSession(Long sessionId) {
        Long userId = BaseContext.getCurrentId();
        ChatSession session = chatSessionMapper.selectById(sessionId);
        
        if (session == null || (!session.getUserId1().equals(userId) && !session.getUserId2().equals(userId))) {
            throw new RuntimeException("会话不存在或无权访问");
        }
        
        // 删除会话的所有消息
        chatMessageMapper.deleteBySessionId(sessionId);
        
        // 删除会话
        chatSessionMapper.delete(sessionId);
    }
    
    @Override
    public void closeSession(Long sessionId) {
        Long userId = BaseContext.getCurrentId();
        ChatSession session = chatSessionMapper.selectById(sessionId);
        
        if (session == null || (!session.getUserId1().equals(userId) && !session.getUserId2().equals(userId))) {
            throw new RuntimeException("会话不存在或无权访问");
        }
        
        // 更新会话状态为已关闭
        session.setStatus(0);
        chatSessionMapper.update(session);
    }
}
