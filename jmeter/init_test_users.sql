-- 生成压测用户数据
-- 密码统一为 Test@123456 (BCrypt加密后的值: $2a$10$t2zs5W0JnuXUHCjOWkwWi.79KPu8d4ynVU9K4GmhErBGLddFTWTnC)

-- 先清空可能存在的测试用户
DELETE FROM user WHERE username LIKE 'testuser%' OR username LIKE 'stress_user%';

-- 插入10个基础测试用户
INSERT INTO user (username, password, nickname, avatar, email, phone, bio, gender, status, role, following_count, follower_count, post_count, points, create_time, update_time) VALUES
('testuser1', '$2a$10$t2zs5W0JnuXUHCjOWkwWi.79KPu8d4ynVU9K4GmhErBGLddFTWTnC', '测试用户1', NULL, 'test1@test.com', '13800000001', '这是测试用户1', 0, 1, 0, 0, 0, 0, 0, NOW(), NOW()),
('testuser2', '$2a$10$t2zs5W0JnuXUHCjOWkwWi.79KPu8d4ynVU9K4GmhErBGLddFTWTnC', '测试用户2', NULL, 'test2@test.com', '13800000002', '这是测试用户2', 0, 1, 0, 0, 0, 0, 0, NOW(), NOW()),
('testuser3', '$2a$10$t2zs5W0JnuXUHCjOWkwWi.79KPu8d4ynVU9K4GmhErBGLddFTWTnC', '测试用户3', NULL, 'test3@test.com', '13800000003', '这是测试用户3', 0, 1, 0, 0, 0, 0, 0, NOW(), NOW()),
('testuser4', '$2a$10$t2zs5W0JnuXUHCjOWkwWi.79KPu8d4ynVU9K4GmhErBGLddFTWTnC', '测试用户4', NULL, 'test4@test.com', '13800000004', '这是测试用户4', 0, 1, 0, 0, 0, 0, 0, NOW(), NOW()),
('testuser5', '$2a$10$t2zs5W0JnuXUHCjOWkwWi.79KPu8d4ynVU9K4GmhErBGLddFTWTnC', '测试用户5', NULL, 'test5@test.com', '13800000005', '这是测试用户5', 0, 1, 0, 0, 0, 0, 0, NOW(), NOW()),
('testuser6', '$2a$10$t2zs5W0JnuXUHCjOWkwWi.79KPu8d4ynVU9K4GmhErBGLddFTWTnC', '测试用户6', NULL, 'test6@test.com', '13800000006', '这是测试用户6', 0, 1, 0, 0, 0, 0, 0, NOW(), NOW()),
('testuser7', '$2a$10$t2zs5W0JnuXUHCjOWkwWi.79KPu8d4ynVU9K4GmhErBGLddFTWTnC', '测试用户7', NULL, 'test7@test.com', '13800000007', '这是测试用户7', 0, 1, 0, 0, 0, 0, 0, NOW(), NOW()),
('testuser8', '$2a$10$t2zs5W0JnuXUHCjOWkwWi.79KPu8d4ynVU9K4GmhErBGLddFTWTnC', '测试用户8', NULL, 'test8@test.com', '13800000008', '这是测试用户8', 0, 1, 0, 0, 0, 0, 0, NOW(), NOW()),
('testuser9', '$2a$10$t2zs5W0JnuXUHCjOWkwWi.79KPu8d4ynVU9K4GmhErBGLddFTWTnC', '测试用户9', NULL, 'test9@test.com', '13800000009', '这是测试用户9', 0, 1, 0, 0, 0, 0, 0, NOW(), NOW()),
('testuser10', '$2a$10$t2zs5W0JnuXUHCjOWkwWi.79KPu8d4ynVU9K4GmhErBGLddFTWTnC', '测试用户10', NULL, 'test10@test.com', '13800000010', '这是测试用户10', 0, 1, 0, 0, 0, 0, 0, NOW(), NOW());

-- 批量生成更多测试用户 (stress_user_1 到 stress_user_100)
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS generate_stress_test_users()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 100 DO
        INSERT INTO user (username, password, nickname, avatar, email, phone, bio, gender, status, role, following_count, follower_count, post_count, points, create_time, update_time)
        VALUES (
            CONCAT('stress_user_', i),
            '$2a$10$t2zs5W0JnuXUHCjOWkwWi.79KPu8d4ynVU9K4GmhErBGLddFTWTnC',
            CONCAT('压测用户', i),
            NULL,
            CONCAT('stress', i, '@test.com'),
            CONCAT('13900000', LPAD(i, 4, '0')),
            CONCAT('这是压测用户', i),
            0, 1, 0, 0, 0, 0, 0, NOW(), NOW()
        );
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

-- 执行存储过程
CALL generate_stress_test_users();

-- 删除存储过程
DROP PROCEDURE IF EXISTS generate_stress_test_users;

-- 验证插入结果
SELECT COUNT(*) as '测试用户数量' FROM user WHERE username LIKE 'testuser%' OR username LIKE 'stress_user%';
SELECT id, username, nickname, status FROM user WHERE username LIKE 'testuser%' OR username LIKE 'stress_user%' LIMIT 20;

