CREATE TABLE IF NOT EXISTS `credit_account` (
    `account_id` int NOT NULL COMMENT '账号ID',
    `balance` bigint NOT NULL DEFAULT 0 COMMENT 'Credit 余额（整数，非负）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`account_id`) USING BTREE,
    CONSTRAINT `credit_account_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `chk_credit_account_balance` CHECK (`balance` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Credit 货币余额表';

CREATE TABLE IF NOT EXISTS `credit_transaction` (
    `transaction_id` bigint NOT NULL AUTO_INCREMENT COMMENT '流水ID',
    `account_id` int NOT NULL COMMENT '账号ID',
    `delta` bigint NOT NULL COMMENT '变动数量：正=发放，负=扣减',
    `balance_after` bigint NOT NULL COMMENT '变动后余额快照',
    `change_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '变动类型：admin_grant/admin_deduct',
    `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '变动备注',
    `operator_id` int NOT NULL COMMENT '操作管理员账号ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`transaction_id`) USING BTREE,
    KEY `idx_credit_transaction_account_time` (`account_id`, `create_time`),
    KEY `idx_credit_transaction_operator_time` (`operator_id`, `create_time`),
    CONSTRAINT `credit_transaction_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `credit_transaction_ibfk_2` FOREIGN KEY (`operator_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Credit 货币流水表';
