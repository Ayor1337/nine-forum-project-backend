CREATE TABLE IF NOT EXISTS `follow_message` (
    `follow_message_id` int NOT NULL AUTO_INCREMENT,
    `account_id` int NOT NULL,
    `from_account_id` int NOT NULL,
    `thread_id` int NOT NULL,
    `topic_id` int NOT NULL,
    `path` varchar(255) NOT NULL,
    `title` varchar(255) NOT NULL,
    `content_summary` varchar(255) NOT NULL DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`follow_message_id`) USING BTREE,
    KEY `idx_follow_message_account_time` (`account_id`, `create_time`),
    KEY `idx_follow_message_from_account` (`from_account_id`),
    KEY `idx_follow_message_thread` (`thread_id`),
    KEY `idx_follow_message_topic` (`topic_id`),
    CONSTRAINT `follow_message_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `follow_message_ibfk_2` FOREIGN KEY (`from_account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
