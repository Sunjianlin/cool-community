# Cool Community 开发文档

## 目录

- [项目概述](#项目概述)
- [快速开始](#快速开始)
- [环境配置](#环境配置)
- [项目结构](#项目结构)
- [核心功能](#核心功能)
- [API 接口](#api-接口)
- [数据库设计](#数据库设计)
- [消息队列设计](#消息队列设计)
- [WebSocket 设计](#websocket-设计)
- [缓存策略](#缓存策略)
- [部署指南](#部署指南)
- [开发规范](#开发规范)

---

## 项目概述

Cool Community 是一个仿酷安社区的数码科技社区平台，采用前后端分离架构，提供帖子发布、话题讨论、产品评测、实时私信、秒杀活动等核心功能。

### 技术栈

#### 后端

| 技术 | 版本 | 说明 |
|-----|------|-----|
| Spring Boot | 3.2.0 | 核心框架 |
| MyBatis | 3.0.3 | ORM框架 |
| MySQL | 8.0.33 | 关系型数据库 |
| Redis | - | 缓存中间件 |
| RabbitMQ | 3.2.0 | 消息队列 |
| Elasticsearch | 8.11.0 | 搜索引擎 |
| Spring WebSocket | - | 实时通信 |
| JJWT | 0.12.3 | JWT认证 |
| Druid | 1.2.20 | 数据库连接池 |
| 阿里云OSS | 3.17.4 | 对象存储 |

#### 前端

| 技术 | 版本 | 说明 |
|-----|------|-----|
| Vue | 3.5.13 | 前端框架 |
| Vite | 6.0.5 | 构建工具 |
| Pinia | 2.3.1 | 状态管理 |
| Vue Router | 4.4.5 | 路由管理 |
| Element Plus | 2.13.2 | UI组件库 |
| Axios | 1.13.5 | HTTP客户端 |

---

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.x
- Elasticsearch 8.x (可选)

### 启动步骤

#### 1. 克隆项目

```bash
git clone https://github.com/your-username/cool-community.git
cd cool-community
```

#### 2. 初始化数据库

```bash
mysql -u root -p < sql/cool.sql
```

#### 3. 配置后端

修改 `community-server/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cool_community
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

#### 4. 启动后端

```bash
cd community-server
mvn spring-boot:run
```

后端服务将在 `http://localhost:8082` 启动。

#### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 `http://localhost:5173` 启动。

---

## 环境配置

### MySQL 配置

```sql
CREATE DATABASE cool_community CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'cool_user'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON cool_community.* TO 'cool_user'@'%';
FLUSH PRIVILEGES;
```

### Redis 配置

```bash
# redis.conf
bind 0.0.0.0
port 6379
requirepass your_password
```

### RabbitMQ 配置

```bash
# 启动 RabbitMQ
rabbitmq-server

# 启用管理界面
rabbitmq-plugins enable rabbitmq_management

# 创建用户
rabbitmqctl add_user cool_user your_password
rabbitmqctl set_user_tags cool_user administrator
rabbitmqctl set_permissions -p / cool_user ".*" ".*" ".*"
```

### Elasticsearch 配置

```bash
# 安装 IK 分词器
./bin/elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v8.11.0/elasticsearch-analysis-ik-8.11.0.zip
```

---

## 项目结构

```
cool-community/
├── community-common/              # 公共模块
│   └── src/main/java/com/cool/common/
│       ├── constant/              # 常量定义
│       │   ├── MessageConstant.java
│       │   ├── RedisConstant.java
│       │   └── RabbitMQNotifyConstants.java
│       ├── enumeration/           # 枚举类
│       ├── exception/             # 异常类
│       │   ├── BaseException.java
│       │   └── BusinessException.java
│       └── utils/                 # 工具类
│
├── community-pojo/                # 实体模块
│   └── src/main/java/com/cool/pojo/
│       ├── document/              # ES 文档类
│       │   ├── PostDocument.java
│       │   ├── UserDocument.java
│       │   └── TopicDocument.java
│       ├── dto/                   # 数据传输对象
│       │   ├── UserLoginDTO.java
│       │   ├── PostCreateDTO.java
│       │   └── SearchDTO.java
│       ├── entity/                # 数据库实体
│       │   ├── User.java
│       │   ├── Post.java
│       │   ├── Comment.java
│       │   ├── Topic.java
│       │   └── notify/            # 通知相关实体
│       └── vo/                    # 视图对象
│           ├── UserVO.java
│           ├── PostVO.java
│           └── SearchResultVO.java
│
├── community-server/              # 服务模块
│   └── src/main/java/com/cool/server/
│       ├── config/                # 配置类
│       │   ├── SecurityConfig.java
│       │   ├── RedisConfig.java
│       │   ├── RabbitMQConfig.java
│       │   ├── WebSocketConfig.java
│       │   └── ElasticsearchConfig.java
│       ├── controller/            # 控制器
│       │   ├── admin/             # 管理端接口
│       │   └── client/            # 客户端接口
│       ├── service/               # 服务层
│       │   ├── impl/              # 服务实现
│       │   ├── notify/            # 通知服务
│       │   ├── producer/          # 消息生产者
│       │   └── consumer/          # 消息消费者
│       ├── mapper/                # MyBatis Mapper
│       ├── repository/            # ES Repository
│       ├── security/              # 安全配置
│       ├── websocket/             # WebSocket 处理
│       ├── aspect/                # AOP 切面
│       ├── annotation/            # 自定义注解
│       └── task/                  # 定时任务
│
├── frontend/                      # 前端项目
│   └── src/
│       ├── api/                   # API 接口
│       │   ├── userApi.js
│       │   ├── postApi.js
│       │   └── searchApi.js
│       ├── components/            # 公共组件
│       ├── store/                 # Pinia 状态管理
│       │   ├── user.js
│       │   └── chat.js
│       ├── views/                 # 页面视图
│       │   ├── HomePage.vue
│       │   ├── LoginPage.vue
│       │   ├── PostDetailPage.vue
│       │   └── SearchPage.vue
│       ├── router/                # 路由配置
│       └── utils/                 # 工具函数
│
└── sql/                           # SQL 脚本
    └── cool.sql
```

---

## 核心功能

### 1. 用户模块

#### 功能列表

- 用户注册/登录
- JWT 双 Token 认证
- 个人信息管理
- 在线状态管理
- 积分系统

#### 关键类

| 类名 | 说明 |
|------|------|
| `UserClientController` | 用户接口控制器 |
| `UserServiceImpl` | 用户业务逻辑 |
| `UserMapper` | 用户数据访问 |
| `JwtTokenUtil` | Token 工具类 |

#### 认证流程

```
用户登录 → 验证账号密码 → 生成 AccessToken + RefreshToken → 返回给前端
    │
    ▼
前端存储 Token (localStorage)
    │
    ▼
请求携带 Token (Authorization: Bearer xxx)
    │
    ▼
JwtFilter 验证 Token → 解析用户信息 → 放行请求
```

### 2. 帖子模块

#### 功能列表

- 发布/编辑/删除帖子
- 帖子列表分页
- 帖子详情
- 点赞/取消点赞
- 收藏/取消收藏
- 评论/回复

#### 关键类

| 类名 | 说明 |
|------|------|
| `PostClientController` | 帖子接口控制器 |
| `PostServiceImpl` | 帖子业务逻辑 |
| `PostMapper` | 帖子数据访问 |

### 3. 消息通知模块

#### 通知类型

| 类型 | 说明 | 队列 |
|------|------|------|
| COMMENT | 评论/回复通知 | comment.queue |
| LIKE | 点赞通知 | like.queue |
| FOLLOW | 关注通知 | follow.queue |
| PRIVATE | 私信通知 | private.queue |
| SYSTEM | 系统通知 | system.queue |

#### 消息流转

```
业务操作 → NotifyProducer → RabbitMQ → NotifyConsumer → 数据库 + WebSocket推送
```

### 4. 搜索模块

#### 功能列表

- 帖子搜索
- 用户搜索
- 话题搜索
- 搜索建议
- 热门关键词

#### 关键类

| 类名 | 说明 |
|------|------|
| `SearchController` | 搜索接口控制器 |
| `SearchServiceImpl` | 搜索业务逻辑 |
| `PostDocument` | 帖子 ES 文档 |
| `PostSearchRepository` | ES 仓库 |

### 5. 秒杀模块

#### 功能列表

- 秒杀活动管理
- 库存预热
- 秒杀下单
- 防重复参与

#### 关键技术

- Redis Lua 脚本原子扣减
- Redis 库存预热
- 消息队列异步落库

---

## API 接口

### 用户接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/user/register` | POST | 用户注册 |
| `/user/login` | POST | 用户登录 |
| `/user/logout` | POST | 用户登出 |
| `/user/refresh` | POST | 刷新 Token |
| `/user/{id}` | GET | 获取用户信息 |
| `/user/update` | PUT | 更新用户信息 |

### 帖子接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/post` | GET | 帖子列表 |
| `/post/{id}` | GET | 帖子详情 |
| `/post` | POST | 发布帖子 |
| `/post/{id}` | PUT | 更新帖子 |
| `/post/{id}` | DELETE | 删除帖子 |
| `/post/{id}/like` | POST | 点赞帖子 |
| `/post/{id}/collect` | POST | 收藏帖子 |

### 评论接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/comment` | GET | 评论列表 |
| `/comment` | POST | 发布评论 |
| `/comment/{id}` | DELETE | 删除评论 |
| `/comment/{id}/like` | POST | 点赞评论 |

### 搜索接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/search/all` | GET | 综合搜索 |
| `/search/posts` | GET | 帖子搜索 |
| `/search/users` | GET | 用户搜索 |
| `/search/topics` | GET | 话题搜索 |
| `/search/suggest` | GET | 搜索建议 |
| `/search/hot-keywords` | GET | 热门关键词 |

### 消息中心接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/message-center/summary` | GET | 消息汇总 |
| `/message-center/comment` | GET | 评论通知 |
| `/message-center/like` | GET | 点赞通知 |
| `/message-center/follow` | GET | 关注通知 |

---

## 数据库设计

### 核心表结构

#### 用户表 (user)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名 |
| password | VARCHAR(255) | 密码 |
| nickname | VARCHAR(50) | 昵称 |
| avatar | VARCHAR(255) | 头像 |
| email | VARCHAR(100) | 邮箱 |
| phone | VARCHAR(20) | 手机号 |
| bio | VARCHAR(500) | 个人简介 |
| gender | TINYINT | 性别 |
| status | TINYINT | 状态 |
| role | TINYINT | 角色 |
| following_count | INT | 关注数 |
| follower_count | INT | 粉丝数 |
| post_count | INT | 帖子数 |
| points | INT | 积分 |
| create_time | DATETIME | 创建时间 |

#### 帖子表 (post)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| topic_id | BIGINT | 话题ID |
| title | VARCHAR(200) | 标题 |
| content | TEXT | 内容 |
| images | VARCHAR(2000) | 图片 |
| like_count | INT | 点赞数 |
| comment_count | INT | 评论数 |
| collect_count | INT | 收藏数 |
| view_count | INT | 浏览数 |
| status | TINYINT | 状态 |
| is_top | TINYINT | 是否置顶 |
| is_essence | TINYINT | 是否精华 |
| create_time | DATETIME | 创建时间 |

#### 通知主表 (user_notification_main)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| message_id | VARCHAR(100) | 消息唯一ID |
| receiver_id | BIGINT | 接收者ID |
| sender_id | BIGINT | 发送者ID |
| notify_type | VARCHAR(20) | 通知类型 |
| content | VARCHAR(500) | 通知内容 |
| read_status | TINYINT | 已读状态 |
| create_time | DATETIME | 创建时间 |

---

## 消息队列设计

### 队列定义

| 队列名称 | 路由键 | 说明 |
|---------|--------|------|
| community.notify.comment.queue | comment.notify | 评论通知 |
| community.notify.like.queue | like.notify | 点赞通知 |
| community.notify.follow.queue | follow.notify | 关注通知 |
| community.notify.private.queue | private.notify | 私信通知 |
| community.notify.system.queue | system.notify | 系统通知 |

### 消息格式

```json
{
  "messageId": "unique_message_id",
  "receiverId": 1,
  "senderId": 2,
  "notifyType": "COMMENT",
  "content": "用户A评论了你的帖子",
  "createTime": "2024-01-01T12:00:00",
  "businessId": 100,
  "businessType": "POST_COMMENT",
  "extra": ""
}
```

---

## WebSocket 设计

### 连接地址

```
ws://localhost:8082/ws/chat
```

### 消息格式

#### 发送消息

```json
{
  "messageId": 1704067200000,
  "userId": 1,
  "targetUserId": 2,
  "content": "你好"
}
```

#### 接收消息

```json
{
  "type": "message",
  "fromUserId": 1,
  "content": "你好",
  "time": 1704067200000
}
```

---

## 缓存策略

### Redis Key 设计

| Key | 类型 | 说明 | 过期时间 |
|-----|------|------|---------|
| `user:token:{userId}` | String | 用户Token | 7天 |
| `user:online:{userId}` | Hash | 在线状态 | 65秒 |
| `post:like:{postId}:{userId}` | String | 点赞记录 | 永久 |
| `post:collect:{postId}:{userId}` | String | 收藏记录 | 永久 |
| `post:view:{postId}` | String | 浏览计数 | 24小时 |
| `search:hot:keywords` | ZSet | 热门搜索 | 永久 |
| `seckill:stock:{seckillId}` | String | 秒杀库存 | 活动期间 |

---

## 部署指南

### Docker 部署

#### 1. 构建镜像

```bash
# 后端
cd community-server
mvn clean package
docker build -t cool-community-server:1.0 .

# 前端
cd frontend
npm run build
docker build -t cool-community-frontend:1.0 .
```

#### 2. Docker Compose

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: cool_community
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql/cool.sql:/docker-entrypoint-initdb.d/init.sql

  redis:
    image: redis:7
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq

  elasticsearch:
    image: elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
      - ES_JAVA_OPTS=-Xms512m -Xmx512m
    ports:
      - "9200:9200"
    volumes:
      - es_data:/usr/share/elasticsearch/data

  backend:
    image: cool-community-server:1.0
    ports:
      - "8082:8082"
    depends_on:
      - mysql
      - redis
      - rabbitmq
      - elasticsearch
    environment:
      - SPRING_PROFILES_ACTIVE=prod

  frontend:
    image: cool-community-frontend:1.0
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql_data:
  redis_data:
  rabbitmq_data:
  es_data:
```

#### 3. 启动服务

```bash
docker-compose up -d
```

---

## 开发规范

### 代码规范

#### 后端规范

1. **命名规范**
   - 类名：大驼峰 `UserService`
   - 方法名：小驼峰 `getUserById`
   - 常量：全大写 `MAX_SIZE`
   - 包名：全小写 `com.cool.server`

2. **注释规范**
   - 类注释：说明类的职责
   - 方法注释：说明参数、返回值、异常
   - 关键代码：行内注释

3. **异常处理**
   - 使用自定义异常 `BusinessException`
   - 统一异常处理 `GlobalExceptionHandler`

4. **日志规范**
   - 使用 Lombok `@Slf4j`
   - 关键操作记录日志
   - 异常记录堆栈

#### 前端规范

1. **组件命名**
   - 页面组件：`XxxPage.vue`
   - 公共组件：`XxxComponent.vue`

2. **API 调用**
   - 统一使用 `api/` 目录下的模块
   - 错误统一处理

3. **状态管理**
   - 使用 Pinia
   - 模块化拆分

### Git 提交规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式
refactor: 重构
test: 测试
chore: 构建/工具
```

### 分支管理

```
main        # 主分支
develop     # 开发分支
feature/*   # 功能分支
hotfix/*    # 热修复分支
release/*   # 发布分支
```

---

## 常见问题

### 1. 启动报错找不到数据源

检查 `application.yml` 中的数据库配置是否正确。

### 2. RabbitMQ 连接失败

确保 RabbitMQ 服务已启动，并检查用户名密码配置。

### 3. WebSocket 连接失败

检查前端 WebSocket 地址配置，确保与后端地址一致。

### 4. 搜索功能无数据

执行 `/search/sync` 接口同步数据到 Elasticsearch。

---

## 联系方式

- 作者：Sunjianlin
- 项目地址：https://github.com/your-username/cool-community

---

## 许可证

本项目仅供学习交流使用。
