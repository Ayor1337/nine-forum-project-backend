CREATE TABLE IF NOT EXISTS `daily_check_in` (
    `check_in_id` bigint NOT NULL AUTO_INCREMENT COMMENT '签到记录ID',
    `account_id` int NOT NULL COMMENT '账号ID',
    `check_in_date` date NOT NULL COMMENT '签到日期（东京自然日）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`check_in_id`) USING BTREE,
    UNIQUE KEY `uk_daily_check_in_account_date` (`account_id`, `check_in_date`),
    CONSTRAINT `daily_check_in_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='每日签到记录表';
