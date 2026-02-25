-- 添加用户在线状态字段
ALTER TABLE `user` ADD COLUMN `online_status` TINYINT(1) DEFAULT 0 COMMENT '在线状态：0-离线，1-在线，2-忙碌，3-离开';
ALTER TABLE `user` ADD COLUMN `last_active_time` DATETIME DEFAULT NULL COMMENT '最后活跃时间';
ALTER TABLE `user` ADD COLUMN `heartbeat_time` DATETIME DEFAULT NULL COMMENT '最后心跳时间';

-- 添加在线状态索引
CREATE INDEX idx_user_online_status ON `user` (`online_status`);
-- 添加最后活跃时间索引
CREATE INDEX idx_user_last_active_time ON `user` (`last_active_time`);
