use cool_community;

-- 修改post表，添加帖子类型字段和产品ID字段
ALTER TABLE post ADD COLUMN `type` INT DEFAULT 1 COMMENT '帖子类型：1-话题贴，2-产品评测贴';
ALTER TABLE post ADD COLUMN `product_id` BIGINT DEFAULT NULL COMMENT '产品ID，当帖子类型为产品评测贴时使用';

-- 修改product_review表，添加帖子ID字段（如果不存在）
-- 注意：如果product_review表不存在，需要先创建
-- CREATE TABLE IF NOT EXISTS product_review (
--   id BIGINT PRIMARY KEY AUTO_INCREMENT,
--   product_id BIGINT NOT NULL COMMENT '产品ID',
--   post_id BIGINT NOT NULL COMMENT '帖子ID',
--   user_id BIGINT NOT NULL COMMENT '用户ID',
--   title VARCHAR(255) NOT NULL COMMENT '评测标题',
--   content TEXT NOT NULL COMMENT '评测内容',
--   rating INT NOT NULL COMMENT '评分，1-5星',
--   images VARCHAR(1000) DEFAULT NULL COMMENT '图片URL，多个用逗号分隔',
--   like_count INT DEFAULT 0 COMMENT '点赞数',
--   comment_count INT DEFAULT 0 COMMENT '评论数',
--   status INT DEFAULT 1 COMMENT '状态：1-正常，0-删除',
--   create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
--   update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
--   deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
--   FOREIGN KEY (product_id) REFERENCES product(id),
--   FOREIGN KEY (post_id) REFERENCES post(id),
--   FOREIGN KEY (user_id) REFERENCES user(id)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品评测表';

-- 如果product_review表已存在，添加post_id字段
ALTER TABLE product_review ADD COLUMN IF NOT EXISTS `post_id` BIGINT DEFAULT NULL COMMENT '帖子ID';
ALTER TABLE product_review ADD CONSTRAINT IF NOT EXISTS `fk_product_review_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`);

-- 创建索引，提高查询效率
CREATE INDEX IF NOT EXISTS `idx_post_type` ON `post` (`type`);
CREATE INDEX IF NOT EXISTS `idx_post_product_id` ON `post` (`product_id`);
CREATE INDEX IF NOT EXISTS `idx_product_review_post_id` ON `product_review` (`post_id`);


ALTER TABLE product_review ADD COLUMN IF NOT EXISTS `post_id` BIGINT DEFAULT NULL COMMENT '帖子ID';