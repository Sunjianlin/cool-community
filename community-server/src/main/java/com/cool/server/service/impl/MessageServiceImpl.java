package com.cool.server.service.impl;

import com.cool.common.constant.MessageConstant;
import com.cool.pojo.dto.MessageSendDTO;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.entity.Message;
import com.cool.pojo.vo.MessageVO;
import com.cool.pojo.vo.PageVO;
import com.cool.server.context.BaseContext;
import com.cool.server.mapper.MessageMapper;
import com.cool.server.mapper.UserMapper;
import com.cool.server.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {
    
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    public MessageServiceImpl(MessageMapper messageMapper, UserMapper userMapper) {
        this.messageMapper = messageMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Long sendMessage(MessageSendDTO dto) {
        Long fromUserId = BaseContext.getCurrentId();
        
        if (fromUserId.equals(dto.getToUserId())) {
            throw new RuntimeException("不能给自己发送消息");
        }
        
        if (userMapper.getById(dto.getToUserId()) == null) {
            throw new RuntimeException(MessageConstant.USER_NOT_FOUND);
        }
        
        Message message = new Message();
        message.setFromUserId(fromUserId);
        message.setToUserId(dto.getToUserId());
        message.setType(dto.getType() != null ? dto.getType() : 0);
        message.setContent(dto.getContent());
        message.setRelatedId(dto.getRelatedId());
        
        messageMapper.insert(message);
        return message.getId();
    }

    @Override
    public PageVO<MessageVO> getMessageList(PageQueryDTO pageQueryDTO) {
        Long userId = BaseContext.getCurrentId();
        int offset = (pageQueryDTO.getPage() - 1) * pageQueryDTO.getPageSize();
        
        Long total = messageMapper.count(userId);
        List<MessageVO> messages = messageMapper.list(userId, offset, pageQueryDTO.getPageSize());
        
        return PageVO.of(messages, total, pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
    }

    @Override
    public Long getUnreadCount() {
        Long userId = BaseContext.getCurrentId();
        return messageMapper.countUnread(userId);
    }

    @Override
    public void markAsRead(Long id) {
        MessageVO message = messageMapper.getById(id);
        if (message == null) {
            throw new RuntimeException(MessageConstant.DATA_NOT_FOUND);
        }
        
        Long userId = BaseContext.getCurrentId();
        if (!message.getToUserId().equals(userId)) {
            throw new RuntimeException(MessageConstant.NO_PERMISSION);
        }
        
        messageMapper.markAsRead(id);
    }

    @Override
    public void markAllAsRead() {
        Long userId = BaseContext.getCurrentId();
        messageMapper.markAllAsRead(userId);
    }

    @Override
    public void deleteMessage(Long id) {
        MessageVO message = messageMapper.getById(id);
        if (message == null) {
            throw new RuntimeException(MessageConstant.DATA_NOT_FOUND);
        }
        
        Long userId = BaseContext.getCurrentId();
        if (!message.getToUserId().equals(userId) && !message.getFromUserId().equals(userId)) {
            throw new RuntimeException(MessageConstant.NO_PERMISSION);
        }
        
        messageMapper.deleteById(id);
    }

    @Override
    public List<MessageVO> getConversationList() {
        Long userId = BaseContext.getCurrentId();
        return messageMapper.getConversationList(userId);
    }

    @Override
    public PageVO<MessageVO> getConversation(Long toUserId, PageQueryDTO pageQueryDTO) {
        Long userId = BaseContext.getCurrentId();
        int offset = (pageQueryDTO.getPage() - 1) * pageQueryDTO.getPageSize();
        
        List<MessageVO> messages = messageMapper.getConversation(userId, toUserId, offset, pageQueryDTO.getPageSize());
        Long total = messageMapper.countConversation(userId, toUserId);
        
        return PageVO.of(messages, total, pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
    }

    @Override
    public void markConversationAsRead(Long toUserId) {
        Long userId = BaseContext.getCurrentId();
        messageMapper.markAllAsRead(userId);
    }
}
