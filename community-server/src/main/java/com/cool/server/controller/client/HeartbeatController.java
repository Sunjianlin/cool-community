package com.cool.server.controller.client;

import com.cool.server.context.BaseContext;
import com.cool.server.service.OnlineStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;

@Tag(name = "心跳接口", description = "用户心跳检测")
@RestController
@RequestMapping("/api/heartbeat")
public class HeartbeatController {
    
    @Autowired
    private OnlineStatusService onlineStatusService;
    
    @Operation(summary = "发送心跳", description = "发送用户心跳，保持在线状态")
    @PostMapping
    public ResponseEntity<Void> sendHeartbeat() {
        Long userId = BaseContext.getCurrentId();
        onlineStatusService.handleHeartbeat(userId);
        return ResponseEntity.ok().build();
    }
}
