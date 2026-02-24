package com.cool.server.controller.client;

import com.cool.pojo.dto.MessageSendDTO;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.vo.MessageVO;
import com.cool.pojo.vo.PageVO;
import com.cool.server.annotation.RequireLogin;
import com.cool.server.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "消息接口", description = "消息发送、列表、已读")
@RestController
@RequestMapping("/message")
public class MessageController {
    
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(summary = "发送消息", description = "发送私信给其他用户", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @PostMapping("/send")
    public Long sendMessage(@RequestBody MessageSendDTO dto) {
        return messageService.sendMessage(dto);
    }

    @Operation(summary = "消息列表", description = "获取当前用户的消息列表", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @GetMapping("/list")
    public PageVO<MessageVO> getMessageList(PageQueryDTO pageQueryDTO) {
        return messageService.getMessageList(pageQueryDTO);
    }

    @Operation(summary = "未读数量", description = "获取当前用户的未读消息数量", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @GetMapping("/unread")
    public Long getUnreadCount() {
        return messageService.getUnreadCount();
    }

    @Operation(summary = "标记已读", description = "标记指定消息为已读", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @PostMapping("/read/{id}")
    public void markAsRead(@Parameter(description = "消息ID") @PathVariable Long id) {
        messageService.markAsRead(id);
    }

    @Operation(summary = "全部已读", description = "标记所有消息为已读", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @PostMapping("/read/all")
    public void markAllAsRead() {
        messageService.markAllAsRead();
    }

    @Operation(summary = "删除消息", description = "删除指定消息", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @DeleteMapping("/delete/{id}")
    public void deleteMessage(@Parameter(description = "消息ID") @PathVariable Long id) {
        messageService.deleteMessage(id);
    }

    @Operation(summary = "会话列表", description = "获取当前用户的会话列表", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @GetMapping("/conversations")
    public List<MessageVO> getConversationList() {
        return messageService.getConversationList();
    }

    @Operation(summary = "会话消息", description = "获取与指定用户的聊天记录", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @GetMapping("/conversation/{toUserId}")
    public PageVO<MessageVO> getConversation(
            @Parameter(description = "对方用户ID") @PathVariable Long toUserId, 
            PageQueryDTO pageQueryDTO) {
        return messageService.getConversation(toUserId, pageQueryDTO);
    }

    @Operation(summary = "会话已读", description = "标记与指定用户的会话消息为已读", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @PostMapping("/conversation/read/{toUserId}")
    public void markConversationAsRead(@Parameter(description = "对方用户ID") @PathVariable Long toUserId) {
        messageService.markConversationAsRead(toUserId);
    }
}
