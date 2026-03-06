# Cool Community - 酷安社区高仿项目

一个基于 Spring Boot + Vue 3 的数码科技社区平台，高仿酷安社区的核心功能。

## 项目简介

Cool Community 是一个功能完善的社区平台，包含帖子发布、话题讨论、产品评测、实时私信、秒杀活动等核心功能。项目采用前后端分离架构，后端使用 Spring Boot，前端使用 Vue 3。

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|-----|------|-----|
| Spring Boot | 3.2.0 | 核心框架 |
| MyBatis | 3.0.3 | ORM框架 |
| MySQL | 8.0.33 | 关系型数据库 |
| Redis | - | 缓存中间件 |
| RabbitMQ | 3.2.0 | 消息队列 |
| Spring WebSocket | - | 实时通信 |
| JJWT | 0.12.3 | JWT认证 |
| Druid | 1.2.20 | 数据库连接池 |
| Hutool | 5.8.23 | Java工具库 |
| 阿里云OSS | 3.17.4 | 对象存储 |
| SpringDoc | 2.3.0 | API文档 |
| Lombok | 1.18.40 | 代码简化 |

### 前端

| 技术 | 版本 | 说明 |
|-----|------|-----|
| Vue | 3.5.13 | 前端框架 |
| Vite | 6.0.5 | 构建工具 |
| Pinia | 2.3.1 | 状态管理 |
| Vue Router | 4.4.5 | 路由管理 |
| Element Plus | 2.13.2 | UI组件库 |
| Axios | 1.13.5 | HTTP客户端 |
| Vue Quill | 1.2.0 | 富文本编辑器 |

## 项目结构

```
cool-community/
├── community-common/          # 公共模块
│   └── src/main/java/com/cool/common/
│       ├── constant/          # 常量定义
│       ├── enumeration/       # 枚举类
│       ├── exception/         # 异常类
│       └── utils/             # 工具类
├── community-pojo/            # 实体模块
│   └── src/main/java/com/cool/pojo/
│       ├── dto/               # 数据传输对象
│       ├── entity/            # 实体类
│       └── vo/                # 视图对象
├── community-server/          # 服务模块
│   └── src/main/java/com/cool/server/
│       ├── config/            # 配置类
│       ├── controller/        # 控制器
│       ├── mapper/            # MyBatis Mapper
│       ├── service/           # 业务服务
│       ├── security/          # 安全模块
│       └── websocket/         # WebSocket处理
├── frontend/                  # 前端项目
│   └── src/
│       ├── api/               # API接口
│       ├── components/        # 组件
│       ├── store/             # 状态管理
│       └── views/             # 页面视图
└── sql/                       # SQL脚本
```

## 功能模块

### 核心功能

- **用户模块**：注册/登录、个人信息管理、在线状态、角色权限
- **帖子模块**：发布帖子、浏览、点赞、收藏、评论回复
- **话题模块**：话题创建、关注、热门话题
- **产品模块**：产品展示、品牌管理、产品评测

### 社交功能

- **私信聊天**：WebSocket实时通信、会话管理
- **消息中心**：评论回复、点赞、关注、系统通知
- **关注系统**：用户关注、粉丝管理

### 活动功能

- **秒杀活动**：Redis预热、Lua原子扣减、防重复参与
- **每日签到**：签到奖励、连续签到加成
- **积分系统**：积分获取、消费、明细记录

### 管理后台

- 用户管理
- 帖子审核
- 产品管理
- 话题管理
- 秒杀活动管理
- 数据统计

## 技术亮点

### Redis缓存应用

- 用户Token缓存
- 帖子点赞/收藏/浏览计数
- 用户关注关系缓存
- 秒杀库存预热
- 热门帖子缓存

### 消息队列异步处理

- RabbitMQ处理各类通知
- 秒杀异步落库
- 业务解耦

### WebSocket实时通信

- 实时私聊
- 在线状态同步
- 心跳检测

### 秒杀高并发处理

- Lua脚本原子操作
- Redis库存预热
- 防重复参与
- 异步数据库写入

### 安全认证

- JWT双Token机制（Access Token + Refresh Token）
- Spring Security集成
- 角色权限控制


## 开发计划

- [ ] 搜索功能优化
- [ ] 帖子推荐算法
- [ ] 移动端适配
- [ ] 性能优化
- [ ] 单元测试覆盖

## 致谢

本项目仅供学习交流使用，灵感来源于酷安社区。
by Sunjianlin
