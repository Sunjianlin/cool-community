package com.cool.server.service.impl;

import com.cool.pojo.dto.MessageSendDTO;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.entity.Message;
import com.cool.pojo.vo.MessageVO;
import com.cool.pojo.vo.PageVO;
import com.cool.server.context.BaseContext;
import com.cool.server.mapper.MessageMapper;
import com.cool.server.service.MessageService;
import cn.hutool.core.bean.BeanUtil;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    
    private final MessageMapper messageMapper;

    public MessageServiceImpl(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    @Override
    public Long sendMessage(MessageSendDTO messageSendDTO) {
        Long userId = BaseContext.getCurrentId();
        
        Message message = new Message();
        BeanUtil.copyProperties(messageSendDTO, message);
        message.setFromUserId(userId);
        message.setIsRead(0);
        
        messageMapper.insert(message);
        return message.getId();
    }

    @Override
    public PageVO<MessageVO> getMessageList(PageQueryDTO pageQueryDTO) {
        Long userId = BaseContext.getCurrentId();
        Long total = messageMapper.count(userId);
        List<MessageVO> messages = messageMapper.list(userId, pageQueryDTO);
        return PageVO.of(messages, total, pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
    }

    @Override
    public Long getUnreadCount() {
        Long userId = BaseContext.getCurrentId();
        return messageMapper.countUnread(userId);
    }

    @Override
    public void markAsRead(Long id) {
        messageMapper.markAsRead(id);
    }

    @Override
    public void markAllAsRead() {
        Long userId = BaseContext.getCurrentId();
        messageMapper.markAllAsRead(userId);
    }

    @Override
    public void deleteMessage(Long id) {
        messageMapper.deleteById(id);
    }
}
