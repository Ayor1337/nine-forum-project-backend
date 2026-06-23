CREATE TABLE IF NOT EXISTS `feedback` (
    `feedback_id` int NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
    `account_id` int NOT NULL COMMENT '提交用户账号ID',
    `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '反馈类型',
    `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '反馈内容',
    `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '处理状态',
    `handler_account_id` int NULL DEFAULT NULL COMMENT '处理管理员账号ID',
    `handle_note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理备注',
    `handled_at` datetime NULL DEFAULT NULL COMMENT '处理完成时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`feedback_id`) USING BTREE,
    INDEX `idx_feedback_account_time` (`account_id`, `create_time`) USING BTREE,
    INDEX `idx_feedback_status_time` (`status`, `create_time`) USING BTREE,
    INDEX `idx_feedback_type_time` (`type`, `create_time`) USING BTREE,
    CONSTRAINT `fk_feedback_account`
        FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `fk_feedback_handler`
        FOREIGN KEY (`handler_account_id`) REFERENCES `account` (`account_id`)
        ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;
