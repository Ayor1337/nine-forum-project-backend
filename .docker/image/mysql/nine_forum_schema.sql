/*
 Navicat Premium Dump SQL

 Source Server         : NIne数据库
 Source Server Type    : MySQL
 Source Server Version : 90300 (9.3.0)
 Source Host           : localhost:6033
 Source Schema         : nine_forum

 Target Server Type    : MySQL
 Target Server Version : 90300 (9.3.0)
 File Encoding         : 65001

 Date: 29/09/2025 16:29:44
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for account
-- ----------------------------
CREATE TABLE IF NOT EXISTS `account`  (
                               `account_id` int NOT NULL AUTO_INCREMENT COMMENT '账号ID',
                               `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名，用来登录',
                               `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
                               `nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户昵称',
                               `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
                               `status` tinyint NOT NULL COMMENT '账号状态: 1=ACTIVE, 2=MUTED, 3=BANNED',
                               `create_time` datetime NULL DEFAULT NULL COMMENT '账号创建时间',
                               `update_time` datetime NULL DEFAULT NULL COMMENT '账号更新时间',
                               `role_id` int NULL DEFAULT NULL COMMENT '权限等级',
                               PRIMARY KEY (`account_id`) USING BTREE,
                               INDEX `role_id`(`role_id` ASC) USING BTREE,
                               CONSTRAINT `db_account_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for account_stat
-- ----------------------------
CREATE TABLE IF NOT EXISTS `account_stat`  (
                                    `user_stat_id` int NOT NULL,
                                    `thread_count` int NULL DEFAULT NULL,
                                    `post_count` int NULL DEFAULT NULL,
                                    `reply_count` int NULL DEFAULT NULL,
                                    `liked_count` int NULL DEFAULT NULL,
                                    `collected_count` int NULL DEFAULT NULL,
                                    `following_count` int NULL DEFAULT NULL,
                                    `follower_count` int NULL DEFAULT NULL,
                                    `account_id` int NULL DEFAULT NULL,
                                    PRIMARY KEY (`user_stat_id`) USING BTREE,
                                    INDEX `account_id`(`account_id` ASC) USING BTREE,
                                    CONSTRAINT `db_account_stat_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of account_stat
-- ----------------------------

-- ----------------------------
-- Table structure for collect
-- ----------------------------
CREATE TABLE IF NOT EXISTS `collect`  (
                               `collect_id` int NOT NULL COMMENT '收藏ID',
                               `account_id` int NULL DEFAULT NULL COMMENT '用户ID',
                               `thread_id` int NULL DEFAULT NULL COMMENT '收藏的帖子',
                               PRIMARY KEY (`collect_id`) USING BTREE,
                               INDEX `account_id`(`account_id` ASC) USING BTREE,
                               CONSTRAINT `db_collect_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of collect
-- ----------------------------

-- ----------------------------
-- Table structure for history
-- ----------------------------
CREATE TABLE IF NOT EXISTS `history`  (
                               `history_id` int NOT NULL,
                               `thread_id` int NULL DEFAULT NULL,
                               `account_id` int NULL DEFAULT NULL,
                               `visit_time` datetime NULL DEFAULT NULL,
                               PRIMARY KEY (`history_id`) USING BTREE,
                               INDEX `thread_id`(`thread_id` ASC) USING BTREE,
                               INDEX `account_id`(`account_id` ASC) USING BTREE,
                               CONSTRAINT `db_history_ibfk_1` FOREIGN KEY (`thread_id`) REFERENCES `thread` (`thread_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                               CONSTRAINT `db_history_ibfk_2` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of history
-- ----------------------------

-- ----------------------------
-- Table structure for like_thread
-- ----------------------------
CREATE TABLE IF NOT EXISTS `like_thread`  (
                            `like_id` int NOT NULL COMMENT '喜欢ID',
                            `account_id` int NULL DEFAULT NULL COMMENT '谁喜欢',
                            `thread_id` int NULL DEFAULT NULL COMMENT '喜欢的帖子',
                            PRIMARY KEY (`like_id`) USING BTREE,
                            INDEX `account_id`(`account_id` ASC) USING BTREE,
                            CONSTRAINT `db_like_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of like_thread
-- ----------------------------

-- ----------------------------
-- Table structure for permission
-- ----------------------------
CREATE TABLE IF NOT EXISTS `permission`  (
                                  `permission_id` int NOT NULL AUTO_INCREMENT COMMENT '权限id',
                                  `role_id` int NOT NULL COMMENT '哪些权能者拥有权限',
                                  `grant` tinyint NOT NULL DEFAULT 0 COMMENT '是否可以给别人赋予权限, 只能给予比自己权能等级低的',
                                  `theme_edit` tinyint NOT NULL DEFAULT 0 COMMENT '是否可以编辑板块',
                                  `topic_edit` tinyint NOT NULL DEFAULT 0 COMMENT '是否可以编辑主题',
                                  `thread_delete` tinyint NOT NULL DEFAULT 0 COMMENT '是否可以编辑帖子, 只能删除, 受到fied_id管理',
                                  `account_muted` tinyint NOT NULL DEFAULT 0 COMMENT '是否可以禁言账号',
                                  `account_ban` tinyint NOT NULL DEFAULT 0 COMMENT '是否禁用账号',
                                  `can_select` tinyint NOT NULL DEFAULT 0 COMMENT '是否可以给帖子加精选, 受到fied_id管理',
                                  `topic_id` int NULL DEFAULT NULL COMMENT '(如果是版主则需要, 添加这个ID, 如果有这个ID 则最帖子的管理只能针对于某一个Topic)',
                                  PRIMARY KEY (`permission_id` DESC) USING BTREE,
                                  UNIQUE INDEX `role_id`(`role_id` ASC) USING BTREE,
                                  CONSTRAINT `db_permission_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Table structure for post
-- ----------------------------
CREATE TABLE IF NOT EXISTS `post`  (
                            `post_id` int NOT NULL AUTO_INCREMENT,
                            `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
                            `account_id` int NULL DEFAULT NULL,
                            `create_time` datetime NULL DEFAULT NULL,
                            `update_time` datetime NULL DEFAULT NULL,
                            `thread_id` int NULL DEFAULT NULL,
                            PRIMARY KEY (`post_id`) USING BTREE,
                            INDEX `account_id`(`account_id` ASC) USING BTREE,
                            INDEX `thread_id`(`thread_id` ASC) USING BTREE,
                            CONSTRAINT `db_post_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                            CONSTRAINT `db_post_ibfk_2` FOREIGN KEY (`thread_id`) REFERENCES `thread` (`thread_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for image_asset
-- ----------------------------
CREATE TABLE IF NOT EXISTS `image_asset` (
    `asset_id` int NOT NULL AUTO_INCREMENT,
    `account_id` int NULL DEFAULT NULL COMMENT '上传者账号ID，可为空以兼容历史自动收编',
    `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '平台内部图片地址',
    `object_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对象存储路径',
    `original_ext` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `output_ext` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `mime_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `file_size` bigint NOT NULL,
    `width` int NOT NULL,
    `height` int NOT NULL,
    `sha256` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `asset_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源语义类型: STICKER/IMAGE',
    `visibility` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `favorite_count` int NOT NULL DEFAULT 0,
    `use_count` int NOT NULL DEFAULT 0,
    `create_time` datetime NULL DEFAULT NULL,
    `update_time` datetime NULL DEFAULT NULL,
    PRIMARY KEY (`asset_id`) USING BTREE,
    UNIQUE INDEX `uk_image_asset_url` (`url`) USING BTREE,
    INDEX `idx_image_asset_account_status` (`account_id`, `asset_type`, `status`) USING BTREE,
    INDEX `idx_image_asset_asset_type` (`asset_type`) USING BTREE,
    INDEX `idx_image_asset_status` (`status`) USING BTREE,
    CONSTRAINT `fk_image_asset_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for image_asset_favorite
-- ----------------------------
CREATE TABLE IF NOT EXISTS `image_asset_favorite` (
    `favorite_id` int NOT NULL AUTO_INCREMENT,
    `account_id` int NOT NULL,
    `asset_id` int NOT NULL,
    `create_time` datetime NULL DEFAULT NULL,
    PRIMARY KEY (`favorite_id`) USING BTREE,
    UNIQUE INDEX `uk_image_asset_favorite` (`account_id`, `asset_id`) USING BTREE,
    INDEX `idx_image_asset_favorite_asset` (`asset_id`) USING BTREE,
    CONSTRAINT `fk_image_asset_favorite_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT `fk_image_asset_favorite_asset` FOREIGN KEY (`asset_id`) REFERENCES `image_asset` (`asset_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for content_image_ref
-- ----------------------------
CREATE TABLE IF NOT EXISTS `content_image_ref` (
    `ref_id` int NOT NULL AUTO_INCREMENT,
    `asset_id` int NOT NULL,
    `content_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `content_id` int NOT NULL,
    `create_time` datetime NULL DEFAULT NULL,
    PRIMARY KEY (`ref_id`) USING BTREE,
    UNIQUE INDEX `uk_content_image_ref` (`asset_id`, `content_type`, `content_id`) USING BTREE,
    INDEX `idx_content_image_ref_content` (`content_type`, `content_id`) USING BTREE,
    CONSTRAINT `fk_content_image_ref_asset` FOREIGN KEY (`asset_id`) REFERENCES `image_asset` (`asset_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for privacy
-- ----------------------------
CREATE TABLE IF NOT EXISTS `privacy`  (
                               `private_id` int NOT NULL COMMENT '隐私表, 用来管理用户的隐私设置',
                               `account_id` int NOT NULL COMMENT '用户ID',
                               `is_activity_show` tinyint NULL DEFAULT NULL COMMENT '是否显示动态',
                               `is_like_show` tinyint NULL DEFAULT NULL COMMENT '是否显示喜欢的帖',
                               `is_collect_show` tinyint NULL DEFAULT NULL COMMENT '是否显示收藏的帖',
                               `is_private_message_allow` tinyint NULL DEFAULT NULL COMMENT '是否允许私信',
                               PRIMARY KEY (`private_id` DESC) USING BTREE,
                               INDEX `account_id`(`account_id` ASC) USING BTREE,
                               CONSTRAINT `db_private_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of privacy
-- ----------------------------

-- ----------------------------
-- ----------------------------
-- Table structure for report
-- ----------------------------
CREATE TABLE IF NOT EXISTS `report`  (
                             `report_id` int NOT NULL AUTO_INCREMENT COMMENT '举报ID',
                             `reporter_account_id` int NOT NULL COMMENT '举报人账号ID',
                             `reported_account_id` int NOT NULL COMMENT '被举报人账号ID',
                             `target_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报对象类型',
                             `target_id` int NOT NULL COMMENT '举报对象ID',
                             `report_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报类型代码',
                             `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报描述',
                             `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报状态',
                             `handler_account_id` int NULL DEFAULT NULL COMMENT '处理管理员账号ID',
                             `handle_note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理备注',
                             `handled_at` datetime NULL DEFAULT NULL COMMENT '处理时间',
                             `reported_username_snapshot` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '被举报用户名快照',
                             `target_summary_snapshot` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '被举报对象摘要快照',
                             `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                             `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                             PRIMARY KEY (`report_id`) USING BTREE,
                             INDEX `idx_report_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
                             INDEX `idx_report_reporter`(`reporter_account_id` ASC) USING BTREE,
                             INDEX `idx_report_reported`(`reported_account_id` ASC) USING BTREE,
                             INDEX `idx_report_status`(`status` ASC) USING BTREE,
                             CONSTRAINT `db_report_ibfk_1` FOREIGN KEY (`reporter_account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                             CONSTRAINT `db_report_ibfk_2` FOREIGN KEY (`reported_account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for role
-- ----------------------------
CREATE TABLE IF NOT EXISTS `role`  (
                            `role_id` int NOT NULL COMMENT '权能id',
                            `role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权能称呼',
                            `priority` int NULL DEFAULT NULL COMMENT '权限等级, 数字越小权限越高, 最高位 0',
                            PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for tag
-- ----------------------------
CREATE TABLE `tag`  (
                           `tag_id` int NOT NULL AUTO_INCREMENT COMMENT '标签id',
                           `tag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签内容',
                           `create_time` datetime NULL DEFAULT NULL,
                           PRIMARY KEY (`tag_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tag
-- ----------------------------

-- ----------------------------
-- Table structure for theme
-- ----------------------------
CREATE TABLE IF NOT EXISTS `theme`  (
                             `theme_id` int NOT NULL,
                             `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                             PRIMARY KEY (`theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for passkey_credential
-- ----------------------------
CREATE TABLE IF NOT EXISTS `passkey_credential` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `account_id` int NOT NULL,
    `credential_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `user_handle` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `attestation_object` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
    `client_data_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
    `signature_count` bigint NOT NULL DEFAULT 0,
    `transports` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
    `backup_eligible` tinyint NULL DEFAULT 0,
    `backup_state` tinyint NULL DEFAULT 0,
    `uv_initialized` tinyint NULL DEFAULT 0,
    `label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
    `last_used_at` datetime NULL DEFAULT NULL,
    `create_time` datetime NULL DEFAULT NULL,
    `update_time` datetime NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_passkey_credential_id` (`credential_id`) USING BTREE,
    INDEX `idx_passkey_account_id` (`account_id`) USING BTREE,
    CONSTRAINT `fk_passkey_credential_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for thread
-- ----------------------------
CREATE TABLE IF NOT EXISTS `thread`  (
                              `thread_id` int NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
                              `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '帖子标题',
                              `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面连接',
                              `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '内容',
                              `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                              `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
                              `view_count` int NULL DEFAULT NULL COMMENT '观看量',
                              `like_count` int NULL DEFAULT NULL COMMENT '点赞量',
                              `topic_id` int NULL DEFAULT NULL COMMENT '主题ID',
                              `tag_id` int NULL DEFAULT NULL COMMENT '标签ID',
                              `account_id` int NULL DEFAULT NULL COMMENT '帖子作者ID',
                              `is_muted` tinyint NULL DEFAULT NULL COMMENT '帖子是否禁止发言',
                              `is_selected` tinyint NULL DEFAULT NULL COMMENT '帖子是否加精',
                              PRIMARY KEY (`thread_id`) USING BTREE,
                              INDEX `account_id`(`account_id` ASC) USING BTREE,
                              INDEX `tag_id`(`tag_id` ASC) USING BTREE,
                              INDEX `topic_id`(`topic_id` ASC) USING BTREE,
                              CONSTRAINT `db_thread_ibfk_1` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`tag_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                              CONSTRAINT `db_thread_ibfk_2` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                              CONSTRAINT `db_thread_ibfk_3` FOREIGN KEY (`topic_id`) REFERENCES `topic` (`topic_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Table structure for topic
-- ----------------------------
CREATE TABLE IF NOT EXISTS `topic`  (
                             `topic_id` int NOT NULL AUTO_INCREMENT,
                             `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                             `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                             `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                             `create_time` datetime NULL DEFAULT NULL,
                             `theme_id` int NULL DEFAULT NULL,
                             PRIMARY KEY (`topic_id`) USING BTREE,
                             INDEX `theme_id`(`theme_id` ASC) USING BTREE,
                             CONSTRAINT `db_topic_ibfk_1` FOREIGN KEY (`theme_id`) REFERENCES `theme` (`theme_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for topic_stat
-- ----------------------------
CREATE TABLE IF NOT EXISTS `topic_stat`  (
                                  `topic_stat_id` int NOT NULL AUTO_INCREMENT,
                                  `topic_id` int NULL DEFAULT NULL,
                                  `thread_count` int NULL DEFAULT NULL,
                                  `reply_count` int NULL DEFAULT NULL,
                                  PRIMARY KEY (`topic_stat_id`) USING BTREE,
                                  INDEX `topic_id`(`topic_id` ASC) USING BTREE,
                                  CONSTRAINT `db_topic_stat_ibfk_1` FOREIGN KEY (`topic_id`) REFERENCES `topic` (`topic_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of topic_stat
-- ----------------------------

-- ----------------------------
-- V1 privacy and relation tables used by the current Spring Boot runtime
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_relation` (
    `relation_id` bigint NOT NULL AUTO_INCREMENT,
    `from_account_id` int NOT NULL,
    `to_account_id` int NOT NULL,
    `relation_type` varchar(32) NOT NULL,
    `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`relation_id`) USING BTREE,
    UNIQUE KEY `uk_user_relation_pair_type` (`from_account_id`, `to_account_id`, `relation_type`),
    KEY `idx_user_relation_from_type_status` (`from_account_id`, `relation_type`, `status`),
    KEY `idx_user_relation_to_type_status` (`to_account_id`, `relation_type`, `status`),
    CONSTRAINT `chk_user_relation_no_self` CHECK (`from_account_id` <> `to_account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `account_info` (
    `account_id` int NOT NULL,
    `bio` varchar(255) DEFAULT NULL,
    `location` varchar(100) DEFAULT NULL,
    `birthday` date DEFAULT NULL,
    `website` varchar(255) DEFAULT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`account_id`) USING BTREE,
    CONSTRAINT `account_info_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Existing deployments can backfill legacy bio data with:
-- INSERT INTO account_info (account_id, bio, create_time, update_time)
-- SELECT account_id, bio, NOW(), NOW()
-- FROM account
-- WHERE bio IS NOT NULL
-- ON DUPLICATE KEY UPDATE bio = VALUES(bio), update_time = VALUES(update_time);

CREATE TABLE IF NOT EXISTS `user_privacy_setting` (
    `account_id` int NOT NULL,
    `profile_visibility` varchar(32) NOT NULL DEFAULT 'PUBLIC',
    `liked_threads_visibility` varchar(32) NOT NULL DEFAULT 'PUBLIC',
    `collected_threads_visibility` varchar(32) NOT NULL DEFAULT 'PRIVATE',
    `follow_list_visibility` varchar(32) NOT NULL DEFAULT 'PUBLIC',
    `follower_list_visibility` varchar(32) NOT NULL DEFAULT 'PUBLIC',
    `birthday_visibility` varchar(32) NOT NULL DEFAULT 'PRIVATE',
    `dm_permission` varchar(32) NOT NULL DEFAULT 'EVERYONE',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`account_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `mention_message` (
    `mention_message_id` int NOT NULL AUTO_INCREMENT,
    `account_id` int NOT NULL,
    `from_account_id` int NOT NULL,
    `source_type` varchar(16) NOT NULL,
    `source_id` int NOT NULL,
    `thread_id` int NOT NULL,
    `path` varchar(255) NOT NULL,
    `content_summary` varchar(255) NOT NULL DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`mention_message_id`) USING BTREE,
    KEY `idx_mention_message_account_time` (`account_id`, `create_time`),
    KEY `idx_mention_message_from_account` (`from_account_id`),
    KEY `idx_mention_message_source` (`source_type`, `source_id`),
    CONSTRAINT `mention_message_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `mention_message_ibfk_2` FOREIGN KEY (`from_account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;
