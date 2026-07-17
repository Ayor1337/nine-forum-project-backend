CREATE TABLE IF NOT EXISTS `conversation` (
    `conversation_id` int NOT NULL AUTO_INCREMENT,
    `alpha_account_id` int NOT NULL,
    `beta_account_id` int NOT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
    `hidden` tinyint NOT NULL DEFAULT 0 COMMENT '0=双方可见,1=alpha隐藏,2=beta隐藏,3=双方隐藏',
    PRIMARY KEY (`conversation_id`) USING BTREE,
    UNIQUE KEY `uk_conversation_pair` (`alpha_account_id`, `beta_account_id`),
    KEY `idx_conversation_alpha_update` (`alpha_account_id`, `update_time`),
    KEY `idx_conversation_beta_update` (`beta_account_id`, `update_time`),
    CONSTRAINT `conversation_ibfk_1` FOREIGN KEY (`alpha_account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `conversation_ibfk_2` FOREIGN KEY (`beta_account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `chk_conversation_no_self` CHECK (`alpha_account_id` <> `beta_account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `conversation_message` (
    `conversation_message_id` int NOT NULL AUTO_INCREMENT,
    `conversation_id` int NOT NULL,
    `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `account_id` int NOT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
    `is_edit` tinyint(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`conversation_message_id`) USING BTREE,
    KEY `idx_conversation_message_conversation_time` (`conversation_id`, `create_time`, `conversation_message_id`),
    KEY `idx_conversation_message_account` (`account_id`),
    CONSTRAINT `conversation_message_ibfk_1` FOREIGN KEY (`conversation_id`) REFERENCES `conversation` (`conversation_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT `conversation_message_ibfk_2` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `conversation_user_setting` (
    `conversation_user_setting_id` int NOT NULL AUTO_INCREMENT,
    `conversation_id` int NOT NULL,
    `account_id` int NOT NULL,
    `pinned` tinyint(1) NOT NULL DEFAULT 0,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`conversation_user_setting_id`) USING BTREE,
    UNIQUE KEY `uk_conversation_user` (`conversation_id`, `account_id`),
    KEY `idx_conversation_user_setting_account_pinned` (`account_id`, `pinned`, `update_time`),
    CONSTRAINT `conversation_user_setting_ibfk_1` FOREIGN KEY (`conversation_id`) REFERENCES `conversation` (`conversation_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT `conversation_user_setting_ibfk_2` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'conversation'
      AND column_name = 'is_deleted'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `conversation` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'conversation'
      AND column_name = 'hidden'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `conversation` ADD COLUMN `hidden` tinyint NOT NULL DEFAULT 0 COMMENT ''0=双方可见,1=alpha隐藏,2=beta隐藏,3=双方隐藏''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE `conversation_message`
    MODIFY COLUMN `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL;

SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'conversation_message'
      AND column_name = 'is_deleted'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `conversation_message` ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'conversation_message'
      AND column_name = 'is_edit'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `conversation_message` ADD COLUMN `is_edit` tinyint(1) NOT NULL DEFAULT 0',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'conversation'
      AND index_name = 'idx_conversation_alpha_update'
);
SET @sql = IF(@idx_exists = 0,
    'ALTER TABLE `conversation` ADD INDEX `idx_conversation_alpha_update` (`alpha_account_id`, `update_time`)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'conversation'
      AND index_name = 'idx_conversation_beta_update'
);
SET @sql = IF(@idx_exists = 0,
    'ALTER TABLE `conversation` ADD INDEX `idx_conversation_beta_update` (`beta_account_id`, `update_time`)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'conversation_message'
      AND index_name = 'idx_conversation_message_conversation_time'
);
SET @sql = IF(@idx_exists = 0,
    'ALTER TABLE `conversation_message` ADD INDEX `idx_conversation_message_conversation_time` (`conversation_id`, `create_time`, `conversation_message_id`)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
