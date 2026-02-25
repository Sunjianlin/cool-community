# 用户在线状态功能实现方案

## 1. 数据库设计

### 1.1 用户表修改

需要在用户表中添加在线状态相关字段：

```sql
-- 添加用户在线状态字段
ALTER TABLE `user` ADD COLUMN `online_status` TINYINT(1) DEFAULT 0 COMMENT '在线状态：0-离线，1-在线，2-忙碌，3-离开';
ALTER TABLE `user` ADD COLUMN `last_active_time` DATETIME DEFAULT NULL COMMENT '最后活跃时间';
ALTER TABLE `user` ADD COLUMN `heartbeat_time` DATETIME DEFAULT NULL COMMENT '最后心跳时间';
```

### 1.2 索引优化

为了提高查询性能，添加索引：

```sql
-- 添加在线状态索引
CREATE INDEX idx_user_online_status ON `user` (`online_status`);
-- 添加最后活跃时间索引
CREATE INDEX idx_user_last_active_time ON `user` (`last_active_time`);
```

## 2. Redis缓存设计

### 2.1 缓存结构

使用Redis的Hash结构存储用户在线状态：

```
# 用户在线状态缓存
user:online:status:{userId} = {
  "status": "online",  # online, busy, away, offline
  "lastActiveTime": "2026-02-25T20:00:00Z",
  "heartbeatTime": "2026-02-25T20:00:00Z",
  "sessionId": "websocket-session-id"
}

# 在线用户集合（用于快速获取所有在线用户）
user:online:set = [userId1, userId2, ...]

# 用户状态过期时间：30秒
# 心跳间隔：10秒
```

### 2.2 缓存键设计

| 缓存键 | 类型 | 描述 | 过期时间 |
|--------|------|------|----------|
| `user:online:status:{userId}` | Hash | 用户在线状态详情 | 30秒 |
| `user:online:set` | Set | 在线用户ID集合 | 永久 |

## 3. 后端实现

### 3.1 在线状态服务

创建 `OnlineStatusService` 接口和实现：

```java
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
```

### 3.2 心跳检测机制

1. **前端实现**：
   - 每10秒发送一次心跳请求
   - WebSocket连接时自动开始心跳

2. **后端实现**：
   - 接收心跳请求，更新Redis缓存
   - 定时任务（每5秒）检查心跳时间，超时则标记为离线

### 3.3 WebSocket集成

在WebSocket连接和断开时更新用户状态：

```java
// WebSocket连接建立时
@Override
public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    // 获取用户ID
    Long userId = getUserIdFromSession(session);
    // 更新状态为在线
    onlineStatusService.updateStatus(userId, OnlineStatus.ONLINE);
    // 存储会话信息
    sessionMap.put(userId, session);
}

// WebSocket连接关闭时
@Override
public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    // 获取用户ID
    Long userId = getUserIdFromSession(session);
    // 更新状态为离线
    onlineStatusService.handleOffline(userId);
    // 移除会话
    sessionMap.remove(userId);
}
```

## 4. 前端实现

### 4.1 心跳服务

创建 `heartbeatService.js`：

```javascript
class HeartbeatService {
  constructor() {
    this.interval = null;
    this.heartbeatInterval = 10000; // 10秒
  }

  start(userId) {
    this.stop();
    this.interval = setInterval(() => {
      this.sendHeartbeat(userId);
    }, this.heartbeatInterval);
  }

  async sendHeartbeat(userId) {
    try {
      await api.sendHeartbeat(userId);
    } catch (error) {
      console.error('心跳发送失败:', error);
    }
  }

  stop() {
    if (this.interval) {
      clearInterval(this.interval);
      this.interval = null;
    }
  }
}

export default new HeartbeatService();
```

### 4.2 在线状态组件

创建在线状态显示组件：

