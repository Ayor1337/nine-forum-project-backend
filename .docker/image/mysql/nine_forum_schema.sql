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
-- Table structure for account_login_session
-- ----------------------------
CREATE TABLE IF NOT EXISTS `account_login_session` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录会话ID，写入 JWT sid claim',
    `account_id` int NOT NULL COMMENT '账号ID',
    `jwt_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'JWT jti，仅保存 token 元数据，不保存完整 token',
    `ip_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '登录 IP',
    `user_agent` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '登录 User-Agent',
    `os_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '简单解析出的操作系统',
    `browser_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '简单解析出的浏览器',
    `device_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Desktop/Mobile/Tablet/Unknown',
    `login_time` datetime NOT NULL COMMENT '登录时间',
    `expire_time` datetime NOT NULL COMMENT 'JWT 过期时间',
    `revoked_time` datetime NULL DEFAULT NULL COMMENT '主动登出或被踢下线时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_account_login_session_session_id` (`session_id`) USING BTREE,
    INDEX `idx_account_login_session_account_time` (`account_id`, `login_time`) USING BTREE,
    INDEX `idx_account_login_session_jwt_id` (`jwt_id`) USING BTREE,
    CONSTRAINT `fk_account_login_session_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

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
                                  `permission` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限标识',
                                  PRIMARY KEY (`permission_id` DESC) USING BTREE,
                                  UNIQUE INDEX `uk_role_permission`(`role_id` ASC, `permission` ASC) USING BTREE,
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
                            `reply_to` int NULL DEFAULT NULL,
                            PRIMARY KEY (`post_id`) USING BTREE,
                            INDEX `account_id`(`account_id` ASC) USING BTREE,
                            INDEX `thread_id`(`thread_id` ASC) USING BTREE,
                            INDEX `idx_post_reply_to`(`reply_to` ASC) USING BTREE,
                            CONSTRAINT `db_post_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                            CONSTRAINT `db_post_ibfk_2` FOREIGN KEY (`thread_id`) REFERENCES `thread` (`thread_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                            CONSTRAINT `db_post_ibfk_3` FOREIGN KEY (`reply_to`) REFERENCES `post` (`post_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for post_edit_history
-- ----------------------------
CREATE TABLE IF NOT EXISTS `post_edit_history`  (
                              `history_id` int NOT NULL AUTO_INCREMENT COMMENT '编辑历史ID',
                              `post_id` int NOT NULL COMMENT '回复ID',
                              `editor_account_id` int NOT NULL COMMENT '编辑者账号ID',
                              `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '编辑前内容快照(TipTap JSON)',
                              `edit_time` datetime NOT NULL COMMENT '本次编辑发生时间',
                              PRIMARY KEY (`history_id`) USING BTREE,
                              INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
                              INDEX `idx_editor_account_id`(`editor_account_id` ASC) USING BTREE,
                              CONSTRAINT `db_post_edit_history_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                              CONSTRAINT `db_post_edit_history_ibfk_2` FOREIGN KEY (`editor_account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

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
    `favorite_count` int NOT NULL DEFAULT 0 COMMENT '已添加该表情包的用户数',
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
-- Table structure for image_asset_favorite（用户表情包库关系）
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
-- Table structure for feedback
-- ----------------------------
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
                             INDEX `idx_feedback_account_time`(`account_id` ASC, `create_time` DESC) USING BTREE,
                             INDEX `idx_feedback_status_time`(`status` ASC, `create_time` DESC) USING BTREE,
                             INDEX `idx_feedback_type_time`(`type` ASC, `create_time` DESC) USING BTREE,
                             CONSTRAINT `fk_feedback_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                             CONSTRAINT `fk_feedback_handler` FOREIGN KEY (`handler_account_id`) REFERENCES `account` (`account_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for dashboard_activity
-- ----------------------------
CREATE TABLE IF NOT EXISTS `dashboard_activity` (
                              `activity_id` bigint NOT NULL AUTO_INCREMENT COMMENT '仪表盘动态ID',
                              `source_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '来源唯一键',
                              `created_at` datetime NOT NULL COMMENT '动态发生时间',
                              `user_id` bigint NULL DEFAULT NULL COMMENT '操作者用户ID',
                              `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作者用户名',
                              `action` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
                              `target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作对象名称',
                              `target_id` bigint NULL DEFAULT NULL COMMENT '操作对象ID',
                              `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '动态分类',
                              PRIMARY KEY (`activity_id`) USING BTREE,
                              UNIQUE INDEX `uk_dashboard_activity_source_key` (`source_key`) USING BTREE,
                              INDEX `idx_dashboard_activity_created_at` (`created_at`) USING BTREE,
                              INDEX `idx_dashboard_activity_type_action` (`type`, `action`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for permission_operation_log
-- ----------------------------
CREATE TABLE IF NOT EXISTS `permission_operation_log` (
                              `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限操作日志ID',
                              `user_id` int NOT NULL COMMENT '操作者用户ID',
                              `action` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
                              `target_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作对象类型',
                              `target_id` bigint NULL DEFAULT NULL COMMENT '操作对象ID',
                              `method` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行方法',
                              `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数JSON',
                              `duration_ms` bigint NOT NULL COMMENT '执行耗时毫秒',
                              `create_time` datetime NOT NULL COMMENT '创建时间',
                              PRIMARY KEY (`log_id`) USING BTREE,
                              INDEX `idx_permission_operation_log_create_time` (`create_time`) USING BTREE,
                              INDEX `idx_permission_operation_log_user_action` (`user_id`, `action`) USING BTREE,
                              INDEX `idx_permission_operation_log_target` (`target_type`, `target_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for role
-- ----------------------------
CREATE TABLE IF NOT EXISTS `role`  (
                            `role_id` int NOT NULL AUTO_INCREMENT COMMENT '权能id',
                            `role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权能称呼',
                            `role_nick` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色展示名称',
                            `priority` int NULL DEFAULT NULL COMMENT '权限等级, 数字越小权限越高, 最高位 0',
                            `topic_id` int NULL DEFAULT NULL COMMENT '角色绑定的话题ID',
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
-- Table structure for thread_edit_history
-- ----------------------------
CREATE TABLE IF NOT EXISTS `thread_edit_history`  (
                              `history_id` int NOT NULL AUTO_INCREMENT COMMENT '编辑历史ID',
                              `thread_id` int NOT NULL COMMENT '帖子ID',
                              `editor_account_id` int NOT NULL COMMENT '编辑者账号ID',
                              `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编辑前标题快照',
                              `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '编辑前内容快照(TipTap JSON)',
                              `edit_time` datetime NOT NULL COMMENT '本次编辑发生时间',
                              PRIMARY KEY (`history_id`) USING BTREE,
                              INDEX `idx_thread_id`(`thread_id` ASC) USING BTREE,
                              INDEX `idx_editor_account_id`(`editor_account_id` ASC) USING BTREE,
                              CONSTRAINT `db_thread_edit_history_ibfk_1` FOREIGN KEY (`thread_id`) REFERENCES `thread` (`thread_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                              CONSTRAINT `db_thread_edit_history_ibfk_2` FOREIGN KEY (`editor_account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;


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

CREATE TABLE IF NOT EXISTS `follow_message` (
    `follow_message_id` int NOT NULL AUTO_INCREMENT,
    `account_id` int NOT NULL,
    `from_account_id` int NOT NULL,
    `thread_id` int NOT NULL,
    `path` varchar(255) NOT NULL,
    `title` varchar(255) NOT NULL,
    `content_summary` varchar(255) NOT NULL DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`follow_message_id`) USING BTREE,
    KEY `idx_follow_message_account_time` (`account_id`, `create_time`),
    KEY `idx_follow_message_from_account` (`from_account_id`),
    KEY `idx_follow_message_thread` (`thread_id`),
    CONSTRAINT `follow_message_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `follow_message_ibfk_2` FOREIGN KEY (`from_account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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

-- ----------------------------
-- Table structure for credit_account
-- ----------------------------
CREATE TABLE IF NOT EXISTS `credit_account` (
    `account_id` int NOT NULL COMMENT '账号ID',
    `balance` bigint NOT NULL DEFAULT 0 COMMENT 'Credit 余额（整数，非负）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`account_id`) USING BTREE,
    CONSTRAINT `credit_account_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `chk_credit_account_balance` CHECK (`balance` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Credit 货币余额表';

-- ----------------------------
-- Table structure for credit_transaction
-- ----------------------------
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

-- ----------------------------
-- Table structure for decoration
-- ----------------------------
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

-- ----------------------------
-- Table structure for shop_item
-- ----------------------------
CREATE TABLE IF NOT EXISTS `shop_item` (
    `item_id` int NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称（title 类型即头衔文本）',
    `item_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品关键字（唯一，前端素材映射用）',
    `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品描述',
    `item_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型：badge/avatar_frame/title',
    `decoration_id` int NULL DEFAULT NULL COMMENT '绑定的装扮ID（可空，空则前端回退 item_key 硬编码渲染）',
    `price` bigint NOT NULL COMMENT '售价（Credit，整数）',
    `stock` bigint NOT NULL DEFAULT -1 COMMENT '库存，-1=不限量',
    `purchase_limit` int NOT NULL DEFAULT 0 COMMENT '每人限购数量，0=不限购（预留堆叠道具，装饰类天然限购1件）',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1=上架，2=下架',
    `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '软删除',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`item_id`) USING BTREE,
    UNIQUE KEY `uk_shop_item_item_key` (`item_key`),
    KEY `idx_shop_item_status_type` (`status`, `item_type`),
    KEY `idx_shop_item_decoration` (`decoration_id`),
    CONSTRAINT `chk_shop_item_price` CHECK (`price` >= 0),
    CONSTRAINT `chk_shop_item_stock` CHECK (`stock` >= -1),
    CONSTRAINT `shop_item_ibfk_decoration` FOREIGN KEY (`decoration_id`) REFERENCES `decoration` (`decoration_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商城商品表';

-- ----------------------------
-- Table structure for user_item
-- ----------------------------
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

-- ----------------------------
-- Table structure for shop_order
-- ----------------------------
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

SET FOREIGN_KEY_CHECKS = 1;
