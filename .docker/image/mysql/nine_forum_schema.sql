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
-- Table structure for db_account
-- ----------------------------
CREATE TABLE IF NOT EXISTS `db_account`  (
                               `account_id` int NOT NULL AUTO_INCREMENT COMMENT '账号ID',
                               `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名，用来登录',
                               `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
                               `nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户昵称',
                               `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
                               `bio` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '座右铭',
                               `status` tinyint NOT NULL COMMENT '账号状态',
                               `create_time` datetime NULL DEFAULT NULL COMMENT '账号创建时间',
                               `update_time` datetime NULL DEFAULT NULL COMMENT '账号更新时间',
                               `role_id` int NULL DEFAULT NULL COMMENT '权限等级',
                               PRIMARY KEY (`account_id`) USING BTREE,
                               INDEX `role_id`(`role_id` ASC) USING BTREE,
                               CONSTRAINT `db_account_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `db_role` (`role_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for db_account_stat
-- ----------------------------
CREATE TABLE IF NOT EXISTS `db_account_stat`  (
                                    `user_stat_id` int NOT NULL,
                                    `post_count` int NULL DEFAULT NULL,
                                    `reply_count` int NULL DEFAULT NULL,
                                    `liked_count` int NULL DEFAULT NULL,
                                    `collected_count` int NULL DEFAULT NULL,
                                    `account_id` int NULL DEFAULT NULL,
                                    PRIMARY KEY (`user_stat_id`) USING BTREE,
                                    INDEX `account_id`(`account_id` ASC) USING BTREE,
                                    CONSTRAINT `db_account_stat_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `db_account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of db_account_stat
-- ----------------------------

-- ----------------------------
-- Table structure for db_collect
-- ----------------------------
CREATE TABLE IF NOT EXISTS `db_collect`  (
                               `collect_id` int NOT NULL COMMENT '收藏ID',
                               `account_id` int NULL DEFAULT NULL COMMENT '用户ID',
                               `thread_id` int NULL DEFAULT NULL COMMENT '收藏的帖子',
                               PRIMARY KEY (`collect_id`) USING BTREE,
                               INDEX `account_id`(`account_id` ASC) USING BTREE,
                               CONSTRAINT `db_collect_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `db_account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of db_collect
-- ----------------------------

-- ----------------------------
-- Table structure for db_history
-- ----------------------------
CREATE TABLE IF NOT EXISTS `db_history`  (
                               `history_id` int NOT NULL,
                               `thread_id` int NULL DEFAULT NULL,
                               `account_id` int NULL DEFAULT NULL,
                               `visit_time` datetime NULL DEFAULT NULL,
                               PRIMARY KEY (`history_id`) USING BTREE,
                               INDEX `thread_id`(`thread_id` ASC) USING BTREE,
                               INDEX `account_id`(`account_id` ASC) USING BTREE,
                               CONSTRAINT `db_history_ibfk_1` FOREIGN KEY (`thread_id`) REFERENCES `db_thread` (`thread_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                               CONSTRAINT `db_history_ibfk_2` FOREIGN KEY (`account_id`) REFERENCES `db_account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of db_history
-- ----------------------------

-- ----------------------------
-- Table structure for db_like
-- ----------------------------
CREATE TABLE IF NOT EXISTS `db_like`  (
                            `like_id` int NOT NULL COMMENT '喜欢ID',
                            `account_id` int NULL DEFAULT NULL COMMENT '谁喜欢',
                            `thread_id` int NULL DEFAULT NULL COMMENT '喜欢的帖子',
                            PRIMARY KEY (`like_id`) USING BTREE,
                            INDEX `account_id`(`account_id` ASC) USING BTREE,
                            CONSTRAINT `db_like_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `db_account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of db_like
-- ----------------------------

-- ----------------------------
-- Table structure for db_permission
-- ----------------------------
CREATE TABLE IF NOT EXISTS `db_permission`  (
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
                                  CONSTRAINT `db_permission_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `db_role` (`role_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Table structure for db_post
-- ----------------------------
CREATE TABLE IF NOT EXISTS `db_post`  (
                            `post_id` int NOT NULL AUTO_INCREMENT,
                            `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
                            `account_id` int NULL DEFAULT NULL,
                            `create_time` datetime NULL DEFAULT NULL,
                            `update_time` datetime NULL DEFAULT NULL,
                            `thread_id` int NULL DEFAULT NULL,
                            PRIMARY KEY (`post_id`) USING BTREE,
                            INDEX `account_id`(`account_id` ASC) USING BTREE,
                            INDEX `thread_id`(`thread_id` ASC) USING BTREE,
                            CONSTRAINT `db_post_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `db_account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                            CONSTRAINT `db_post_ibfk_2` FOREIGN KEY (`thread_id`) REFERENCES `db_thread` (`thread_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for db_private
-- ----------------------------
CREATE TABLE IF NOT EXISTS `db_private`  (
                               `private_id` int NOT NULL COMMENT '隐私表, 用来管理用户的隐私设置',
                               `account_id` int NOT NULL COMMENT '用户ID',
                               `is_activity_show` tinyint NULL DEFAULT NULL COMMENT '是否显示动态',
                               `is_like_show` tinyint NULL DEFAULT NULL COMMENT '是否显示喜欢的帖',
                               `is_collect_show` tinyint NULL DEFAULT NULL COMMENT '是否显示收藏的帖',
                               `is_private_message_allow` tinyint NULL DEFAULT NULL COMMENT '是否允许私信',
                               PRIMARY KEY (`private_id` DESC) USING BTREE,
                               INDEX `account_id`(`account_id` ASC) USING BTREE,
                               CONSTRAINT `db_private_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `db_account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of db_private
-- ----------------------------

-- ----------------------------
-- Table structure for db_reply
-- ----------------------------
CREATE TABLE IF NOT EXISTS `db_reply`  (
                             `reply_id` int NOT NULL AUTO_INCREMENT COMMENT '回复消息ID',
                             `account_id` int NULL DEFAULT NULL COMMENT '用户ID 谁回复的',
                             `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '回复内容',
                             `thread_id` int NULL DEFAULT NULL COMMENT '回复的楼层',
                             `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                             PRIMARY KEY (`reply_id`) USING BTREE,
                             INDEX `post_id`(`thread_id` ASC) USING BTREE,
                             INDEX `user_id`(`account_id` ASC) USING BTREE,
                             CONSTRAINT `db_reply_ibfk_1` FOREIGN KEY (`thread_id`) REFERENCES `db_thread` (`thread_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                             CONSTRAINT `db_reply_ibfk_2` FOREIGN KEY (`account_id`) REFERENCES `db_account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of db_reply
-- ----------------------------

-- ----------------------------
-- Table structure for db_role
-- ----------------------------
CREATE TABLE IF NOT EXISTS `db_role`  (
                            `role_id` int NOT NULL COMMENT '权能id',
                            `role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权能称呼',
                            `priority` int NULL DEFAULT NULL COMMENT '权限等级, 数字越小权限越高, 最高位 0',
                            PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for db_tag
-- ----------------------------
CREATE TABLE `db_tag`  (
                           `tag_id` int NOT NULL AUTO_INCREMENT COMMENT '标签id',
                           `tag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签内容',
                           `create_time` datetime NULL DEFAULT NULL,
                           PRIMARY KEY (`tag_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of db_tag
-- ----------------------------

-- ----------------------------
-- Table structure for db_theme
-- ----------------------------
CREATE TABLE IF NOT EXISTS `db_theme`  (
                             `theme_id` int NOT NULL,
                             `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                             PRIMARY KEY (`theme_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for db_thread
-- ----------------------------
CREATE TABLE IF NOT EXISTS `db_thread`  (
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
                              CONSTRAINT `db_thread_ibfk_1` FOREIGN KEY (`tag_id`) REFERENCES `db_tag` (`tag_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                              CONSTRAINT `db_thread_ibfk_2` FOREIGN KEY (`account_id`) REFERENCES `db_account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                              CONSTRAINT `db_thread_ibfk_3` FOREIGN KEY (`topic_id`) REFERENCES `db_topic` (`topic_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Table structure for db_topic
-- ----------------------------
CREATE TABLE IF NOT EXISTS `db_topic`  (
                             `topic_id` int NOT NULL AUTO_INCREMENT,
                             `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                             `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                             `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                             `create_time` datetime NULL DEFAULT NULL,
                             `theme_id` int NULL DEFAULT NULL,
                             PRIMARY KEY (`topic_id`) USING BTREE,
                             INDEX `theme_id`(`theme_id` ASC) USING BTREE,
                             CONSTRAINT `db_topic_ibfk_1` FOREIGN KEY (`theme_id`) REFERENCES `db_theme` (`theme_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for db_topic_stat
-- ----------------------------
CREATE TABLE IF NOT EXISTS `db_topic_stat`  (
                                  `topic_stat_id` int NOT NULL AUTO_INCREMENT,
                                  `topic_id` int NULL DEFAULT NULL,
                                  `thread_count` int NULL DEFAULT NULL,
                                  `reply_count` int NULL DEFAULT NULL,
                                  PRIMARY KEY (`topic_stat_id`) USING BTREE,
                                  INDEX `topic_id`(`topic_id` ASC) USING BTREE,
                                  CONSTRAINT `db_topic_stat_ibfk_1` FOREIGN KEY (`topic_id`) REFERENCES `db_topic` (`topic_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of db_topic_stat
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
