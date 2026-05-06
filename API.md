# Cool Community API 接口文档

## 基础信息

- **Base URL**: `http://localhost:8082/api`
- **认证方式**: Bearer Token (JWT)
- **Content-Type**: `application/json`

## 通用响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 错误响应格式

```json
{
  "code": 500,
  "message": "错误信息",
  "data": null
}
```

---

## 目录

- [用户接口](#用户接口)
- [帖子接口](#帖子接口)
- [评论接口](#评论接口)
- [话题接口](#话题接口)
- [产品接口](#产品接口)
- [关注接口](#关注接口)
- [聊天接口](#聊天接口)
- [消息接口](#消息接口)
- [消息中心接口](#消息中心接口)
- [搜索接口](#搜索接口)
- [秒杀接口](#秒杀接口)
- [签到接口](#签到接口)
- [积分接口](#积分接口)
- [文件接口](#文件接口)
- [分类接口](#分类接口)
- [品牌接口](#品牌接口)
- [推荐接口](#推荐接口)
- [在线状态接口](#在线状态接口)
- [心跳接口](#心跳接口)
- [通知接口](#通知接口)
- [用户背景图接口](#用户背景图接口)
- [管理端接口](#管理端接口)
- [WebSocket 接口](#websocket-接口)

---

## 用户接口

### 1. 用户注册

**POST** `/user/register`

**请求体**:
```json
{
  "username": "testuser",
  "password": "Test@123456",
  "nickname": "测试用户",
  "email": "test@example.com",
  "phone": "13800138000"
}
```

**响应**: 无返回内容（HTTP 200）

### 2. 用户登录

**POST** `/user/login`

**请求体**:
```json
{
  "username": "testuser",
  "password": "Test@123456"
}
```

**响应**:
```json
{
  "id": 1,
  "username": "testuser",
  "nickname": "测试用户",
  "avatar": "https://...",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 3. 刷新Token

**POST** `/user/refresh`

**请求体**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "deviceId": "device-uuid"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "avatar": "https://...",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

### 4. 用户登出

**POST** `/user/logout`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**: 无返回内容（HTTP 200）

### 5. 获取当前用户信息

**GET** `/user/info`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
{
  "id": 1,
  "username": "testuser",
  "nickname": "测试用户",
  "avatar": "https://...",
  "email": "test@example.com",
  "phone": "13800138000",
  "bio": "个人简介",
  "gender": 1,
  "points": 500
}
```

### 6. 获取指定用户信息

**GET** `/user/info/{id}`

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 用户ID |

**响应**:
```json
{
  "id": 1,
  "username": "testuser",
  "nickname": "测试用户",
  "avatar": "https://...",
  "bio": "个人简介",
  "gender": 1,
  "followerCount": 100,
  "followingCount": 50,
  "postCount": 20,
  "isFollowing": false
}
```

### 7. 更新用户信息

**PUT** `/user/update`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**请求体**:
```json
{
  "nickname": "新昵称",
  "bio": "新的个人简介",
  "gender": 1,
  "avatar": "https://..."
}
```

**响应**: 无返回内容（HTTP 200）

### 8. 上传头像

**POST** `/user/avatar`

**请求头**:
```
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data
```

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| file | File | 是 | 头像图片文件 |

**响应**:
```json
"https://oss.example.com/avatar/xxx.jpg"
```

### 9. 关注用户

**POST** `/user/follow/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 用户ID |

**响应**: 无返回内容（HTTP 200）

### 10. 取消关注用户

**DELETE** `/user/unfollow/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 用户ID |

**响应**: 无返回内容（HTTP 200）

### 11. 获取关注列表

**GET** `/user/following/{id}`

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 用户ID |

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应**:
```json
{
  "records": [
    {
      "id": 2,
      "username": "following_user",
      "nickname": "关注用户",
      "avatar": "https://..."
    }
  ],
  "total": 50,
  "page": 1,
  "pageSize": 10
}
```

### 12. 获取粉丝列表

**GET** `/user/followers/{id}`

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 用户ID |

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应**:
```json
{
  "records": [
    {
      "id": 3,
      "username": "follower_user",
      "nickname": "粉丝用户",
      "avatar": "https://..."
    }
  ],
  "total": 100,
  "page": 1,
  "pageSize": 10
}
```

---

## 帖子接口

### 1. 获取帖子列表

**GET** `/post/list`

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |
| topicId | Long | 否 | - | 话题ID |
| userId | Long | 否 | - | 用户ID |
| keyword | String | 否 | - | 搜索关键词 |

**响应**:
```json
{
  "records": [
    {
      "id": 1,
      "title": "帖子标题",
      "content": "帖子内容...",
      "userId": 1,
      "username": "testuser",
      "userNickname": "测试用户",
      "userAvatar": "https://...",
      "topicId": 1,
      "topicName": "数码科技",
      "likeCount": 100,
      "commentCount": 20,
      "viewCount": 500,
      "isTop": false,
      "isEssence": true,
      "createTime": "2024-01-01 12:00:00"
    }
  ],
  "total": 100,
  "page": 1,
  "pageSize": 10
}
```

### 2. 获取帖子详情

**GET** `/post/detail/{id}`

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 帖子ID |

**响应**:
```json
{
  "id": 1,
  "title": "帖子标题",
  "content": "帖子完整内容...",
  "images": "https://...,https://...",
  "userId": 1,
  "username": "testuser",
  "userNickname": "测试用户",
  "userAvatar": "https://...",
  "topicId": 1,
  "topicName": "数码科技",
  "likeCount": 100,
  "commentCount": 20,
  "collectCount": 50,
  "viewCount": 500,
  "isTop": false,
  "isEssence": true,
  "isLiked": false,
  "isCollected": false,
  "createTime": "2024-01-01 12:00:00"
}
```

### 3. 发布帖子

**POST** `/post/create`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**请求体**:
```json
{
  "title": "帖子标题",
  "content": "帖子内容",
  "images": "https://...,https://...",
  "topicId": 1,
  "type": 1
}
```

**响应**:
```json
1
```

### 4. 删除帖子

**DELETE** `/post/delete/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 帖子ID |

**响应**: 无返回内容（HTTP 200）

### 5. 点赞帖子

**POST** `/post/like/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 帖子ID |

**响应**: 无返回内容（HTTP 200）

### 6. 取消点赞

**DELETE** `/post/unlike/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 帖子ID |

**响应**: 无返回内容（HTTP 200）

### 7. 收藏帖子

**POST** `/post/collect/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 帖子ID |

**响应**: 无返回内容（HTTP 200）

### 8. 取消收藏

**DELETE** `/post/uncollect/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 帖子ID |

**响应**: 无返回内容（HTTP 200）

---

## 评论接口

### 1. 获取评论列表

**GET** `/comment/list/{postId}`

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| postId | Long | 是 | 帖子ID |

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 20 | 每页数量 |

**响应**:
```json
{
  "records": [
    {
      "id": 1,
      "postId": 1,
      "userId": 2,
      "username": "评论者",
      "userAvatar": "https://...",
      "content": "评论内容",
      "likeCount": 10,
      "parentId": null,
      "replyCount": 2,
      "createTime": "2024-01-01 12:00:00",
      "replies": [
        {
          "id": 2,
          "content": "回复内容",
          "userId": 3,
          "username": "回复者",
          "replyToName": "评论者",
          "createTime": "2024-01-01 12:30:00"
        }
      ]
    }
  ],
  "total": 50,
  "page": 1,
  "pageSize": 20
}
```

### 2. 发布评论

**POST** `/comment/create`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**请求体**:
```json
{
  "postId": 1,
  "content": "评论内容",
  "parentId": null,
  "replyToId": null
}
```

**响应**:
```json
1
```

### 3. 删除评论

**DELETE** `/comment/delete/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 评论ID |

**响应**: 无返回内容（HTTP 200）

### 4. 点赞评论

**POST** `/comment/like/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 评论ID |

**响应**: 无返回内容（HTTP 200）

### 5. 取消点赞评论

**DELETE** `/comment/unlike/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 评论ID |

**响应**: 无返回内容（HTTP 200）

---

## 话题接口

### 1. 获取话题列表

**GET** `/topic/list`

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |
| category | String | 否 | - | 分类 |

**响应**:
```json
{
  "records": [
    {
      "id": 1,
      "name": "数码科技",
      "description": "数码产品讨论",
      "icon": "📱",
      "cover": "https://...",
      "category": "科技",
      "followCount": 1000,
      "postCount": 500,
      "isHot": true,
      "isFollowed": false
    }
  ],
  "total": 50,
  "page": 1,
  "pageSize": 10
}
```

### 2. 获取热门话题

**GET** `/topic/hot`

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应**:
```json
{
  "records": [
    {
      "id": 1,
      "name": "数码科技",
      "description": "数码产品讨论",
      "icon": "📱",
      "followCount": 1000,
      "postCount": 500,
      "isHot": true
    }
  ],
  "total": 10,
  "page": 1,
  "pageSize": 10
}
```

### 3. 获取话题详情

**GET** `/topic/detail/{id}`

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 话题ID |

**响应**:
```json
{
  "id": 1,
  "name": "数码科技",
  "description": "数码产品讨论专区",
  "icon": "📱",
  "cover": "https://...",
  "category": "科技",
  "followCount": 1000,
  "postCount": 500,
  "isHot": true,
  "isFollowed": false
}
```

### 4. 关注话题

**POST** `/topic/follow/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 话题ID |

**响应**: 无返回内容（HTTP 200）

### 5. 取消关注话题

**DELETE** `/topic/unfollow/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 话题ID |

**响应**: 无返回内容（HTTP 200）

---

## 产品接口

### 1. 获取产品列表

**GET** `/product/list`

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |
| categoryId | Long | 否 | - | 分类ID |
| brandId | Long | 否 | - | 品牌ID |

**响应**:
```json
{
  "records": [
    {
      "id": 1,
      "name": "iPhone 15 Pro",
      "brand": "Apple",
      "price": "7999",
      "image": "https://...",
      "reviewCount": 100,
      "avgRating": 4.8,
      "followCount": 500
    }
  ],
  "total": 100,
  "page": 1,
  "pageSize": 10
}
```

### 2. 获取产品详情

**GET** `/product/detail/{id}`

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 产品ID |

**响应**:
```json
{
  "id": 1,
  "name": "iPhone 15 Pro",
  "brand": "Apple",
  "brandId": 1,
  "price": "7999",
  "image": "https://...",
  "description": "产品描述",
  "specs": "规格参数",
  "reviewCount": 100,
  "avgRating": 4.8,
  "followCount": 500,
  "isFollowed": false
}
```

---

## 关注接口

### 1. 关注用户

**POST** `/follow/user/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 用户ID |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": "关注成功"
}
```

### 2. 取消关注用户

**DELETE** `/follow/user/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 用户ID |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": "已取消关注"
}
```

### 3. 关注话题

**POST** `/follow/topic/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 话题ID |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": "关注成功"
}
```

### 4. 取消关注话题

**DELETE** `/follow/topic/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 话题ID |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": "已取消关注"
}
```

### 5. 关注产品

**POST** `/follow/product/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 产品ID |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": "关注成功"
}
```

### 6. 取消关注产品

**DELETE** `/follow/product/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 产品ID |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": "已取消关注"
}
```

### 7. 检查是否关注

**GET** `/follow/check`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| targetId | Long | 是 | 目标ID |
| targetType | Integer | 是 | 目标类型：0-用户，1-话题，2-产品 |

**响应**:
```json
true
```

### 8. 获取关注列表

**GET** `/follow/following/{userId}`

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| userId | Long | 是 | 用户ID |

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| targetType | Integer | 是 | - | 目标类型：0-用户，1-话题，2-产品 |
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应**:
```json
{
  "records": [...],
  "total": 50,
  "page": 1,
  "pageSize": 10
}
```

### 9. 获取粉丝列表

**GET** `/follow/followers/{targetId}`

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| targetId | Long | 是 | 目标ID |

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| targetType | Integer | 是 | - | 目标类型：0-用户，1-话题，2-产品 |
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应**:
```json
{
  "records": [...],
  "total": 100,
  "page": 1,
  "pageSize": 10
}
```

### 10. 获取关注数量

**GET** `/follow/count/{targetId}`

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| targetId | Long | 是 | 目标ID |

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| targetType | Integer | 是 | 目标类型：0-用户，1-话题，2-产品 |

**响应**:
```json
100
```

### 11. 获取被关注数量

**GET** `/follow/follower/count/{userId}`

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| userId | Long | 是 | 用户ID |

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| targetType | Integer | 是 | 目标类型：0-用户，1-话题，2-产品 |

**响应**:
```json
500
```

---

## 聊天接口

### 1. 获取聊天列表

**GET** `/chat/list`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
[
  {
    "id": 1,
    "targetUserId": 2,
    "targetUserName": "聊天对象",
    "targetUserAvatar": "https://...",
    "lastMessage": "最后一条消息",
    "lastMessageTime": "2024-01-01 12:00:00",
    "unreadCount": 5
  }
]
```

### 2. 获取或创建会话

**GET** `/chat/session/{targetUserId}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| targetUserId | Long | 是 | 目标用户ID |

**响应**:
```json
{
  "id": 1,
  "targetUserId": 2,
  "targetUserName": "聊天对象",
  "targetUserAvatar": "https://..."
}
```

### 3. 获取会话消息

**GET** `/chat/messages/{sessionId}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| sessionId | Long | 是 | 会话ID |

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 50 | 每页数量 |

**响应**:
```json
{
  "records": [
    {
      "id": 1,
      "sessionId": 1,
      "senderId": 1,
      "content": "消息内容",
      "isMine": true,
      "createTime": "2024-01-01 12:00:00"
    }
  ],
  "total": 100,
  "page": 1,
  "pageSize": 50
}
```

### 4. 发送消息

**POST** `/chat/send`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**请求体**:
```json
{
  "sessionId": 1,
  "content": "消息内容"
}
```

**响应**:
```json
1
```

### 5. 标记已读

**POST** `/chat/read/{sessionId}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| sessionId | Long | 是 | 会话ID |

**响应**: 无返回内容（HTTP 200）

### 6. 获取未读消息数

**GET** `/chat/unread`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
5
```

### 7. 删除会话

**DELETE** `/chat/session/{sessionId}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| sessionId | Long | 是 | 会话ID |

**响应**: 无返回内容（HTTP 200）

### 8. 关闭会话

**POST** `/chat/close/{sessionId}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| sessionId | Long | 是 | 会话ID |

**响应**: 无返回内容（HTTP 200）

---

## 消息接口

### 1. 发送消息

**POST** `/message/send`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**请求体**:
```json
{
  "toUserId": 2,
  "content": "消息内容"
}
```

**响应**:
```json
1
```

### 2. 获取消息列表

**GET** `/message/list`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 20 | 每页数量 |

**响应**:
```json
{
  "records": [
    {
      "id": 1,
      "fromUserId": 2,
      "fromUserName": "发送者",
      "fromUserAvatar": "https://...",
      "content": "消息内容",
      "isRead": false,
      "createTime": "2024-01-01 12:00:00"
    }
  ],
  "total": 50,
  "page": 1,
  "pageSize": 20
}
```

### 3. 获取未读消息数量

**GET** `/message/unread`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
5
```

### 4. 标记消息已读

**POST** `/message/read/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 消息ID |

**响应**: 无返回内容（HTTP 200）

### 5. 标记所有消息已读

**POST** `/message/read/all`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**: 无返回内容（HTTP 200）

### 6. 删除消息

**DELETE** `/message/delete/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 消息ID |

**响应**: 无返回内容（HTTP 200）

### 7. 获取会话列表

**GET** `/message/conversations`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
[
  {
    "toUserId": 2,
    "toUserName": "聊天对象",
    "toUserAvatar": "https://...",
    "lastMessage": "最后一条消息",
    "lastMessageTime": "2024-01-01 12:00:00",
    "unreadCount": 5
  }
]
```

### 8. 获取会话消息

**GET** `/message/conversation/{toUserId}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| toUserId | Long | 是 | 对方用户ID |

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 50 | 每页数量 |

**响应**:
```json
{
  "records": [
    {
      "id": 1,
      "fromUserId": 1,
      "toUserId": 2,
      "content": "消息内容",
      "isRead": true,
      "createTime": "2024-01-01 12:00:00"
    }
  ],
  "total": 100,
  "page": 1,
  "pageSize": 50
}
```

### 9. 标记会话已读

**POST** `/message/conversation/read/{toUserId}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| toUserId | Long | 是 | 对方用户ID |

**响应**: 无返回内容（HTTP 200）

---

## 消息中心接口

### 1. 获取消息汇总

**GET** `/message-center/summary`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "unreadPrivate": 5,
    "unreadComment": 10,
    "unreadLike": 20,
    "unreadFollow": 3,
    "unreadSystem": 2,
    "totalUnread": 40
  }
}
```

### 2. 获取评论通知

**GET** `/message-center/comment`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 20 | 每页数量 |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "replyList": [
      {
        "id": 1,
        "commenterName": "评论者",
        "commenterAvatar": "https://...",
        "postTitle": "帖子标题",
        "postId": 1,
        "content": "回复内容",
        "type": "COMMENT_REPLY",
        "isRead": 0,
        "createTime": "2024-01-01 12:00:00"
      }
    ],
    "commentList": [
      {
        "id": 1,
        "commenterName": "评论者",
        "commenterAvatar": "https://...",
        "postTitle": "帖子标题",
        "postId": 1,
        "content": "评论内容",
        "type": "POST_COMMENT",
        "isRead": 0,
        "createTime": "2024-01-01 12:00:00"
      }
    ],
    "current": 1,
    "size": 20
  }
}
```

### 3. 获取点赞通知

**GET** `/message-center/like`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 20 | 每页数量 |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "likerName": "点赞者",
        "likerAvatar": "https://...",
        "postTitle": "帖子标题",
        "postId": 1,
        "type": "POST_LIKE",
        "isRead": 0,
        "createTime": "2024-01-01 12:00:00"
      }
    ],
    "current": 1,
    "size": 20
  }
}
```

### 4. 获取关注通知

**GET** `/message-center/follow`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 20 | 每页数量 |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "followerName": "关注者",
        "followerAvatar": "https://...",
        "followerId": 2,
        "isRead": 0,
        "createTime": "2024-01-01 12:00:00"
      }
    ],
    "current": 1,
    "size": 20
  }
}
```

### 5. 获取系统通知

**GET** `/message-center/system`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 20 | 每页数量 |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "系统通知标题",
        "content": "系统通知内容",
        "isRead": 0,
        "createTime": "2024-01-01 12:00:00"
      }
    ],
    "current": 1,
    "size": 20
  }
}
```

### 6. 标记评论回复通知已读

**PUT** `/message-center/comment-reply/{id}/read`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 通知ID |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 7. 标记帖子评论通知已读

**PUT** `/message-center/post-comment/{id}/read`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 通知ID |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 8. 标记所有评论通知已读

**PUT** `/message-center/comment/read-all`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 9. 标记点赞通知已读

**PUT** `/message-center/like/{id}/read`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 通知ID |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 10. 标记所有点赞通知已读

**PUT** `/message-center/like/read-all`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 11. 标记关注通知已读

**PUT** `/message-center/follow/{id}/read`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 通知ID |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 12. 标记所有关注通知已读

**PUT** `/message-center/follow/read-all`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 13. 标记系统通知已读

**PUT** `/message-center/system/{id}/read`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 通知ID |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 14. 标记所有系统通知已读

**PUT** `/message-center/system/read-all`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 15. 标记所有通知已读

**PUT** `/message-center/read-all`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 搜索接口

### 1. 综合搜索

**GET** `/search/all`

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| keyword | String | 是 | - | 搜索关键词 |
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "posts": {
      "records": [...],
      "total": 50
    },
    "users": {
      "records": [...],
      "total": 20
    },
    "topics": {
      "records": [...],
      "total": 10
    },
    "hotKeywords": ["热门词1", "热门词2"]
  }
}
```

### 2. 帖子搜索

**GET** `/search/posts`

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| keyword | String | 是 | - | 搜索关键词 |
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "帖子标题",
        "content": "帖子内容...",
        "highlightTitle": "<em class=\"highlight\">关键词</em>标题",
        "highlightContent": "内容<em class=\"highlight\">关键词</em>...",
        "userId": 1,
        "username": "用户名",
        "userNickname": "昵称",
        "likeCount": 100,
        "commentCount": 20,
        "createTime": "2024-01-01 12:00:00"
      }
    ],
    "total": 50,
    "page": 1,
    "pageSize": 10
  }
}
```

### 3. 用户搜索

**GET** `/search/users`

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| keyword | String | 是 | - | 搜索关键词 |
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "testuser",
        "nickname": "测试用户",
        "avatar": "https://...",
        "bio": "个人简介",
        "followerCount": 100
      }
    ],
    "total": 20,
    "page": 1,
    "pageSize": 10
  }
}
```

### 4. 话题搜索

**GET** `/search/topics`

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| keyword | String | 是 | - | 搜索关键词 |
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "话题名称",
        "description": "话题描述",
        "icon": "📱",
        "followCount": 1000,
        "postCount": 500
      }
    ],
    "total": 10,
    "page": 1,
    "pageSize": 10
  }
}
```

### 5. 搜索建议

**GET** `/search/suggest`

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| prefix | String | 是 | 搜索前缀 |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": ["建议1", "建议2", "建议3"]
}
```

### 6. 热门搜索词

**GET** `/search/hot-keywords`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": ["热门词1", "热门词2", "热门词3"]
}
```

### 7. 同步数据到ES

**POST** `/search/sync`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": "数据同步完成"
}
```

---

## 秒杀接口

### 1. 获取活动详情

**GET** `/seckill/detail/{id}`

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 活动ID |

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "秒杀活动",
    "description": "活动描述",
    "startTime": "2024-01-01 10:00:00",
    "endTime": "2024-01-01 12:00:00",
    "stock": 100,
    "price": 99.00,
    "originalPrice": 199.00,
    "status": 1
  }
}
```

### 2. 执行秒杀

**POST** `/seckill/do/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 活动ID |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 3. 获取下一个活动

**GET** `/seckill/next`

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "下一个秒杀活动",
    "startTime": "2024-01-02 10:00:00",
    "endTime": "2024-01-02 12:00:00"
  }
}
```

### 4. 检查是否有即将开始的活动

**GET** `/seckill/has-next`

**响应**:
```json
{
  "code": 200,
  "data": true
}
```

### 5. 获取活动列表

**GET** `/seckill/list`

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "秒杀活动1",
      "startTime": "2024-01-01 10:00:00",
      "endTime": "2024-01-01 12:00:00",
      "stock": 100,
      "price": 99.00
    }
  ]
}
```

---

## 签到接口

### 1. 检查今日是否已签到

**GET** `/checkin/status`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": false
}
```

### 2. 用户签到

**POST** `/checkin`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": 10
}
```

### 3. 获取连续签到天数

**GET** `/checkin/consecutive`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": 7
}
```

### 4. 检查指定日期是否签到

**GET** `/checkin/date`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| date | String | 是 | 日期，格式：YYYY-MM-DD |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

---

## 积分接口

### 1. 获取用户积分

**GET** `/points/`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": 500
}
```

### 2. 增加用户积分

**POST** `/points/add`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| points | Integer | 是 | 积分数量 |
| type | Integer | 是 | 积分类型 |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 3. 减少用户积分

**POST** `/points/reduce`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| points | Integer | 是 | 积分数量 |
| type | Integer | 是 | 积分类型 |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

---

## 文件接口

### 1. 上传单个文件

**POST** `/file/upload`

**请求类型**: `multipart/form-data`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| file | File | 是 | 文件 |

**响应**:
```json
"https://oss.example.com/files/xxx.jpg"
```

### 2. 上传多张图片

**POST** `/file/upload/images`

**请求类型**: `multipart/form-data`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| files | File[] | 是 | 图片文件数组 |

**响应**:
```json
[
  "https://oss.example.com/files/1.jpg",
  "https://oss.example.com/files/2.jpg"
]
```

---

## 分类接口

### 1. 获取分类列表

**GET** `/category/list`

**响应**:
```json
[
  {
    "id": 1,
    "name": "手机",
    "icon": "📱",
    "sort": 1
  },
  {
    "id": 2,
    "name": "电脑",
    "icon": "💻",
    "sort": 2
  }
]
```

---

## 品牌接口

### 1. 获取品牌列表

**GET** `/brand/list`

**响应**:
```json
[
  {
    "id": 1,
    "name": "Apple",
    "logo": "https://...",
    "description": "苹果公司"
  }
]
```

### 2. 获取品牌详情

**GET** `/brand/{id}`

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 品牌ID |

**响应**:
```json
{
  "id": 1,
  "name": "Apple",
  "logo": "https://...",
  "description": "苹果公司"
}
```

---

## 推荐接口

### 1. 推荐用户

**GET** `/recommendation/users`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应**:
```json
{
  "records": [...],
  "total": 50,
  "page": 1,
  "pageSize": 10
}
```

### 2. 推荐话题

**GET** `/recommendation/topics`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应**:
```json
{
  "records": [...],
  "total": 20,
  "page": 1,
  "pageSize": 10
}
```

### 3. 推荐产品

**GET** `/recommendation/products`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应**:
```json
{
  "records": [...],
  "total": 30,
  "page": 1,
  "pageSize": 10
}
```

---

## 在线状态接口

### 1. 获取用户在线状态

**GET** `/online-status/{userId}`

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| userId | Long | 是 | 用户ID |

**响应**:
```json
{
  "userId": 1,
  "status": 1,
  "statusDesc": "在线"
}
```

**状态码说明**:
| 状态码 | 说明 |
|-------|------|
| 0 | 离线 |
| 1 | 在线 |
| 2 | 离开 |
| 3 | 忙碌 |

### 2. 更新在线状态

**PUT** `/online-status`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**请求体**:
```json
{
  "status": 1
}
```

**响应**: HTTP 200

### 3. 获取在线用户列表

**GET** `/online-status/online-users`

**响应**:
```json
[1, 2, 3, 5, 8]
```

---

## 心跳接口

### 1. 发送心跳

**POST** `/heartbeat`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 通知接口

### 1. 获取通知列表

**GET** `/notification`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
[
  {
    "id": 1,
    "type": "SYSTEM",
    "title": "系统通知",
    "content": "通知内容",
    "isRead": false,
    "createTime": "2024-01-01 12:00:00"
  }
]
```

### 2. 标记通知已读

**PUT** `/notification/{id}/read`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 通知ID |

**响应**: 无返回内容（HTTP 200）

### 3. 标记所有通知已读

**PUT** `/notification/read-all`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**: 无返回内容（HTTP 200）

### 4. 获取未读通知数量

**GET** `/notification/unread-count`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
5
```

---

## 用户背景图接口

### 1. 获取用户背景图列表

**GET** `/background/list`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "默认背景",
      "imageUrl": "https://...",
      "thumbnail": "https://...",
      "isDefault": true
    }
  ]
}
```

### 2. 获取用户当前背景图

**GET** `/background/current`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "默认背景",
    "imageUrl": "https://...",
    "thumbnail": "https://..."
  }
}
```

### 3. 设置当前背景图

**POST** `/background/set-current`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| backgroundId | Long | 是 | 背景图ID |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": "设置成功"
}
```

---

## 管理端接口

### 用户管理

#### 1. 获取用户列表

**GET** `/admin/user/list`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |
| keyword | String | 否 | - | 搜索关键词 |
| status | Integer | 否 | - | 状态 |

**响应**:
```json
{
  "records": [...],
  "total": 100,
  "page": 1,
  "pageSize": 10
}
```

#### 2. 禁用用户

**POST** `/admin/user/ban/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 用户ID |

**响应**: 无返回内容（HTTP 200）

#### 3. 解禁用户

**POST** `/admin/user/unban/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 用户ID |

**响应**: 无返回内容（HTTP 200）

#### 4. 删除用户

**DELETE** `/admin/user/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 用户ID |

**响应**: 无返回内容（HTTP 200）

#### 5. 踢人下线

**POST** `/admin/user/kick/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 用户ID |

**响应**: 无返回内容（HTTP 200）

#### 6. 在线用户统计

**GET** `/admin/user/online-stats`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
{
  "total": 1000,
  "online": 50,
  "offline": 950
}
```

### 帖子管理

#### 1. 获取帖子列表

**GET** `/admin/post/list`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |
| status | Integer | 否 | - | 状态 |

**响应**:
```json
{
  "records": [...],
  "total": 100,
  "page": 1,
  "pageSize": 10
}
```

#### 2. 审核通过

**POST** `/admin/post/approve/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 帖子ID |

**响应**: 无返回内容（HTTP 200）

#### 3. 审核拒绝

**POST** `/admin/post/reject/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 帖子ID |

**响应**: 无返回内容（HTTP 200）

#### 4. 置顶帖子

**POST** `/admin/post/top/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 帖子ID |

**响应**: 无返回内容（HTTP 200）

#### 5. 取消置顶

**DELETE** `/admin/post/top/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 帖子ID |

**响应**: 无返回内容（HTTP 200）

#### 6. 设为精华

**POST** `/admin/post/essence/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 帖子ID |

**响应**: 无返回内容（HTTP 200）

#### 7. 取消精华

**DELETE** `/admin/post/essence/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 帖子ID |

**响应**: 无返回内容（HTTP 200）

#### 8. 删除帖子

**DELETE** `/admin/post/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 帖子ID |

**响应**: 无返回内容（HTTP 200）

### 话题管理

#### 1. 获取话题列表

**GET** `/admin/topic/list`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应**:
```json
{
  "records": [...],
  "total": 50,
  "page": 1,
  "pageSize": 10
}
```

#### 2. 创建话题

**POST** `/admin/topic/create`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**请求体**:
```json
{
  "name": "新话题",
  "description": "话题描述",
  "icon": "📱",
  "category": "科技"
}
```

**响应**:
```json
1
```

#### 3. 更新话题

**PUT** `/admin/topic/update`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**请求体**:
```json
{
  "id": 1,
  "name": "更新后的话题名",
  "description": "更新后的描述"
}
```

**响应**: 无返回内容（HTTP 200）

#### 4. 删除话题

**DELETE** `/admin/topic/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 话题ID |

**响应**: 无返回内容（HTTP 200）

#### 5. 设为热门

**POST** `/admin/topic/setHot/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 话题ID |

**响应**: 无返回内容（HTTP 200）

#### 6. 取消热门

**DELETE** `/admin/topic/setHot/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 话题ID |

**响应**: 无返回内容（HTTP 200）

### 产品管理

#### 1. 获取产品列表

**GET** `/admin/product/list`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

**响应**:
```json
{
  "records": [...],
  "total": 100,
  "page": 1,
  "pageSize": 10
}
```

#### 2. 创建产品

**POST** `/admin/product/create`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**请求体**:
```json
{
  "name": "新产品",
  "brandId": 1,
  "categoryId": 1,
  "price": "999",
  "description": "产品描述",
  "specs": "规格参数",
  "image": "https://..."
}
```

**响应**:
```json
1
```

#### 3. 更新产品

**PUT** `/admin/product/update`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**请求体**:
```json
{
  "id": 1,
  "name": "更新后的产品",
  "brandId": 1,
  "categoryId": 1,
  "price": "899",
  "description": "更新后的描述"
}
```

**响应**: 无返回内容（HTTP 200）

#### 4. 删除产品

**DELETE** `/admin/product/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 产品ID |

**响应**: 无返回内容（HTTP 200）

### 品牌管理

#### 1. 获取品牌列表

**GET** `/admin/brand/list`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
[
  {
    "id": 1,
    "name": "Apple",
    "logo": "https://...",
    "description": "苹果公司"
  }
]
```

#### 2. 创建品牌

**POST** `/admin/brand/create`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**请求体**:
```json
{
  "name": "新品牌",
  "logo": "https://...",
  "description": "品牌描述"
}
```

**响应**:
```json
1
```

#### 3. 更新品牌

**PUT** `/admin/brand/update`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**请求体**:
```json
{
  "id": 1,
  "name": "更新后的品牌",
  "logo": "https://...",
  "description": "更新后的描述"
}
```

**响应**: 无返回内容（HTTP 200）

#### 4. 删除品牌

**DELETE** `/admin/brand/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 品牌ID |

**响应**: 无返回内容（HTTP 200）

### 秒杀管理

#### 1. 上传背景图

**POST** `/admin/seckill/upload`

**请求头**:
```
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data
```

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| file | File | 是 | 背景图文件 |

**响应**:
```json
{
  "code": 200,
  "data": "https://oss.example.com/seckill/xxx.jpg"
}
```

#### 2. 创建活动

**POST** `/admin/seckill/create`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**请求体**:
```json
{
  "name": "秒杀活动",
  "productId": 1,
  "startTime": "2024-01-01 10:00:00",
  "endTime": "2024-01-01 12:00:00",
  "stock": 100,
  "price": 99.00,
  "backgroundImage": "https://..."
}
```

**响应**:
```json
{
  "code": 200,
  "message": "创建成功"
}
```

#### 3. 更新活动

**PUT** `/admin/seckill/update`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**请求体**:
```json
{
  "id": 1,
  "name": "更新后的活动",
  "stock": 200,
  "price": 89.00
}
```

**响应**:
```json
{
  "code": 200,
  "message": "更新成功"
}
```

#### 4. 删除活动

**DELETE** `/admin/seckill/delete/{id}`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | Long | 是 | 活动ID |

**响应**:
```json
{
  "code": 200,
  "message": "删除成功"
}
```

#### 5. 获取活动列表

**GET** `/admin/seckill/list`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "秒杀活动",
      "startTime": "2024-01-01 10:00:00",
      "endTime": "2024-01-01 12:00:00",
      "stock": 100,
      "price": 99.00
    }
  ]
}
```

### 仪表盘统计

#### 1. 获取统计数据

**GET** `/admin/dashboard/stats`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
{
  "userCount": 1000,
  "postCount": 5000,
  "topicCount": 50,
  "productCount": 200,
  "todayNewUsers": 10,
  "todayNewPosts": 50,
  "todayActiveUsers": 100
}
```

