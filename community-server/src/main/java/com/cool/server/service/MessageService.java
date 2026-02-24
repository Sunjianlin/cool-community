package com.cool.server.service;

import com.cool.pojo.dto.MessageSendDTO;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.vo.MessageVO;
import com.cool.pojo.vo.PageVO;
import java.util.List;

public interface MessageService {
    
    Long sendMessage(MessageSendDTO dto);
    
    PageVO<MessageVO> getMessageList(PageQueryDTO pageQueryDTO);
    
    Long getUnreadCount();
    
    void markAsRead(Long id);
    
    void markAllAsRead();
    
    void deleteMessage(Long id);
    
    List<MessageVO> getConversationList();
    
    PageVO<MessageVO> getConversation(Long toUserId, PageQueryDTO pageQueryDTO);
    
    void markConversationAsRead(Long toUserId);
}
