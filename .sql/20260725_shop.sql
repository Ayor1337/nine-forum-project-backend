CREATE TABLE IF NOT EXISTS `shop_item` (
    `item_id` int NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称（title 类型即头衔文本）',
    `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品描述',
    `item_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型：badge/avatar_frame/title',
    `price` bigint NOT NULL COMMENT '售价（Credit，整数）',
    `stock` bigint NOT NULL DEFAULT -1 COMMENT '库存，-1=不限量',
    `purchase_limit` int NOT NULL DEFAULT 0 COMMENT '每人限购数量，0=不限购（预留堆叠道具，装饰类天然限购1件）',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1=上架，2=下架',
    `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '软删除',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`item_id`) USING BTREE,
    KEY `idx_shop_item_status_type` (`status`, `item_type`),
    CONSTRAINT `chk_shop_item_price` CHECK (`price` >= 0),
    CONSTRAINT `chk_shop_item_stock` CHECK (`stock` >= -1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商城商品表';

CREATE TABLE IF NOT EXISTS `user_item` (
    `user_item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '背包记录ID',
    `account_id` int NOT NULL COMMENT '账号ID',
    `item_id` int NOT NULL COMMENT '商品ID',
    `quantity` int NOT NULL DEFAULT 1 COMMENT '持有数量（装饰类恒为1，预留堆叠道具）',
    `is_equipped` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已装备',
    `acquire_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
    PRIMARY KEY (`user_item_id`) USING BTREE,
    UNIQUE KEY `uk_user_item` (`account_id`, `item_id`),
    KEY `idx_user_item_equipped` (`account_id`, `is_equipped`),
    CONSTRAINT `user_item_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `user_item_ibfk_2` FOREIGN KEY (`item_id`) REFERENCES `shop_item` (`item_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户背包表';

CREATE TABLE IF NOT EXISTS `shop_order` (
    `order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `account_id` int NOT NULL COMMENT '买家账号ID',
    `item_id` int NOT NULL COMMENT '商品ID',
    `price` bigint NOT NULL COMMENT '成交单价快照',
    `quantity` int NOT NULL DEFAULT 1 COMMENT '购买数量',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1=成功，2=已退款（预留）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`order_id`) USING BTREE,
    KEY `idx_shop_order_account_time` (`account_id`, `create_time`),
    KEY `idx_shop_order_item_time` (`item_id`, `create_time`),
    CONSTRAINT `shop_order_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `shop_order_ibfk_2` FOREIGN KEY (`item_id`) REFERENCES `shop_item` (`item_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商城购买记录表';