### 关注统计

#### 1. 关注增长趋势

**GET** `/admin/follow/statistics/growth`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| days | Integer | 否 | 7 | 统计天数 |

**响应**:
```json
[
  {
    "name": "2024-01-01",
    "value": 100
  },
  {
    "name": "2024-01-02",
    "value": 120
  }
]
```

#### 2. 关注类型分布

**GET** `/admin/follow/statistics/distribution`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**响应**:
```json
{
  "user": 5000,
  "topic": 3000,
  "product": 2000
}
```

#### 3. 关注最多的用户

**GET** `/admin/follow/statistics/top/users`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| limit | Integer | 否 | 10 | 返回数量 |

**响应**:
```json
[
  {
    "name": "用户A",
    "value": 1000
  },
  {
    "name": "用户B",
    "value": 800
  }
]
```

#### 4. 关注最多的话题

**GET** `/admin/follow/statistics/top/topics`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| limit | Integer | 否 | 10 | 返回数量 |

**响应**:
```json
[
  {
    "name": "数码科技",
    "value": 5000
  }
]
```

#### 5. 关注最多的产品

**GET** `/admin/follow/statistics/top/products`

**请求头**:
```
Authorization: Bearer {accessToken}
```

**查询参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|-------|------|
| limit | Integer | 否 | 10 | 返回数量 |

