package com.cool.server.controller.client;

import com.cool.pojo.vo.NotificationVO;
import com.cool.server.annotation.RequireLogin;
import com.cool.server.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "通知接口", description = "通知相关操作")
@RestController
@RequestMapping("/notification")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Operation(summary = "获取通知列表", description = "获取用户的通知列表", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @GetMapping
    public List<NotificationVO> getNotifications() {
        return notificationService.getNotifications();
    }
    
    @Operation(summary = "标记通知为已读", description = "标记指定通知为已读", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }
    
    @Operation(summary = "标记所有通知为已读", description = "标记所有通知为已读", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @PutMapping("/read-all")
    public void markAllAsRead() {
        notificationService.markAllAsRead();
    }
    
    @Operation(summary = "获取未读通知数量", description = "获取用户的未读通知数量", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @GetMapping("/unread-count")
    public Integer getUnreadCount() {
        return notificationService.getUnreadCount();
    }
}
