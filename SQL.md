# Cool Community SQL 语句汇总

## 目录

- [用户模块](#用户模块)
- [帖子模块](#帖子模块)
- [评论模块](#评论模块)
- [话题模块](#话题模块)
- [产品模块](#产品模块)
- [关注模块](#关注模块)
- [消息模块](#消息模块)
- [聊天模块](#聊天模块)
- [秒杀模块](#秒杀模块)
- [签到模块](#签到模块)
- [积分模块](#积分模块)
- [用户背景模块](#用户背景模块)
- [仪表盘统计](#仪表盘统计)

---

## 用户模块

### 1. 统计用户数量

```sql
SELECT COUNT(*) FROM user WHERE deleted = 0
```

**条件过滤**：
- `status` - 用户状态
- `role` - 用户角色
- `keyword` - 用户名或昵称模糊搜索

### 2. 查询用户列表

```sql
SELECT id, username, nickname, avatar, email, phone, bio, gender, status, role, 
       following_count, follower_count, post_count
FROM user WHERE deleted = 0
ORDER BY create_time DESC
LIMIT #{pageSize} OFFSET #{offset}
```

### 3. 查询用户关注列表

```sql
SELECT u.id, u.username, u.nickname, u.avatar, u.bio, u.follower_count, u.following_count, u.post_count
FROM follow f
LEFT JOIN user u ON f.follow_id = u.id
WHERE f.user_id = #{userId} AND f.type = 0 AND f.deleted = 0 AND u.deleted = 0
ORDER BY f.create_time DESC
LIMIT #{pageSize} OFFSET #{offset}
```

### 4. 查询用户粉丝列表

```sql
SELECT u.id, u.username, u.nickname, u.avatar, u.bio, u.follower_count, u.following_count, u.post_count
FROM follow f
LEFT JOIN user u ON f.user_id = u.id
WHERE f.follow_id = #{userId} AND f.type = 0 AND f.deleted = 0 AND u.deleted = 0
ORDER BY f.create_time DESC
LIMIT #{pageSize} OFFSET #{offset}
```

### 5. 查询推荐用户

```sql
SELECT id, username, nickname, avatar, bio, follower_count, following_count, post_count
FROM user 
WHERE id != #{userId} AND deleted = 0 
AND id NOT IN (SELECT follow_id FROM follow WHERE user_id = #{userId} AND type = 0 AND deleted = 0)
ORDER BY follower_count DESC, create_time DESC
LIMIT #{pageSize} OFFSET #{offset}
```

---

## 帖子模块

### 1. 统计帖子数量

```sql
SELECT COUNT(*) FROM post
WHERE deleted = 0
```

**条件过滤**：
- `topicId` - 话题ID
- `userId` - 用户ID
- `type` - 帖子类型
- `productId` - 产品ID
- `status` - 帖子状态
- `keyword` - 标题或内容模糊搜索

### 2. 查询帖子列表

```sql
SELECT p.id, p.user_id, p.topic_id, p.product_id, p.title, p.content, p.images, 
       p.type, p.like_count, p.comment_count, p.collect_count, p.view_count,
       p.status, p.is_top, p.is_essence, p.create_time,
       u.username, u.avatar as user_avatar, u.nickname as user_nickname, 
       t.name as topic_name,
       pr.rating as product_rating
FROM post p
LEFT JOIN user u ON p.user_id = u.id
LEFT JOIN topic t ON p.topic_id = t.id
LEFT JOIN product_review pr ON p.id = pr.post_id
WHERE p.deleted = 0
ORDER BY p.is_top DESC, p.create_time DESC
LIMIT #{pageSize} OFFSET #{offset}
```

### 3. 查询帖子详情

```sql
SELECT p.id, p.user_id, p.topic_id, p.product_id, p.title, p.content, p.images, 
       p.type, p.like_count, p.comment_count, p.collect_count, p.view_count,
       p.status, p.is_top, p.is_essence, p.create_time,
       u.username, u.avatar as user_avatar, u.nickname as user_nickname, 
       t.name as topic_name,
       pr.rating as product_rating
FROM post p
LEFT JOIN user u ON p.user_id = u.id
LEFT JOIN topic t ON p.topic_id = t.id
LEFT JOIN product_review pr ON p.id = pr.post_id
WHERE p.id = #{id} AND p.deleted = 0
```

---

## 评论模块

### 1. 查询帖子评论列表

```sql
SELECT c.*, u.username, u.avatar as user_avatar, u.nickname as user_nickname, 
       ru.username as reply_username
FROM comment c
LEFT JOIN user u ON c.user_id = u.id
LEFT JOIN user ru ON c.reply_user_id = ru.id
WHERE c.post_id = #{postId} AND c.deleted = 0
ORDER BY c.create_time DESC
```

---

## 话题模块

### 1. 统计话题数量

```sql
SELECT COUNT(*) FROM topic
WHERE deleted = 0
```

**条件过滤**：
- `status` - 话题状态
- `category` - 话题分类
- `keyword` - 话题名称模糊搜索

### 2. 查询话题列表

```sql
SELECT id, name, description, icon, cover, category, follow_count, post_count, is_hot
FROM topic
WHERE deleted = 0
ORDER BY follow_count DESC
LIMIT #{pageSize} OFFSET #{offset}
```

### 3. 查询热门话题

```sql
SELECT id, name, description, icon, cover, category, follow_count, post_count, is_hot
FROM topic
WHERE is_hot = 1 AND deleted = 0
ORDER BY follow_count DESC
LIMIT #{pageSize} OFFSET #{offset}
```

### 4. 查询话题详情

```sql
SELECT id, name, description, icon, cover, category, follow_count, post_count, is_hot
FROM topic WHERE id = #{id} AND deleted = 0
```

### 5. 查询推荐话题

```sql
SELECT id, name, description, icon, cover, category, follow_count, post_count, is_hot
FROM topic 
WHERE deleted = 0 
AND id NOT IN (SELECT follow_id FROM follow WHERE user_id = #{userId} AND type = 1 AND deleted = 0)
ORDER BY follow_count DESC, post_count DESC
LIMIT #{pageSize} OFFSET #{offset}
```

---

## 产品模块

### 1. 统计产品数量

```sql
SELECT COUNT(*) FROM product
WHERE deleted = 0
```

**条件过滤**：
- `status` - 产品状态
- `categoryId` - 分类ID
- `brand` - 品牌
- `keyword` - 名称或品牌模糊搜索

### 2. 查询产品列表

```sql
SELECT p.id, p.name, p.description, p.image, p.brand, p.category_id, p.price, p.specs, p.review_count,
       pc.name as categoryName,
       (SELECT AVG(rating) FROM product_review WHERE product_id = p.id AND deleted = 0) as avgRating
FROM product p
LEFT JOIN product_category pc ON p.category_id = pc.id
WHERE p.deleted = 0
ORDER BY p.create_time DESC
LIMIT #{pageSize} OFFSET #{offset}
```

### 3. 查询产品详情

```sql
SELECT p.id, p.name, p.description, p.image, p.brand, p.category_id, p.price, p.specs, p.review_count,
       pc.name as categoryName,
       (SELECT AVG(rating) FROM product_review WHERE product_id = p.id AND deleted = 0) as avgRating
FROM product p
LEFT JOIN product_category pc ON p.category_id = pc.id
WHERE p.id = #{id} AND p.deleted = 0
```

### 4. 查询推荐产品

```sql
SELECT p.id, p.name, p.description, p.image, p.brand, p.category_id, p.price, p.specs, p.review_count, p.follow_count,
       pc.name as categoryName,
       (SELECT AVG(rating) FROM product_review WHERE product_id = p.id AND deleted = 0) as avgRating
FROM product p
LEFT JOIN product_category pc ON p.category_id = pc.id
WHERE p.deleted = 0
ORDER BY p.review_count DESC, p.follow_count DESC
LIMIT #{pageSize} OFFSET #{offset}
```

---

## 关注模块

### 1. 查询关注的用户列表

```sql
SELECT u.id, u.username, u.nickname, u.avatar, u.bio, u.follower_count, u.following_count, u.post_count
FROM follow f
LEFT JOIN user u ON f.follow_id = u.id
WHERE f.user_id = #{userId} AND f.type = 0 AND f.deleted = 0 AND u.deleted = 0
ORDER BY f.create_time DESC
LIMIT #{pageSize} OFFSET #{offset}
```

### 2. 查询粉丝列表

```sql
SELECT u.id, u.username, u.nickname, u.avatar, u.bio, u.follower_count, u.following_count, u.post_count
FROM follow f
LEFT JOIN user u ON f.user_id = u.id
WHERE f.follow_id = #{userId} AND f.type = 0 AND f.deleted = 0 AND u.deleted = 0
ORDER BY f.create_time DESC
LIMIT #{pageSize} OFFSET #{offset}
```

### 3. 查询关注的话题列表

```sql
SELECT t.id, t.name, t.description, t.icon, t.cover, t.category, t.follow_count, t.post_count, t.is_hot
FROM follow f
LEFT JOIN topic t ON f.follow_id = t.id
WHERE f.user_id = #{userId} AND f.type = 1 AND f.deleted = 0 AND t.deleted = 0
ORDER BY f.create_time DESC
LIMIT #{pageSize} OFFSET #{offset}
```

### 4. 查询话题关注者列表

```sql
SELECT u.id, u.username, u.nickname, u.avatar, u.bio, u.follower_count, u.following_count, u.post_count
FROM follow f
LEFT JOIN user u ON f.user_id = u.id
WHERE f.follow_id = #{topicId} AND f.type = 1 AND f.deleted = 0 AND u.deleted = 0
ORDER BY f.create_time DESC
LIMIT #{pageSize} OFFSET #{offset}
```

### 5. 查询关注的产品列表

```sql
SELECT p.id, p.name, p.description, p.image, p.brand, p.category_id as categoryId, p.price, p.specs, 
       p.review_count as reviewCount, p.follow_count as followCount, p.status
FROM follow f
LEFT JOIN product p ON f.follow_id = p.id
WHERE f.user_id = #{userId} AND f.type = 2 AND f.deleted = 0 AND p.deleted = 0
ORDER BY f.create_time DESC
LIMIT #{pageSize} OFFSET #{offset}
```

### 6. 查询产品关注者列表

```sql
SELECT u.id, u.username, u.nickname, u.avatar, u.bio, u.follower_count, u.following_count, u.post_count
FROM follow f
LEFT JOIN user u ON f.user_id = u.id
WHERE f.follow_id = #{productId} AND f.type = 2 AND f.deleted = 0 AND u.deleted = 0
ORDER BY f.create_time DESC
LIMIT #{pageSize} OFFSET #{offset}
```

---

## 消息模块

### 1. 插入消息

```sql
INSERT INTO message (from_user_id, to_user_id, type, content, related_id, is_read)
VALUES (#{fromUserId}, #{toUserId}, #{type}, #{content}, #{relatedId}, 0)
```

### 2. 查询消息详情

```sql
SELECT m.*, 
       fu.username as from_username, fu.nickname as from_user_nickname, fu.avatar as from_user_avatar,
       tu.username as to_username, tu.nickname as to_user_nickname, tu.avatar as to_user_avatar
FROM message m
LEFT JOIN user fu ON m.from_user_id = fu.id
LEFT JOIN user tu ON m.to_user_id = tu.id
WHERE m.id = #{id}
```

### 3. 查询消息列表

```sql
SELECT m.*, 
       fu.username as from_username, fu.nickname as from_user_nickname, fu.avatar as from_user_avatar,
       tu.username as to_username, tu.nickname as to_user_nickname, tu.avatar as to_user_avatar
FROM message m
LEFT JOIN user fu ON m.from_user_id = fu.id
LEFT JOIN user tu ON m.to_user_id = tu.id
WHERE m.to_user_id = #{userId} AND m.deleted = 0
ORDER BY m.create_time DESC
LIMIT #{pageSize} OFFSET #{offset}
```

### 4. 统计消息数量

```sql
SELECT COUNT(*) FROM message WHERE to_user_id = #{userId} AND deleted = 0
```

### 5. 标记消息已读

```sql
UPDATE message SET is_read = 1 WHERE id = #{id}
```

### 6. 标记所有消息已读

```sql
UPDATE message SET is_read = 1 WHERE to_user_id = #{userId} AND is_read = 0 AND deleted = 0
```

### 7. 删除消息（软删除）

```sql
UPDATE message SET deleted = 1 WHERE id = #{id}
```

### 8. 统计未读消息数量

```sql
SELECT COUNT(*) FROM message WHERE to_user_id = #{userId} AND is_read = 0 AND deleted = 0
```

### 9. 查询会话消息

```sql
SELECT m.*, 
       fu.username as from_username, fu.nickname as from_user_nickname, fu.avatar as from_user_avatar,
       tu.username as to_username, tu.nickname as to_user_nickname, tu.avatar as to_user_avatar
FROM message m
LEFT JOIN user fu ON m.from_user_id = fu.id
LEFT JOIN user tu ON m.to_user_id = tu.id
WHERE ((m.from_user_id = #{userId1} AND m.to_user_id = #{userId2}) 
   OR (m.from_user_id = #{userId2} AND m.to_user_id = #{userId1}))
   AND m.deleted = 0
ORDER BY m.create_time ASC
LIMIT #{pageSize} OFFSET #{offset}
```

### 10. 统计会话消息数量

```sql
SELECT COUNT(*) FROM message 
WHERE ((from_user_id = #{userId1} AND to_user_id = #{userId2}) 
   OR (from_user_id = #{userId2} AND to_user_id = #{userId1}))
   AND deleted = 0
```

### 11. 查询会话列表

```sql
SELECT 
    m.*,
    u.id as from_user_id,
    u.username as from_username,
    u.nickname as from_user_nickname,
    u.avatar as from_user_avatar,
    (SELECT content FROM message m2 
     WHERE ((m2.from_user_id = m.from_user_id AND m2.to_user_id = m.to_user_id)
        OR (m2.from_user_id = m.to_user_id AND m2.to_user_id = m.from_user_id))
        AND m2.deleted = 0
     ORDER BY m2.create_time DESC LIMIT 1) as last_content,
    (SELECT create_time FROM message m2 
     WHERE ((m2.from_user_id = m.from_user_id AND m2.to_user_id = m.to_user_id)
        OR (m2.from_user_id = m.to_user_id AND m2.to_user_id = m.from_user_id))
        AND m2.deleted = 0
     ORDER BY m2.create_time DESC LIMIT 1) as last_time
FROM (
    SELECT 
        CASE WHEN from_user_id = #{userId} THEN to_user_id ELSE from_user_id END as from_user_id,
        MAX(create_time) as create_time
    FROM message
    WHERE (from_user_id = #{userId} OR to_user_id = #{userId}) AND deleted = 0
    GROUP BY CASE WHEN from_user_id = #{userId} THEN to_user_id ELSE from_user_id END
) latest
JOIN message m ON m.create_time = latest.create_time 
    AND ((m.from_user_id = #{userId} AND m.to_user_id = latest.from_user_id)
        OR (m.to_user_id = #{userId} AND m.from_user_id = latest.from_user_id))
LEFT JOIN user u ON u.id = latest.from_user_id
WHERE m.deleted = 0
ORDER BY m.create_time DESC
```

---

## 聊天模块

### 1. 插入聊天消息

```sql
INSERT INTO chat_message (
    session_id, sender_id, receiver_id, content, 
    type, is_read, status, create_time, update_time
) VALUES (
    #{sessionId}, #{senderId}, #{receiverId}, #{content}, 
    #{type}, #{isRead}, #{status}, #{createTime}, #{updateTime}
)
```

### 2. 更新聊天消息

```sql
UPDATE chat_message SET
    content = #{content},
    type = #{type},
    is_read = #{isRead},
    status = #{status},
    update_time = #{updateTime}
WHERE id = #{id}
```

### 3. 查询聊天消息

```sql
SELECT * FROM chat_message WHERE id = #{id}
```

### 4. 查询会话消息列表

```sql
SELECT * FROM chat_message 
WHERE session_id = #{sessionId}
ORDER BY create_time ASC
LIMIT #{limit} OFFSET #{offset}
```

### 5. 查询未读消息

```sql
SELECT * FROM chat_message 
WHERE receiver_id = #{receiverId} AND is_read = 0
ORDER BY create_time DESC
```

### 6. 标记消息已读

```sql
UPDATE chat_message SET
    is_read = 1,
    update_time = NOW()
WHERE session_id = #{sessionId} AND receiver_id = #{receiverId} AND is_read = 0
```

### 7. 统计会话消息数

```sql
SELECT COUNT(*) FROM chat_message WHERE session_id = #{sessionId}
```

### 8. 统计未读消息数

```sql
SELECT COUNT(*) FROM chat_message WHERE receiver_id = #{receiverId} AND is_read = 0
```

### 9. 删除会话消息

```sql
DELETE FROM chat_message WHERE session_id = #{sessionId}
```

### 10. 删除消息

```sql
DELETE FROM chat_message WHERE id = #{id}
```

### 11. 插入聊天会话

```sql
INSERT INTO chat_session (
    user_id1, user_id2, last_message, last_message_time, 
    unread_count_user1, unread_count_user2, status, 
    create_time, update_time
) VALUES (
    #{userId1}, #{userId2}, #{lastMessage}, #{lastMessageTime}, 
    #{unreadCountUser1}, #{unreadCountUser2}, #{status}, 
    #{createTime}, #{updateTime}
)
```

### 12. 更新聊天会话

```sql
UPDATE chat_session SET
    last_message = #{lastMessage},
    last_message_time = #{lastMessageTime},
    unread_count_user1 = #{unreadCountUser1},
    unread_count_user2 = #{unreadCountUser2},
    status = #{status},
    update_time = #{updateTime}
WHERE id = #{id}
```

### 13. 查询会话

```sql
SELECT * FROM chat_session WHERE id = #{id}
```

### 14. 查询用户会话列表

```sql
SELECT * FROM chat_session 
WHERE (user_id1 = #{userId} OR user_id2 = #{userId}) 
AND status = 1
ORDER BY update_time DESC
```

### 15. 查询两个用户的会话

```sql
SELECT * FROM chat_session 
WHERE user_id1 = #{userId1} AND user_id2 = #{userId2}
AND status = 1
```

### 16. 更新会话最后消息

```sql
UPDATE chat_session SET
    last_message = #{lastMessage},
    last_message_time = #{lastMessageTime},
    update_time = NOW()
WHERE id = #{id}
```

### 17. 更新未读消息数

```sql
UPDATE chat_session SET
    unread_count_user1 = #{unreadCount}  -- 或 unread_count_user2
    update_time = NOW()
WHERE id = #{id}
```

### 18. 更新会话状态

```sql
UPDATE chat_session SET
    status = #{status},
    update_time = NOW()
WHERE id = #{id}
```

### 19. 删除会话

```sql
DELETE FROM chat_session WHERE id = #{id}
```

---

## 秒杀模块

### 1. 查询秒杀活动

```sql
SELECT * FROM seckill_activity WHERE id = #{id}
```

### 2. 按日期查询秒杀活动

```sql
SELECT * FROM seckill_activity 
WHERE DATE(start_time) = #{date} AND status != 3
```

### 3. 查询次日秒杀活动

```sql
SELECT * FROM seckill_activity 
WHERE DATE(start_time) = DATE_ADD(CURDATE(), INTERVAL 1 DAY) 
AND status != 3
LIMIT 1
```

### 4. 查询下一个秒杀活动

```sql
SELECT * FROM seckill_activity 
WHERE start_time > NOW() AND status != 3
ORDER BY start_time ASC
LIMIT 1
```

### 5. 创建秒杀活动

```sql
INSERT INTO seckill_activity (
    activity_name, background_image, start_time, end_time, 
    stock, status, create_time, update_time
) VALUES (
    #{activityName}, #{backgroundImage}, #{startTime}, #{endTime}, 
    #{stock}, #{status}, #{createTime}, #{updateTime}
)
```

### 6. 更新秒杀活动

```sql
UPDATE seckill_activity SET
    activity_name = #{activityName},
    background_image = #{backgroundImage},
    start_time = #{startTime},
    end_time = #{endTime},
    stock = #{stock},
    status = #{status},
    update_time = #{updateTime}
WHERE id = #{id}
```

### 7. 删除秒杀活动

```sql
DELETE FROM seckill_activity WHERE id = #{id}
```

### 8. 查询秒杀活动列表

```sql
SELECT * FROM seckill_activity ORDER BY create_time DESC
```

---

## 签到模块

### 1. 查询用户签到记录

```sql
SELECT * FROM user_checkin WHERE user_id = #{userId} AND checkin_date = #{checkinDate}
```

### 2. 插入签到记录

```sql
INSERT INTO user_checkin (user_id, checkin_date, created_at)
VALUES (#{userId}, #{checkinDate}, NOW())
```

### 3. 统计连续签到天数

```sql
SELECT COUNT(*)
FROM (
    SELECT checkin_date,
           DATEDIFF(checkin_date, '1970-01-01') - ROW_NUMBER() OVER (ORDER BY checkin_date) AS grp
    FROM user_checkin
    WHERE user_id = #{userId}
    ORDER BY checkin_date DESC
) t
WHERE grp = (SELECT DATEDIFF(MAX(checkin_date), '1970-01-01') - 1
             FROM user_checkin
             WHERE user_id = #{userId})
```

---

## 积分模块

### 1. 查询用户积分

```sql
SELECT * FROM user_points WHERE user_id = #{userId}
```

### 2. 创建用户积分记录

```sql
INSERT INTO user_points (user_id, points, version, update_at)
VALUES (#{userId}, #{points}, #{version}, NOW())
```

### 3. 增加积分（乐观锁）

```sql
UPDATE user_points 
SET points = points + #{points}, version = version + 1, update_at = NOW()
WHERE user_id = #{userId} AND version = #{version}
```

### 4. 减少积分（乐观锁）

```sql
UPDATE user_points 
SET points = points - #{points}, version = version + 1, update_at = NOW()
WHERE user_id = #{userId} AND version = #{version} AND points >= #{points}
```

### 5. 插入积分交易记录

```sql
INSERT INTO points_transaction (user_id, points, type, create_at)
VALUES (#{userId}, #{points}, #{type}, NOW())
```

---

## 用户背景模块

### 1. 插入背景图

```sql
INSERT INTO user_background (
    user_id, background_image, is_current, acquire_time, create_time
) VALUES (
    #{userId}, #{backgroundImage}, #{isCurrent}, #{acquireTime}, #{createTime}
)
```

### 2. 查询用户背景图列表

```sql
SELECT * FROM user_background WHERE user_id = #{userId} ORDER BY create_time DESC
```

### 3. 查询用户当前背景图

```sql
SELECT * FROM user_background WHERE user_id = #{userId} AND is_current = 1
```

### 4. 更新所有背景图为非当前

```sql
UPDATE user_background SET is_current = 0 WHERE user_id = #{userId}
```

### 5. 设置当前背景图

```sql
UPDATE user_background SET is_current = 1 WHERE id = #{id} AND user_id = #{userId}
```

---

## 仪表盘统计

### 1. 统计用户总数

```sql
SELECT COUNT(*) FROM user WHERE deleted = 0
```

### 2. 统计帖子总数

```sql
SELECT COUNT(*) FROM post WHERE deleted = 0
```

### 3. 统计话题总数

```sql
SELECT COUNT(*) FROM topic WHERE deleted = 0
```

### 4. 统计产品总数

```sql
SELECT COUNT(*) FROM product WHERE deleted = 0
```

### 5. 统计今日新增帖子

```sql
SELECT COUNT(*) FROM post WHERE deleted = 0 AND DATE(create_time) = CURDATE()
```

### 6. 统计今日新增用户

```sql
SELECT COUNT(*) FROM user WHERE deleted = 0 AND DATE(create_time) = CURDATE()
```

---

## 数据库表结构说明

### 核心表

| 表名 | 说明 |
|------|------|
| `user` | 用户表 |
| `post` | 帖子表 |
| `comment` | 评论表 |
| `topic` | 话题表 |
| `product` | 产品表 |
| `product_category` | 产品分类表 |
| `product_review` | 产品评测表 |
| `follow` | 关注表（用户/话题/产品） |
| `like` | 点赞表 |
| `collect` | 收藏表 |

### 消息相关表

| 表名 | 说明 |
|------|------|
| `message` | 私信消息表 |
| `chat_session` | 聊天会话表 |
| `chat_message` | 聊天消息表 |
| `notification` | 通知表 |

### 秒杀相关表

| 表名 | 说明 |
|------|------|
| `seckill_activity` | 秒杀活动表 |
| `user_seckill_record` | 用户秒杀记录表 |

### 用户相关表

| 表名 | 说明 |
|------|------|
| `user_points` | 用户积分表 |
| `user_checkin` | 用户签到表 |
| `user_background` | 用户背景图表 |
| `points_transaction` | 积分交易记录表 |

### 通知相关表

| 表名 | 说明 |
|------|------|
| `user_notification_main` | 用户通知主表 |
| `user_notification_comment` | 评论通知表 |
| `user_notification_like` | 点赞通知表 |
| `user_notification_follow` | 关注通知表 |
| `user_notification_system` | 系统通知表 |
