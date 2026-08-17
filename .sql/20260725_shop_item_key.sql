ALTER TABLE `shop_item`
    ADD COLUMN `item_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品关键字（唯一，前端素材映射用）' AFTER `name`,
    ADD UNIQUE KEY `uk_shop_item_item_key` (`item_key`);
