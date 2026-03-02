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
    
    // 获取在线用户总数
    long getOnlineUserCount();
    
    // 获取不同状态的在线用户数
    java.util.Map<OnlineStatus, Long> getOnlineUserCountByStatus();
    
    // 获取最近在线的用户
    java.util.List<com.cool.pojo.vo.UserVO> getRecentOnlineUsers(int limit);
}
