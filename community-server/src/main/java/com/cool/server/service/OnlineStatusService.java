package com.cool.server.service;

import com.cool.common.enumeration.OnlineStatus;
import java.util.List;

public interface OnlineStatusService {
    // 更新用户在线状态
    void updateStatus(Long userId, OnlineStatus status);
    
    // 处理用户心跳
    void handleHeartbeat(Long userId);
    
    // 获取用户在线状态
    OnlineStatus getStatus(Long userId);
    
    // 获取所有在线用户
    List<Long> getOnlineUsers();
    
    // 检查用户是否在线
    boolean isOnline(Long userId);
    
    // 用户离线处理
    void handleOffline(Long userId);
}
