CREATE TABLE IF NOT EXISTS `decoration` (
    `decoration_id` int NOT NULL AUTO_INCREMENT COMMENT '装扮ID',
    `decoration_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '装扮关键字（唯一）',
    `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '装扮名称（title 类型即头衔文本）',
    `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '装扮描述',
    `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型：badge/avatar_frame/title',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1=DRAFT，2=PUBLISHED，3=ARCHIVED',
    `draft_config` json NULL DEFAULT NULL COMMENT '编辑中的结构化配置',
    `published_config` json NULL DEFAULT NULL COMMENT '已发布配置（用户端只读此字段）',
    `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `published_at` datetime NULL DEFAULT NULL COMMENT '最近发布时间',
    `created_by` int NULL DEFAULT NULL COMMENT '创建管理员账号ID',
    `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '软删除',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`decoration_id`) USING BTREE,
    UNIQUE KEY `uk_decoration_key` (`decoration_key`),
    KEY `idx_decoration_status_type` (`status`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='装扮设计表（低代码平台）';

ALTER TABLE `shop_item`
    ADD COLUMN `decoration_id` int NULL DEFAULT NULL COMMENT '绑定的装扮ID（可空，空则前端回退 item_key 硬编码渲染）' AFTER `item_type`,
    ADD KEY `idx_shop_item_decoration` (`decoration_id`),
    ADD CONSTRAINT `shop_item_ibfk_decoration` FOREIGN KEY (`decoration_id`) REFERENCES `decoration` (`decoration_id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