```vue
<template>
  <span class="online-status" :class="statusClass">
    {{ statusText }}
  </span>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  status: {
    type: Number,
    default: 0
  }
});

const statusClass = computed(() => {
  const classes = {
    0: 'offline',
    1: 'online',
    2: 'busy',
    3: 'away'
  };
  return classes[props.status] || 'offline';
});

const statusText = computed(() => {
  const texts = {
    0: '离线',
    1: '在线',
    2: '忙碌',
    3: '离开'
  };
  return texts[props.status] || '离线';
});
</script>

<style scoped>
.online-status {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

.online {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.busy {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.away {
  background-color: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.offline {
  background-color: #e2e3e5;
  color: #383d41;
  border: 1px solid #d6d8db;
}
</style>
```

## 5. API接口设计

### 5.1 心跳接口

```java
@RestController
@RequestMapping("/api/heartbeat")
public class HeartbeatController {
    
    @Autowired
    private OnlineStatusService onlineStatusService;
    
    @PostMapping
    public ResponseEntity<Void> sendHeartbeat() {
        Long userId = BaseContext.getCurrentId();
        onlineStatusService.handleHeartbeat(userId);
        return ResponseEntity.ok().build();
    }
}
```

### 5.2 在线状态接口

```java
@RestController
@RequestMapping("/api/online-status")
public class OnlineStatusController {
    
    @Autowired
    private OnlineStatusService onlineStatusService;
    
    @GetMapping("/{userId}")
    public ResponseEntity<OnlineStatusDTO> getStatus(@PathVariable Long userId) {
        OnlineStatus status = onlineStatusService.getStatus(userId);
        return ResponseEntity.ok(new OnlineStatusDTO(userId, status));
    }
    
    @PutMapping
    public ResponseEntity<Void> updateStatus(@RequestBody UpdateStatusDTO dto) {
        Long userId = BaseContext.getCurrentId();
        onlineStatusService.updateStatus(userId, dto.getStatus());
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/online-users")
    public ResponseEntity<List<Long>> getOnlineUsers() {
        List<Long> onlineUsers = onlineStatusService.getOnlineUsers();
        return ResponseEntity.ok(onlineUsers);
    }
}
```

## 6. 状态枚举

```java
public enum OnlineStatus {
    OFFLINE(0, "离线"),
    ONLINE(1, "在线"),
    BUSY(2, "忙碌"),
    AWAY(3, "离开");
    
    private final int code;
    private final String desc;
    
    // 构造方法、getter方法
    
    public static OnlineStatus getByCode(int code) {
        for (OnlineStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return OFFLINE;
    }
}
```

## 7. 定时任务

```java
@Component
public class OnlineStatusTask {
    
    @Autowired
    private OnlineStatusService onlineStatusService;
    
    @Scheduled(fixedRate = 5000) // 每5秒执行一次
    public void checkHeartbeat() {
        // 检查所有在线用户的心跳时间
        // 超过30秒未心跳则标记为离线
    }
}
```

## 8. 集成到现有系统

1. **修改用户服务**：在用户登录和登出时更新在线状态
2. **修改WebSocket处理器**：集成在线状态管理
3. **前端集成**：在聊天界面显示用户在线状态
4. **API文档更新**：添加在线状态相关接口文档

## 9. 性能考虑

1. **Redis缓存**：使用Redis的Hash结构和Set结构，提高查询性能
2. **批量操作**：获取在线用户时使用Redis的批量命令
3. **定时任务优化**：使用分片处理，避免一次性处理过多用户
4. **连接池**：使用Redis连接池，减少连接开销

## 10. 测试方案

1. **功能测试**：
   - 在线状态切换
   - 心跳检测
   - 离线处理
   - 在线用户列表

2. **性能测试**：
   - 1000用户同时在线
   - 心跳并发测试
   - 在线状态查询性能

3. **可靠性测试**：
   - 网络中断恢复
   - Redis故障恢复
   - 服务器重启恢复

## 11. 部署注意事项

1. **Redis配置**：确保Redis服务稳定运行
2. **定时任务**：调整定时任务频率，根据实际用户量
3. **监控**：添加在线状态相关监控指标
4. **日志**：记录在线状态变更日志，便于排查问题
