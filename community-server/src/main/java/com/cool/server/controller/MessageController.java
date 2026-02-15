package com.cool.server.controller;

import com.cool.pojo.dto.MessageSendDTO;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.vo.MessageVO;
import com.cool.pojo.vo.PageVO;
import com.cool.server.annotation.RequireLogin;
import com.cool.server.service.MessageService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
public class MessageController {
    
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    @RequireLogin
    public Long sendMessage(@RequestBody MessageSendDTO messageSendDTO) {
        return messageService.sendMessage(messageSendDTO);
    }

    @GetMapping("/list")
    @RequireLogin
    public PageVO<MessageVO> getMessageList(PageQueryDTO pageQueryDTO) {
        return messageService.getMessageList(pageQueryDTO);
    }

    @GetMapping("/unread")
    @RequireLogin
    public Long getUnreadCount() {
        return messageService.getUnreadCount();
    }

    @PostMapping("/read/{id}")
    @RequireLogin
    public void markAsRead(@PathVariable Long id) {
        messageService.markAsRead(id);
    }

    @PostMapping("/read/all")
    @RequireLogin
    public void markAllAsRead() {
        messageService.markAllAsRead();
    }

    @DeleteMapping("/delete/{id}")
    @RequireLogin
    public void deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
    }
}
