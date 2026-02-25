package com.cool.server.controller.client;

import com.cool.pojo.dto.MessageSendDTO;
import com.cool.pojo.vo.ChatMessageVO;
import com.cool.pojo.vo.ChatSessionVO;
import com.cool.pojo.vo.PageVO;
import com.cool.server.annotation.RequireLogin;
import com.cool.server.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 聊天控制器
 */
@Tag(name = "聊天接口", description = "私信聊天功能")
@RestController
@RequestMapping("/chat")
public class ChatController {
    
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(summary = "聊天列表", description = "获取当前用户的聊天列表", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @GetMapping("/list")
    public List<ChatSessionVO> getChatList() {
        return chatService.getChatList();
    }

    @Operation(summary = "获取或创建会话", description = "获取或创建与指定用户的会话", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @GetMapping("/session/{targetUserId}")
    public ChatSessionVO getOrCreateSession(
            @Parameter(description = "目标用户ID") @PathVariable Long targetUserId) {
        return chatService.getOrCreateSession(targetUserId);
    }

    @Operation(summary = "会话消息", description = "获取会话的消息列表", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @GetMapping("/messages/{sessionId}")
    public PageVO<ChatMessageVO> getSessionMessages(
            @Parameter(description = "会话ID") @PathVariable Long sessionId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "50") Integer pageSize) {
        return chatService.getSessionMessages(sessionId, page, pageSize);
    }

    @Operation(summary = "发送消息", description = "发送消息到指定会话", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @PostMapping("/send")
    public Long sendMessage(@RequestBody MessageSendDTO dto) {
        return chatService.sendMessage(dto);
    }

    @Operation(summary = "标记已读", description = "标记会话消息为已读", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @PostMapping("/read/{sessionId}")
    public void markSessionAsRead(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        chatService.markSessionAsRead(sessionId);
    }

    @Operation(summary = "未读消息数", description = "获取当前用户的未读消息总数", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @GetMapping("/unread")
    public Integer getUnreadCount() {
        return chatService.getUnreadCount();
    }

    @Operation(summary = "删除会话", description = "删除指定会话", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @DeleteMapping("/session/{sessionId}")
    public void deleteSession(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        chatService.deleteSession(sessionId);
    }

    @Operation(summary = "关闭会话", description = "关闭指定会话", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @PostMapping("/close/{sessionId}")
    public void closeSession(
            @Parameter(description = "会话ID") @PathVariable Long sessionId) {
        chatService.closeSession(sessionId);
    }
}
