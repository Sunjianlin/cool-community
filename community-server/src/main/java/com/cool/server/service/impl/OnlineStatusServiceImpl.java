package com.cool.server.service.impl;

import com.cool.common.enumeration.OnlineStatus;
import com.cool.server.service.OnlineStatusService;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.cool.server.mapper.UserMapper;
import com.cool.pojo.vo.UserVO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OnlineStatusServiceImpl implements OnlineStatusService {
    
    private static final Logger log = LoggerFactory.getLogger(OnlineStatusServiceImpl.class);
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private UserMapper userMapper;
    
    // Redis键前缀
    private static final String ONLINE_STATUS_PREFIX = "user:online:status:";
    private static final String ONLINE_USER_SET = "user:online:set";
    
    // 心跳超时时间（秒）
    private static final int HEARTBEAT_TIMEOUT = 65;
    
    // 日期时间格式化器
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
    
    @Override
    public void updateStatus(Long userId, OnlineStatus status) {
        try {
            String statusKey = ONLINE_STATUS_PREFIX + userId;
            HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
            
            // 更新Redis缓存
            Map<String, Object> statusMap = Map.of(
                "status", status.name(),
                "lastActiveTime", LocalDateTime.now().format(formatter),
                "heartbeatTime", LocalDateTime.now().format(formatter)
            );
            hashOps.putAll(statusKey, statusMap);
            
            // 设置过期时间
            redisTemplate.expire(statusKey, HEARTBEAT_TIMEOUT, java.util.concurrent.TimeUnit.SECONDS);
            
            // 更新在线用户集合
            SetOperations<String, Object> setOps = redisTemplate.opsForSet();
            if (status == OnlineStatus.ONLINE || status == OnlineStatus.BUSY || status == OnlineStatus.AWAY) {
                setOps.add(ONLINE_USER_SET, userId);
            } else {
                setOps.remove(ONLINE_USER_SET, userId);
            }
            
            log.info("用户 {} 在线状态更新为: {}", userId, status.getDesc());
        } catch (Exception e) {
            log.error("更新用户在线状态失败: {}", e.getMessage());
        }
    }
    
    @Override
    public void handleHeartbeat(Long userId) {
        try {
            String statusKey = ONLINE_STATUS_PREFIX + userId;
            HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
            
            // 更新心跳时间
            String currentTime = LocalDateTime.now().format(formatter);
            hashOps.put(statusKey, "heartbeatTime", currentTime);
            hashOps.put(statusKey, "lastActiveTime", currentTime);
            
            // 重置过期时间
            redisTemplate.expire(statusKey, HEARTBEAT_TIMEOUT, java.util.concurrent.TimeUnit.SECONDS);
            
            // 确保用户在在线集合中
            SetOperations<String, Object> setOps = redisTemplate.opsForSet();
            setOps.add(ONLINE_USER_SET, userId);
            
            log.debug("用户 {} 心跳更新", userId);
        } catch (Exception e) {
            log.error("处理用户心跳失败: {}", e.getMessage());
        }
    }
    
    @Override
    public OnlineStatus getStatus(Long userId) {
        try {
            String statusKey = ONLINE_STATUS_PREFIX + userId;
            HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
            
            // 检查缓存是否存在
            if (!redisTemplate.hasKey(statusKey)) {
                return OnlineStatus.OFFLINE;
            }
            
            // 获取状态
            Object statusObj = hashOps.get(statusKey, "status");
            if (statusObj == null) {
                return OnlineStatus.OFFLINE;
            }
            
            // 检查心跳是否超时
            Object heartbeatTimeObj = hashOps.get(statusKey, "heartbeatTime");
            if (heartbeatTimeObj != null) {
                LocalDateTime heartbeatTime = LocalDateTime.parse(heartbeatTimeObj.toString(), formatter);
                if (LocalDateTime.now().minusSeconds(HEARTBEAT_TIMEOUT).isAfter(heartbeatTime)) {
                    // 心跳超时，标记为离线
                    handleOffline(userId);
                    return OnlineStatus.OFFLINE;
                }
            }
            
            // 解析状态
            String statusStr = statusObj.toString();
            return OnlineStatus.valueOf(statusStr);
        } catch (Exception e) {
            log.error("获取用户在线状态失败: {}", e.getMessage());
            return OnlineStatus.OFFLINE;
        }
    }
    
    @Override
    public List<Long> getOnlineUsers() {
        try {
            SetOperations<String, Object> setOps = redisTemplate.opsForSet();
            Set<Object> onlineUsers = setOps.members(ONLINE_USER_SET);
            
            // 过滤掉已过期的用户
            return onlineUsers.stream()
                .map(obj -> Long.parseLong(obj.toString()))
                .filter(this::isOnline)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取在线用户列表失败: {}", e.getMessage());
            return List.of();
        }
    }
    
    @Override
    public boolean isOnline(Long userId) {
        OnlineStatus status = getStatus(userId);
        return status != OnlineStatus.OFFLINE;
    }
    
    @Override
    public void handleOffline(Long userId) {
        try {
            String statusKey = ONLINE_STATUS_PREFIX + userId;
            
            // 从Redis中删除状态
            redisTemplate.delete(statusKey);
            
            // 从在线集合中移除
            SetOperations<String, Object> setOps = redisTemplate.opsForSet();
            setOps.remove(ONLINE_USER_SET, userId);
            
            log.info("用户 {} 标记为离线", userId);
        } catch (Exception e) {
            log.error("处理用户离线失败: {}", e.getMessage());
        }
    }
    
    // 检查所有在线用户的心跳，超时则标记为离线
    public void checkHeartbeats() {
        try {
            SetOperations<String, Object> setOps = redisTemplate.opsForSet();
            Set<Object> onlineUsers = setOps.members(ONLINE_USER_SET);
            
            for (Object obj : onlineUsers) {
                Long userId = Long.parseLong(obj.toString());
                OnlineStatus status = getStatus(userId);
                if (status == OnlineStatus.OFFLINE) {
                    // 心跳超时，从在线集合中移除
                    setOps.remove(ONLINE_USER_SET, userId);
                }
            }
        } catch (Exception e) {
            log.error("检查用户心跳失败: {}", e.getMessage());
        }
    }

    @Override
    public long getOnlineUserCount() {
        try {
            List<Long> onlineUsers = getOnlineUsers();
            return onlineUsers.size();
        } catch (Exception e) {
            log.error("获取在线用户总数失败: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public java.util.Map<OnlineStatus, Long> getOnlineUserCountByStatus() {
        try {
            List<Long> onlineUsers = getOnlineUsers();
            java.util.Map<OnlineStatus, Long> statusCounts = new java.util.HashMap<>();
            
            for (Long userId : onlineUsers) {
                OnlineStatus status = getStatus(userId);
                statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
            }
            
            return statusCounts;
        } catch (Exception e) {
            log.error("获取在线用户状态分布失败: {}", e.getMessage());
            return new java.util.HashMap<>();
        }
    }

    @Override
    public java.util.List<com.cool.pojo.vo.UserVO> getRecentOnlineUsers(int limit) {
        try {
            List<Long> onlineUsers = getOnlineUsers();
            java.util.List<com.cool.pojo.vo.UserVO> recentUsers = new java.util.ArrayList<>();
            
            for (Long userId : onlineUsers) {
                try {
                    com.cool.pojo.entity.User user = userMapper.getById(userId);
                    if (user != null) {
                        com.cool.pojo.vo.UserVO userVO = new com.cool.pojo.vo.UserVO();
                        userVO.setId(user.getId());
                        userVO.setUsername(user.getUsername());
                        userVO.setNickname(user.getNickname());
                        userVO.setAvatar(user.getAvatar());
                        userVO.setEmail(user.getEmail());
                        userVO.setPhone(user.getPhone());
                        userVO.setBio(user.getBio());
                        userVO.setGender(user.getGender());
                        userVO.setStatus(user.getStatus());
                        userVO.setRole(user.getRole());
                        userVO.setFollowingCount(userMapper.getFollowingCount(userId));
                        userVO.setFollowerCount(userMapper.getFollowerCount(userId));
                        recentUsers.add(userVO);
                    }
                } catch (Exception e) {
                    log.error("获取用户信息失败: {}", e.getMessage());
                }
                
                if (recentUsers.size() >= limit) {
                    break;
                }
            }
            
            return recentUsers;
        } catch (Exception e) {
            log.error("获取最近在线用户失败: {}", e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
}
