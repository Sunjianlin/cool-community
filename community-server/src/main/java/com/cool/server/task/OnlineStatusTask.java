package com.cool.server.task;

import com.cool.server.service.impl.OnlineStatusServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

@Component
public class OnlineStatusTask {
    
    private static final Logger log = LoggerFactory.getLogger(OnlineStatusTask.class);
    
    @Autowired
    private OnlineStatusServiceImpl onlineStatusService;
    
    // 每5秒检查一次心跳
    @Scheduled(fixedRate = 5000)
    public void checkHeartbeat() {
        try {
            log.debug("开始检查用户心跳状态");
            onlineStatusService.checkHeartbeats();
            log.debug("用户心跳状态检查完成");
        } catch (Exception e) {
            log.error("检查用户心跳失败: {}", e.getMessage());
        }
    }
}
