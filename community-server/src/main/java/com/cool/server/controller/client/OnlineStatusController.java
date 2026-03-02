package com.cool.server.controller.client;

import com.cool.common.enumeration.OnlineStatus;
import com.cool.server.context.BaseContext;
import com.cool.server.service.OnlineStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Tag(name = "在线状态接口", description = "用户在线状态管理")
@RestController
@RequestMapping("/online-status")
public class OnlineStatusController {
    
    @Autowired
    private OnlineStatusService onlineStatusService;
    
    @Operation(summary = "获取用户在线状态", description = "获取指定用户的在线状态")
    @GetMapping("/{userId}")
    public ResponseEntity<OnlineStatusDTO> getStatus(@Parameter(description = "用户ID") @PathVariable Long userId) {
        OnlineStatus status = onlineStatusService.getStatus(userId);
        return ResponseEntity.ok(new OnlineStatusDTO(userId, status));
    }
    
    @Operation(summary = "更新在线状态", description = "更新当前用户的在线状态")
    @PutMapping
    public ResponseEntity<Void> updateStatus(@RequestBody UpdateStatusDTO dto) {
        Long userId = BaseContext.getCurrentId();
        OnlineStatus status = OnlineStatus.getByCode(dto.getStatus());
        onlineStatusService.updateStatus(userId, status);
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "获取在线用户列表", description = "获取所有在线用户的ID列表")
    @GetMapping("/online-users")
    public ResponseEntity<List<Long>> getOnlineUsers() {
        List<Long> onlineUsers = onlineStatusService.getOnlineUsers();
        return ResponseEntity.ok(onlineUsers);
    }
    
    // 在线状态DTO
    public static class OnlineStatusDTO {
        private Long userId;
        private int status;
        private String statusDesc;
        
        public OnlineStatusDTO(Long userId, OnlineStatus status) {
            this.userId = userId;
            this.status = status.getCode();
            this.statusDesc = status.getDesc();
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public int getStatus() {
            return status;
        }
        
        public String getStatusDesc() {
            return statusDesc;
        }
    }
    
    // 更新状态DTO
    public static class UpdateStatusDTO {
        private int status;
        
        public int getStatus() {
            return status;
        }
        
        public void setStatus(int status) {
            this.status = status;
        }
    }
}
