package com.cool.server.service;

import com.cool.pojo.dto.MessageSendDTO;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.vo.MessageVO;
import com.cool.pojo.vo.PageVO;

public interface MessageService {
    
    Long sendMessage(MessageSendDTO messageSendDTO);

    PageVO<MessageVO> getMessageList(PageQueryDTO pageQueryDTO);

    Long getUnreadCount();

    void markAsRead(Long id);

    void markAllAsRead();

    void deleteMessage(Long id);
}