**响应**:
```json
[
  {
    "name": "iPhone 15 Pro",
    "value": 3000
  }
]
```

---

## WebSocket 接口

### 连接地址

```
ws://localhost:8082/ws/chat
```

### 发送消息格式

```json
{
  "messageId": 1704067200000,
  "userId": 1,
  "targetUserId": 2,
  "content": "你好"
}
```

### 接收消息格式

```json
{
  "type": "message",
  "fromUserId": 1,
  "content": "你好",
  "time": 1704067200000
}
```

### 消息确认格式

```json
{
  "type": "confirm",
  "status": "success",
  "messageId": 1704067200000
}
```

### 通知推送格式

```json
{
  "type": "notification",
  "data": {
    "notifyType": "COMMENT",
    "content": "有人评论了你的帖子"
  },
  "time": 1704067200000
}
```

---

## 错误码说明

| 错误码 | 说明 |
|-------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权/Token过期 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 1001 | 用户名已存在 |
| 1002 | 密码错误 |
| 1003 | 用户不存在 |
| 2001 | 帖子不存在 |
| 2002 | 无权操作此帖子 |
| 3001 | 评论不存在 |
| 4001 | 话题不存在 |
| 5001 | 产品不存在 |
| 6001 | 秒杀活动不存在 |
| 6002 | 秒杀库存不足 |
| 6003 | 重复参与秒杀 |
