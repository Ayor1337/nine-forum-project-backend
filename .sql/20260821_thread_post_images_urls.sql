-- 先执行本迁移，再部署使用 images_urls 的应用版本。
-- 不从历史 TipTap 正文回填图片；旧正文图片格式不再兼容。

ALTER TABLE `thread`
    ADD COLUMN `images_urls` JSON NOT NULL DEFAULT (JSON_ARRAY()) COMMENT '独立图片 URL 数组' AFTER `content`;

ALTER TABLE `post`
    ADD COLUMN `images_urls` JSON NOT NULL DEFAULT (JSON_ARRAY()) COMMENT '独立图片 URL 数组' AFTER `content`;
