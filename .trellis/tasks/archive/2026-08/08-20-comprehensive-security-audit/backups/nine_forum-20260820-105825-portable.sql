-- MySQL dump 10.13  Distrib 9.5.0, for Linux (x86_64)
--
-- Host: localhost    Database: nine_forum
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `nine_forum`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `nine_forum` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `nine_forum`;

--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account` (
  `account_id` int NOT NULL AUTO_INCREMENT COMMENT '账号ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名，用来登录',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户昵称',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '头像URL',
  `banner_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '账号状态',
  `create_time` datetime DEFAULT NULL COMMENT '账号创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '账号更新时间',
  `role_id` int DEFAULT NULL COMMENT '权限等级',
  `is_deleted` tinyint DEFAULT '0' COMMENT '用户是否注销账号/或被删除',
  `email` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`account_id`) USING BTREE,
  UNIQUE KEY `role_username` (`username`) USING BTREE,
  KEY `role_id` (`role_id`) USING BTREE,
  CONSTRAINT `account_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,'admin','$2a$10$/m4hMr/yL5BuHF6axCRUq.e90VT26j.ANItT4J.45S9cFRbgMyCXC','鹿目まどか','nineforum/avatar/756f7ea2df0a469e9da929c631cb9992.jpg','nineforum/banner/49810c06f2e74580b994cb131f6f7868.jpg',1,NULL,'2025-11-26 15:04:37',1,0,NULL),(2,'ayor','$2a$10$/m4hMr/yL5BuHF6axCRUq.e90VT26j.ANItT4J.45S9cFRbgMyCXC','绀野木棉季','nineforum/avatar/285c5855b4bb4b718b2837b473fbdb72.png','nineforum/banner/1c11346eb44843cca0e8a7408ec49ac5.png',1,NULL,'2026-05-06 12:24:47',4,0,NULL),(3,'lucifer','$2a$10$/m4hMr/yL5BuHF6axCRUq.e90VT26j.ANItT4J.45S9cFRbgMyCXC','改名3d0','nineforum/avatar/default.jpg','nineforum/banner/default.webp',1,NULL,NULL,3,0,NULL),(8,'ayor7557','$2a$10$EcNALMusSz773ikhvIjXROkq3Y9iZ6QO/ZKbWLS.jBWzxPZ1fLu1O','改名4da','nineforum/avatar/default.jpg','nineforum/banner/default.webp',1,'2025-11-13 10:57:17',NULL,3,0,'ayor1337@qq.com'),(9,'hxl6','$2a$10$mFwnXqxqV9ZgvCUzhXq/Ne7vr2MK0R7Aq4n5hPae6sEELCoB4cYwe','安师大','nineforum/avatar/default.jpg','nineforum/banner/default.webp',1,'2026-04-29 13:53:16',NULL,3,0,'ayor1337@gmail.com');
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `account_info`
--

DROP TABLE IF EXISTS `account_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account_info` (
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_info`
--

LOCK TABLES `account_info` WRITE;
/*!40000 ALTER TABLE `account_info` DISABLE KEYS */;
INSERT INTO `account_info` VALUES (1,'我喜欢你   ','武汉','2006-03-26',NULL,'2026-04-30 09:09:21','2026-04-30 09:31:39'),(2,NULL,NULL,NULL,NULL,'2026-04-30 10:05:24','2026-05-15 14:47:03'),(3,NULL,NULL,NULL,NULL,'2026-05-04 09:22:31','2026-05-04 09:22:31'),(8,NULL,NULL,NULL,NULL,'2026-05-04 09:22:31','2026-05-04 09:22:31'),(9,NULL,NULL,NULL,NULL,'2026-05-04 09:22:31','2026-05-04 09:22:31');
/*!40000 ALTER TABLE `account_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `account_login_session`
--

DROP TABLE IF EXISTS `account_login_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account_login_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录会话ID，写入 JWT sid claim',
  `account_id` int NOT NULL COMMENT '账号ID',
  `jwt_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'JWT jti，仅保存 token 元数据，不保存完整 token',
  `ip_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '登录 IP',
  `user_agent` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '登录 User-Agent',
  `os_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '简单解析出的操作系统',
  `browser_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '简单解析出的浏览器',
  `device_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Desktop/Mobile/Tablet/Unknown',
  `login_time` datetime NOT NULL COMMENT '登录时间',
  `expire_time` datetime NOT NULL COMMENT 'JWT 过期时间',
  `revoked_time` datetime DEFAULT NULL COMMENT '主动登出或被踢下线时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_account_login_session_session_id` (`session_id`) USING BTREE,
  KEY `idx_account_login_session_account_time` (`account_id`,`login_time`) USING BTREE,
  KEY `idx_account_login_session_jwt_id` (`jwt_id`) USING BTREE,
  CONSTRAINT `fk_account_login_session_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_login_session`
--

LOCK TABLES `account_login_session` WRITE;
/*!40000 ALTER TABLE `account_login_session` DISABLE KEYS */;
INSERT INTO `account_login_session` VALUES (1,'3929d547-5267-4afa-9ca2-b5506121b3d7',1,'605afefb-5445-4b80-bcd2-df1749489afb','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-05-29 14:30:55','2026-06-05 14:30:55','2026-05-29 19:50:51'),(2,'f8d9a7a3-d27d-4f0c-b023-2f65b51ae577',1,'3594d4df-3ff4-45d0-b2e1-876a7d155fcb','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-05-29 14:31:14','2026-06-05 14:31:14','2026-05-29 14:31:18'),(3,'65174415-8f2d-4e26-811a-0b7d31f48b27',2,'3f7fc89e-37d0-4534-b131-4b9ec17cf987','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-05-29 19:17:46','2026-06-05 19:17:46','2026-05-29 19:17:48'),(4,'e5c23165-de50-46c4-98cf-218eabd1e018',1,'217a44b9-8fc5-4edc-9f42-73bf40b2f7e6','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-05-29 19:17:54','2026-06-05 19:17:54','2026-05-29 19:18:19'),(5,'36ebc3cb-1ada-43cc-acd0-54e1f85bbb44',1,'8ed1e189-45f8-475b-a0bb-9c59b563a20a','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-05-29 19:21:31','2026-06-05 19:21:31','2026-05-29 19:50:57'),(6,'33f77fe0-1b7d-4bbd-8dac-6e2bf3844474',1,'f49ff15d-db2f-4774-b7ee-ca34e4ea81c1','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-05-29 19:38:43','2026-06-05 19:38:43','2026-05-31 20:03:21'),(7,'849fb6a1-4900-4795-9b4d-822a74b276b7',1,'44aca153-aaa1-41f3-963b-3a2018f6ff37','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-05-29 19:57:24','2026-06-05 19:57:24','2026-05-29 19:57:59'),(8,'8d8e1000-86a6-425c-8786-1a637596dae1',1,'ccc7cc96-d143-4d6a-b984-01d24b18f458','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-05-29 19:57:52','2026-06-05 19:57:52','2026-05-29 19:57:56'),(9,'a65e4bb2-832e-476c-8e94-ec209d16c61c',2,'9d2bb579-91e9-4b09-919f-032290f88d75','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-05-30 13:46:06','2026-06-06 13:46:06',NULL),(10,'61b928cf-a47d-40fd-b6c1-e79ab5ebcfcd',2,'563eef51-c3f6-473e-938a-160d62923194','0:0:0:0:0:0:0:1','Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36','Linux','Chrome','Desktop','2026-05-30 14:48:28','2026-06-06 14:48:28',NULL),(11,'d234bc13-bc86-44b3-90cf-79690993c6ac',1,'e61b5eb7-a9d7-4c7a-a6ba-e5b93a0ded00','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-05-31 20:03:30','2026-06-07 20:03:30',NULL),(12,'10eb53aa-8670-4181-b598-d474ccc5d6c9',2,'8935c98d-ebb2-4a4f-b02d-8d9edc611409','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-05-31 20:03:56','2026-06-07 20:03:56',NULL),(13,'af5b7c82-4c78-4114-b609-dabf450d3dc4',2,'04aaed61-e281-4a31-983b-5f702cb18c2d','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-05-31 20:29:48','2026-06-07 20:29:48',NULL),(14,'245330fd-d918-44c1-acee-3c59a2c53a97',1,'b2169f5c-935f-4d22-a3cd-354df1c7f111','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-06-13 16:47:30','2026-06-20 16:47:30','2026-06-16 14:54:24'),(15,'284f0bcd-93b2-4cc1-ba15-5674550dcffa',1,'87673fde-1880-4c8a-83dc-b8056f7db72f','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Cursor/3.7.27 Chrome/142.0.7444.265 Electron/39.8.1 Safari/537.36','Windows','Chrome','Desktop','2026-06-13 17:49:08','2026-06-20 17:49:08','2026-06-15 17:27:01'),(16,'c14bbb3b-316a-48b7-b15b-6cc8a49fccc6',1,'a65c661d-4e77-49c2-9bfb-91e49a076620','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Cursor/3.7.36 Chrome/142.0.7444.265 Electron/39.8.1 Safari/537.36','Windows','Chrome','Desktop','2026-06-15 17:27:04','2026-06-22 17:27:04',NULL),(17,'79952ea7-fd89-4ae6-a4eb-b414e98a4f75',2,'c95e6434-3b0e-4ddc-82e0-ac21a597ae3f','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-06-16 14:54:31','2026-06-23 14:54:31',NULL),(18,'2ea92de5-ddc9-457f-8c1b-51ac7b3a1ce1',2,'bc81e51d-2235-4ddb-a694-17b5a5141902','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-06-16 16:40:44','2026-06-23 16:40:44',NULL),(19,'90ebfd65-b929-4daf-8d11-5a64227ae7cb',1,'69a74c88-1570-49e6-a565-e156133dbf4b','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-06-16 16:47:54','2026-06-23 16:47:54',NULL),(20,'c07a0035-71ef-40f4-960e-4657e0a3bc26',2,'e7428a82-dc47-4a6b-9b05-d835c89f64fe','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Cursor/3.8.11 Chrome/144.0.7559.236 Electron/40.10.3 Safari/537.36','Windows','Chrome','Desktop','2026-06-22 20:53:32','2026-06-29 20:53:32',NULL),(21,'d373cdc8-cfd2-412f-9346-7f4e1d882c28',2,'82009b5a-5755-4347-8ad0-86d7ef6dd242','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Cursor/3.8.23 Chrome/144.0.7559.236 Electron/40.10.3 Safari/537.36','Windows','Chrome','Desktop','2026-06-24 16:46:18','2026-07-01 16:46:18',NULL),(22,'43a5180d-2062-4f34-941c-9d2015884577',2,'92569f97-1cdc-4593-b3e6-5ee5ea7809c0','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Cursor/3.8.23 Chrome/144.0.7559.236 Electron/40.10.3 Safari/537.36','Windows','Chrome','Desktop','2026-06-24 16:46:30','2026-07-01 16:46:30',NULL),(23,'b6077697-5260-40de-affa-d7e6e1323132',2,'db86c880-30bf-45d1-9155-6a31543e9ed6','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Cursor/3.8.23 Chrome/144.0.7559.236 Electron/40.10.3 Safari/537.36','Windows','Chrome','Desktop','2026-06-24 16:50:42','2026-07-01 16:50:42',NULL),(24,'06a9ea59-c2ae-4588-8aa3-3fc025f600f8',1,'015442ec-ae1c-4d80-926b-7fe89fc350d9','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-06-24 17:09:07','2026-07-01 17:09:07','2026-06-24 21:11:53'),(25,'76b3d308-3b33-42d4-9120-865a6940f9bb',1,'0b25ef7a-f14a-4043-ba37-3e69c88d5876','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-06-26 10:27:41','2026-07-03 10:27:41',NULL),(26,'e00ce70b-1c07-484c-8bd6-1c75a85349d0',2,'041fcc12-161a-4223-b14f-a8694b101af4','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-07-17 10:37:35','2026-07-24 10:37:35','2026-07-18 14:16:44'),(27,'edd924a3-cba4-4482-afc4-a8a586985a0d',2,'176c5331-d6cc-47eb-8b8f-3b3969a02025','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-07-18 14:16:50','2026-07-25 14:16:50',NULL),(28,'be5e2fd5-4958-44e3-bbd5-7f9c6ffdbaa4',1,'5b06bbe6-abb5-4d24-b0d3-412254f0907b','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-07-25 14:58:54','2026-08-01 14:58:54',NULL),(29,'f6a3ccca-171c-444a-8319-541c19026336',2,'fae194cb-9e0d-469d-99d0-4a810d52a985','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-07-26 16:37:56','2026-08-02 16:37:56',NULL),(30,'409c1c31-5236-4b48-a471-54076acea92c',1,'6dd8ce9b-2630-4569-a25d-ae7cbcf12f02','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-08-17 22:02:19','2026-08-24 22:02:19',NULL),(31,'36e5c8bb-6957-4607-a7d5-0eb5c2c24b92',1,'b2e38442-98a8-4c86-bf59-d74ee68e4977','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-08-17 22:17:37','2026-08-24 22:17:37',NULL),(32,'c10d1ba5-ca8e-44db-a940-9dba74d6808b',1,'4b3b1505-fb26-4bc7-971c-a63dc3ca2da8','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36','Windows','Chrome','Desktop','2026-08-18 17:33:25','2026-08-25 17:33:25',NULL);
/*!40000 ALTER TABLE `account_login_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `account_stat`
--

DROP TABLE IF EXISTS `account_stat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account_stat` (
  `user_stat_id` int NOT NULL AUTO_INCREMENT COMMENT '账号统计ID',
  `thread_count` int DEFAULT '0' COMMENT '统计发的帖子的数量',
  `post_count` int DEFAULT '0' COMMENT '统计回复帖子的数量',
  `reply_count` int DEFAULT '0' COMMENT '统计回复楼层的数量',
  `liked_count` int DEFAULT '0' COMMENT '统计被点赞的帖子的数量\r\n',
  `collected_count` int DEFAULT '0' COMMENT '统计被收藏的数量',
  `account_id` int DEFAULT NULL COMMENT '账号ID',
  `following_count` int DEFAULT NULL,
  `follower_count` int DEFAULT NULL,
  PRIMARY KEY (`user_stat_id`) USING BTREE,
  KEY `account_id` (`account_id`) USING BTREE,
  CONSTRAINT `account_stat_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_stat`
--

LOCK TABLES `account_stat` WRITE;
/*!40000 ALTER TABLE `account_stat` DISABLE KEYS */;
INSERT INTO `account_stat` VALUES (1,9,32,0,0,0,1,1,1),(2,3,20,0,0,0,2,1,1),(7,0,0,0,0,0,8,0,0),(8,0,0,0,0,0,9,0,0),(9,0,0,0,0,0,3,0,0);
/*!40000 ALTER TABLE `account_stat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `announcements`
--

DROP TABLE IF EXISTS `announcements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `announcements` (
  `announcement_id` int NOT NULL AUTO_INCREMENT,
  `thread_id` int NOT NULL,
  `is_global` tinyint(1) NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`announcement_id`),
  UNIQUE KEY `uk_announcements_thread_scope` (`thread_id`,`is_global`),
  KEY `idx_announcements_scope_time` (`is_global`,`create_time`),
  CONSTRAINT `fk_announcements_thread` FOREIGN KEY (`thread_id`) REFERENCES `thread` (`thread_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `announcements`
--

LOCK TABLES `announcements` WRITE;
/*!40000 ALTER TABLE `announcements` DISABLE KEYS */;
INSERT INTO `announcements` VALUES (1,85,0,'2026-05-06 13:51:19'),(2,86,1,'2026-06-15 20:07:06');
/*!40000 ALTER TABLE `announcements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chatboard_history`
--

DROP TABLE IF EXISTS `chatboard_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chatboard_history` (
  `chatboard_history_id` int NOT NULL AUTO_INCREMENT,
  `account_id` int DEFAULT NULL,
  `topic_id` int DEFAULT NULL,
  `content` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`chatboard_history_id`)
) ENGINE=InnoDB AUTO_INCREMENT=77 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chatboard_history`
--

LOCK TABLES `chatboard_history` WRITE;
/*!40000 ALTER TABLE `chatboard_history` DISABLE KEYS */;
INSERT INTO `chatboard_history` VALUES (37,1,1,'没有圆周的钟 失去旋转意义','2025-11-05 21:54:14'),(38,1,1,'下雨这天 好安静','2025-11-05 21:54:18'),(39,1,1,'远行没有目的 距离不是问题','2025-11-05 21:54:21'),(40,2,1,'不爱了 是你的谜底','2025-11-05 21:54:34'),(41,2,1,'我占据 格林威治 守候着你','2025-11-05 21:54:37'),(42,2,1,'在时间 标准起点 回忆过去','2025-11-05 21:54:41'),(43,2,1,'你却在 永夜了的 极地旅行','2025-11-05 21:54:44'),(44,2,1,'等爱在 失温后 渐渐死去','2025-11-05 21:54:48'),(45,2,1,'喔 对不起 这句话 打乱了时区','2025-11-05 21:54:57'),(46,2,1,'喔 你要我 在最爱的时候 睡去','2025-11-05 21:55:01'),(47,1,1,'我越想越清醒','2025-11-05 21:55:09'),(48,1,1,'喔 爱你没差','2025-11-05 21:55:24'),(49,1,1,'那一点时差 喔~','2025-11-05 21:55:28'),(50,1,1,'你离开这一拳给的 太重','2025-11-05 21:55:33'),(51,1,1,'我的心找不到 换日线 它在哪','2025-11-05 21:55:37'),(52,1,1,'我只能不停的飞','2025-11-05 21:55:43'),(53,1,1,'直到我将你挽回','2025-11-05 21:55:46'),(54,1,1,'a','2025-11-26 16:39:37'),(55,1,1,'213','2025-12-08 17:40:17'),(56,1,1,'aaa','2026-01-28 19:45:31'),(57,1,1,'bbb','2026-01-28 19:45:32'),(58,1,1,'bbb','2026-01-28 19:45:32'),(59,1,1,'bb','2026-01-28 19:45:33'),(60,1,1,'b','2026-01-28 19:45:33'),(61,1,1,'b','2026-01-28 19:45:34'),(62,1,1,'b','2026-01-28 19:45:34'),(63,1,1,'b','2026-01-28 19:45:34'),(64,1,1,'b','2026-01-28 19:45:34'),(65,1,1,'b','2026-01-28 19:45:34'),(66,1,1,'b','2026-01-28 19:45:34'),(67,1,1,'b','2026-01-28 19:45:35'),(68,1,1,'b','2026-01-28 19:45:35'),(69,1,1,'adsadsad','2026-01-28 19:45:36'),(70,1,1,'sad','2026-01-28 19:45:36'),(71,1,1,'sadas','2026-01-28 19:45:36'),(72,1,1,'das','2026-01-28 19:45:36'),(73,1,1,'a','2026-01-28 19:45:36'),(74,1,1,'wew','2026-01-28 19:45:37'),(75,1,1,'eq','2026-01-28 19:45:37'),(76,9,1,'123','2026-04-29 13:54:12');
/*!40000 ALTER TABLE `chatboard_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `collect`
--

DROP TABLE IF EXISTS `collect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `collect` (
  `collect_id` int NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `account_id` int DEFAULT NULL COMMENT '用户ID',
  `thread_id` int DEFAULT NULL COMMENT '收藏的帖子',
  PRIMARY KEY (`collect_id`) USING BTREE,
  KEY `account_id` (`account_id`) USING BTREE,
  CONSTRAINT `collect_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `collect`
--

LOCK TABLES `collect` WRITE;
/*!40000 ALTER TABLE `collect` DISABLE KEYS */;
INSERT INTO `collect` VALUES (4,1,75),(5,2,83);
/*!40000 ALTER TABLE `collect` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `content_image_ref`
--

DROP TABLE IF EXISTS `content_image_ref`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `content_image_ref` (
  `ref_id` int NOT NULL AUTO_INCREMENT,
  `asset_id` int NOT NULL,
  `content_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `content_id` int NOT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`ref_id`) USING BTREE,
  UNIQUE KEY `uk_content_image_ref` (`asset_id`,`content_type`,`content_id`) USING BTREE,
  KEY `idx_content_image_ref_content` (`content_type`,`content_id`) USING BTREE,
  CONSTRAINT `fk_content_image_ref_asset` FOREIGN KEY (`asset_id`) REFERENCES `image_asset` (`asset_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `content_image_ref`
--

LOCK TABLES `content_image_ref` WRITE;
/*!40000 ALTER TABLE `content_image_ref` DISABLE KEYS */;
INSERT INTO `content_image_ref` VALUES (1,2,'POST',97,'2026-05-08 14:26:48'),(2,3,'POST',98,'2026-05-08 14:51:28'),(3,4,'POST',99,'2026-05-08 14:52:38'),(4,2,'POST',99,'2026-05-08 14:52:38');
/*!40000 ALTER TABLE `content_image_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conversation`
--

DROP TABLE IF EXISTS `conversation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conversation` (
  `conversation_id` int NOT NULL AUTO_INCREMENT,
  `alpha_account_id` int NOT NULL,
  `beta_account_id` int NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` tinyint DEFAULT '0',
  `hidden` tinyint NOT NULL DEFAULT '0' COMMENT '0表示都可见, 1表示对A隐藏, 2表示对B隐藏 3表示都隐藏',
  PRIMARY KEY (`conversation_id`),
  KEY `idx_conversation_alpha_update` (`alpha_account_id`,`update_time`),
  KEY `idx_conversation_beta_update` (`beta_account_id`,`update_time`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conversation`
--

LOCK TABLES `conversation` WRITE;
/*!40000 ALTER TABLE `conversation` DISABLE KEYS */;
INSERT INTO `conversation` VALUES (1,1,2,'2025-10-27 15:40:31','2026-07-19 22:37:44',0,0),(2,3,1,'2025-10-27 16:53:40','2025-10-27 16:53:40',0,0),(3,3,2,'2025-10-27 16:53:53','2025-10-27 16:53:53',0,2);
/*!40000 ALTER TABLE `conversation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conversation_message`
--

DROP TABLE IF EXISTS `conversation_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conversation_message` (
  `conversation_message_id` int NOT NULL AUTO_INCREMENT,
  `conversation_id` int DEFAULT NULL,
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `account_id` int DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` tinyint DEFAULT '0',
  `is_edit` tinyint DEFAULT '0',
  PRIMARY KEY (`conversation_message_id`),
  KEY `idx_conversation_message_conversation_time` (`conversation_id`,`create_time`,`conversation_message_id`)
) ENGINE=InnoDB AUTO_INCREMENT=311 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conversation_message`
--

LOCK TABLES `conversation_message` WRITE;
/*!40000 ALTER TABLE `conversation_message` DISABLE KEYS */;
INSERT INTO `conversation_message` VALUES (1,1,'想念变成空气在叹息',2,'2025-10-29 08:53:51','2025-10-29 08:53:51',0,0),(2,1,'拥抱心中最真的感应 调整呼吸',1,'2025-10-29 08:53:58','2025-10-29 08:53:58',0,0),(3,1,'写着不管多少时间我都会等你',2,'2025-10-29 08:54:00','2025-10-29 08:54:00',0,0),(4,1,'不管怎样 虽然人们总说爱情会很受伤',2,'2025-10-29 08:54:05','2025-10-29 08:54:05',0,0),(5,1,'但我想 我会让自己更坚强吧',1,'2025-10-29 08:54:07','2025-10-29 08:54:07',0,0),(6,1,'每天起床都让心情不会慌张',2,'2025-10-29 08:54:09','2025-10-29 08:54:09',0,0),(7,1,'想让流星能够实现 幸福的愿望',1,'2025-10-29 08:54:13','2025-10-29 08:54:13',0,0),(8,1,'想念变成空气在叹息',2,'2025-10-29 08:54:16','2025-10-29 08:54:16',0,0),(9,1,'多么想要躺在你怀里',2,'2025-10-29 08:54:22','2025-10-29 08:54:22',0,0),(10,1,'那是爱情带来的讯息',1,'2025-10-29 08:54:25','2025-10-29 08:54:25',0,0),(11,1,'让自己傻的很确定',1,'2025-10-29 08:54:29','2025-10-29 08:54:29',0,0),(12,1,'爱情总是不能被预期',2,'2025-10-29 08:54:34','2025-10-29 08:54:34',0,0),(13,1,'需要勇气来面对决心',1,'2025-10-29 08:54:37','2025-10-29 08:54:37',0,0),(14,1,'调整呼吸 写下',2,'2025-10-29 08:54:40','2025-10-29 08:54:40',0,0),(15,1,'不管多少时间我会等你',2,'2025-10-29 08:54:42','2025-10-29 08:54:42',0,0),(283,2,'在我看来这 React 就是一个 BUG',1,'2025-11-12 13:57:02','2025-11-12 13:57:02',0,0),(284,1,'你好',1,'2025-11-10 14:11:01','2025-11-04 14:14:01',0,0),(285,2,'123',3,'2025-11-12 15:55:51','2025-11-12 15:55:51',0,0),(286,2,'0.0',3,'2025-11-12 15:59:20','2025-11-12 15:59:20',0,0),(287,2,'ovo',3,'2025-11-12 15:59:29','2025-11-12 15:59:29',0,0),(288,2,'?',3,'2025-11-13 08:05:47','2025-11-13 08:05:47',0,0),(289,2,'搞笑呢',3,'2025-11-13 08:05:54','2025-11-13 08:05:54',0,0),(290,1,'早安',2,'2025-11-13 08:06:32','2025-11-13 08:06:32',0,0),(291,1,'你好',1,'2025-11-13 11:42:27','2025-11-13 11:42:27',0,0),(292,1,'我',1,'2025-11-13 11:42:41','2025-11-13 11:42:41',0,0),(293,1,'我',1,'2025-11-13 11:42:59','2025-11-13 11:42:59',0,0),(294,1,'你好',2,'2025-11-13 11:43:10','2025-11-13 11:43:10',0,0),(295,1,'123',2,'2025-11-13 11:47:05','2025-11-13 11:47:05',0,0),(296,1,'123',2,'2025-11-30 21:41:24','2025-11-30 21:41:24',0,0),(297,1,'123',1,'2025-11-30 21:41:42','2025-11-30 21:41:42',0,0),(298,1,'hello',2,'2026-04-27 09:37:48','2026-04-27 09:37:48',0,0),(299,1,'好久不见',1,'2026-05-06 15:21:20','2026-05-06 15:21:20',0,0),(300,1,'heello',1,'2026-05-15 14:41:05','2026-05-15 14:41:05',0,0),(301,1,'?',2,'2026-05-15 14:41:14','2026-05-15 14:41:14',0,0),(302,1,'hello',2,'2026-06-26 14:36:35','2026-06-26 14:36:38',1,0),(303,1,'你好',1,'2026-06-26 14:37:38','2026-06-26 14:37:38',0,0),(304,1,'呃',1,'2026-06-26 14:56:46','2026-06-26 14:56:46',0,0),(305,1,'123',2,'2026-07-17 21:54:03','2026-07-17 21:54:05',1,0),(306,1,'hi',2,'2026-07-19 22:17:51','2026-07-19 22:17:51',0,0),(307,1,'aa',2,'2026-07-19 22:30:59','2026-07-19 22:30:59',0,0),(308,1,'aa',2,'2026-07-19 22:31:00','2026-07-19 22:31:00',0,0),(309,1,'aa',2,'2026-07-19 22:31:01','2026-07-19 22:31:01',0,0),(310,1,'aaa',2,'2026-07-19 22:37:44','2026-07-19 22:37:44',0,0);
/*!40000 ALTER TABLE `conversation_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conversation_user_setting`
--

DROP TABLE IF EXISTS `conversation_user_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conversation_user_setting` (
  `conversation_user_setting_id` int NOT NULL AUTO_INCREMENT,
  `conversation_id` int NOT NULL,
  `account_id` int NOT NULL,
  `pinned` tinyint(1) NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`conversation_user_setting_id`) USING BTREE,
  UNIQUE KEY `uk_conversation_user` (`conversation_id`,`account_id`),
  KEY `idx_conversation_user_setting_account_pinned` (`account_id`,`pinned`,`update_time`),
  CONSTRAINT `conversation_user_setting_ibfk_1` FOREIGN KEY (`conversation_id`) REFERENCES `conversation` (`conversation_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `conversation_user_setting_ibfk_2` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conversation_user_setting`
--

LOCK TABLES `conversation_user_setting` WRITE;
/*!40000 ALTER TABLE `conversation_user_setting` DISABLE KEYS */;
INSERT INTO `conversation_user_setting` VALUES (1,1,2,0,'2026-07-17 21:54:26','2026-07-17 21:54:28');
/*!40000 ALTER TABLE `conversation_user_setting` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `credit_account`
--

DROP TABLE IF EXISTS `credit_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `credit_account` (
  `account_id` int NOT NULL COMMENT '账号ID',
  `balance` bigint NOT NULL DEFAULT '0' COMMENT 'Credit 余额（整数，非负）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`account_id`) USING BTREE,
  CONSTRAINT `credit_account_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_credit_account_balance` CHECK ((`balance` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Credit 货币余额表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `credit_account`
--

LOCK TABLES `credit_account` WRITE;
/*!40000 ALTER TABLE `credit_account` DISABLE KEYS */;
INSERT INTO `credit_account` VALUES (1,900045,'2026-07-25 11:09:50','2026-08-19 11:33:39');
/*!40000 ALTER TABLE `credit_account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `credit_transaction`
--

DROP TABLE IF EXISTS `credit_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `credit_transaction` (
  `transaction_id` bigint NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `account_id` int NOT NULL COMMENT '账号ID',
  `delta` bigint NOT NULL COMMENT '变动数量：正=发放，负=扣减',
  `balance_after` bigint NOT NULL COMMENT '变动后余额快照',
  `change_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '变动类型：admin_grant/admin_deduct',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '变动备注',
  `operator_id` int NOT NULL COMMENT '操作管理员账号ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`transaction_id`) USING BTREE,
  KEY `idx_credit_transaction_account_time` (`account_id`,`create_time`),
  KEY `idx_credit_transaction_operator_time` (`operator_id`,`create_time`),
  CONSTRAINT `credit_transaction_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `credit_transaction_ibfk_2` FOREIGN KEY (`operator_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Credit 货币流水表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `credit_transaction`
--

LOCK TABLES `credit_transaction` WRITE;
/*!40000 ALTER TABLE `credit_transaction` DISABLE KEYS */;
INSERT INTO `credit_transaction` VALUES (1,1,100,100,'admin_grant','送的',1,'2026-07-25 11:09:50'),(2,1,-10,90,'purchase','购买商品：测试彩色头像框',1,'2026-07-26 04:19:24'),(3,1,-20,70,'purchase','购买商品：测试徽章',1,'2026-07-26 08:58:59'),(4,1,-5,65,'purchase','购买商品：黄金之星',1,'2026-07-30 07:05:38'),(5,1,-20,45,'purchase','购买商品：白色',1,'2026-07-30 07:14:56'),(6,1,-5,40,'purchase','购买商品：测试徽章',1,'2026-07-30 07:16:06'),(7,1,-1,39,'purchase','购买商品：黄金头像框',1,'2026-07-31 03:48:46'),(8,1,1000000,1000039,'admin_grant','',1,'2026-07-31 03:51:24'),(9,1,-99999,900040,'purchase','购买商品：房主有神器',1,'2026-08-17 14:08:37'),(10,1,5,900045,'daily_check_in','每日签到奖励',1,'2026-08-19 11:33:39');
/*!40000 ALTER TABLE `credit_transaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `daily_check_in`
--

DROP TABLE IF EXISTS `daily_check_in`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `daily_check_in` (
  `check_in_id` bigint NOT NULL AUTO_INCREMENT COMMENT '签到记录ID',
  `account_id` int NOT NULL COMMENT '账号ID',
  `check_in_date` date NOT NULL COMMENT '签到日期（东京自然日）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`check_in_id`) USING BTREE,
  UNIQUE KEY `uk_daily_check_in_account_date` (`account_id`,`check_in_date`),
  CONSTRAINT `daily_check_in_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='每日签到记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `daily_check_in`
--

LOCK TABLES `daily_check_in` WRITE;
/*!40000 ALTER TABLE `daily_check_in` DISABLE KEYS */;
INSERT INTO `daily_check_in` VALUES (1,1,'2026-08-19','2026-08-19 11:33:39');
/*!40000 ALTER TABLE `daily_check_in` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dashboard_activity`
--

DROP TABLE IF EXISTS `dashboard_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dashboard_activity` (
  `activity_id` bigint NOT NULL AUTO_INCREMENT COMMENT '仪表盘动态ID',
  `source_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '来源唯一键',
  `created_at` datetime NOT NULL COMMENT '动态发生时间',
  `user_id` bigint DEFAULT NULL COMMENT '操作者用户ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作者用户名',
  `action` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作对象名称',
  `target_id` bigint DEFAULT NULL COMMENT '操作对象ID',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '动态分类',
  PRIMARY KEY (`activity_id`) USING BTREE,
  UNIQUE KEY `uk_dashboard_activity_source_key` (`source_key`) USING BTREE,
  KEY `idx_dashboard_activity_created_at` (`created_at`) USING BTREE,
  KEY `idx_dashboard_activity_type_action` (`type`,`action`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=989 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dashboard_activity`
--

LOCK TABLES `dashboard_activity` WRITE;
/*!40000 ALTER TABLE `dashboard_activity` DISABLE KEYS */;
INSERT INTO `dashboard_activity` VALUES (1,'THREAD:82','2026-04-27 11:03:40',1,'鹿目まどか','POST_THREAD','大家好啊',82,'thread'),(2,'THREAD:83','2026-04-27 11:17:16',1,'鹿目まどか','POST_THREAD','Saki Saki Saki',83,'thread'),(3,'THREAD:84','2026-05-06 13:10:48',2,'绀野木棉季','POST_THREAD','测试',84,'thread'),(4,'THREAD:85','2026-05-06 13:51:19',1,'鹿目まどか','POST_THREAD','测试啊',85,'thread'),(5,'REPORT:1','2026-05-04 20:51:18',1,'鹿目まどか','SUBMIT_REPORT','ayorね',1,'report'),(126,'THREAD:86','2026-05-21 08:54:36',1,'鹿目まどか','POST_THREAD','今天天气不错',86,'thread'),(575,'THREAD:87','2026-06-24 17:32:26',2,'绀野木棉季','POST_THREAD','测试',87,'thread'),(576,'THREAD:88','2026-06-24 17:37:01',1,'鹿目まどか','POST_THREAD','实时通讯真不错',88,'thread'),(577,'THREAD:89','2026-06-24 17:38:42',1,'鹿目まどか','POST_THREAD','123',89,'thread'),(578,'THREAD:90','2026-06-24 17:41:02',1,'鹿目まどか','POST_THREAD','123',90,'thread'),(579,'THREAD:91','2026-06-24 17:41:20',1,'鹿目まどか','POST_THREAD','晓美焰最可爱',91,'thread'),(580,'THREAD:92','2026-06-24 17:48:06',1,'鹿目まどか','POST_THREAD','哇哦哦哦',92,'thread'),(581,'THREAD:93','2026-07-20 10:01:48',2,'绀野木棉季','POST_THREAD','测试一下标签',93,'thread');
/*!40000 ALTER TABLE `dashboard_activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `decoration`
--

DROP TABLE IF EXISTS `decoration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `decoration` (
  `decoration_id` int NOT NULL AUTO_INCREMENT COMMENT '装扮ID',
  `decoration_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '装扮关键字（唯一）',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '装扮名称（title 类型即头衔文本）',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '装扮描述',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型：badge/avatar_frame/title',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1=DRAFT，2=PUBLISHED，3=ARCHIVED',
  `draft_config` json DEFAULT NULL COMMENT '编辑中的结构化配置',
  `published_config` json DEFAULT NULL COMMENT '已发布配置（用户端只读此字段）',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `published_at` datetime DEFAULT NULL COMMENT '最近发布时间',
  `created_by` int DEFAULT NULL COMMENT '创建管理员账号ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`decoration_id`) USING BTREE,
  UNIQUE KEY `uk_decoration_key` (`decoration_key`),
  KEY `idx_decoration_status_type` (`status`,`type`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='装扮设计表（低代码平台）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `decoration`
--

LOCK TABLES `decoration` WRITE;
/*!40000 ALTER TABLE `decoration` DISABLE KEYS */;
INSERT INTO `decoration` VALUES (6,'frame_golden','黄金','','avatar_frame',2,'{\"mode\": \"css\", \"scale\": 1.1, \"border\": {\"color\": \"#ffd700\", \"width\": 0.05}, \"animation\": {\"type\": \"none\", \"durationMs\": 2000}, \"schemaVersion\": 2}','{\"mode\": \"css\", \"scale\": 1.1, \"border\": {\"color\": \"#ffd700\", \"width\": 0.05}, \"animation\": {\"type\": \"none\", \"durationMs\": 2000}, \"schemaVersion\": 2}',4,'2026-07-31 11:49:04',1,0,'2026-07-31 03:02:53','2026-07-31 03:02:53'),(7,'badget_owner','房主有神器','','badge',2,'{\"mode\": \"icon\", \"size\": 0.4, \"color\": \"#ffffff\", \"iconKey\": \"crown\", \"background\": \"#f65a3b\", \"schemaVersion\": 2}','{\"mode\": \"icon\", \"size\": 0.4, \"color\": \"#ffffff\", \"iconKey\": \"crown\", \"background\": \"#f65a3b\", \"schemaVersion\": 2}',2,'2026-07-31 11:50:40',1,0,'2026-07-31 03:50:06','2026-07-31 03:50:06');
/*!40000 ALTER TABLE `decoration` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `feedback`
--

DROP TABLE IF EXISTS `feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `feedback` (
  `feedback_id` int NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `account_id` int NOT NULL COMMENT '提交用户账号ID',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '反馈类型',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '反馈内容',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '处理状态',
  `handler_account_id` int DEFAULT NULL COMMENT '处理管理员账号ID',
  `handle_note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '处理备注',
  `handled_at` datetime DEFAULT NULL COMMENT '处理完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`feedback_id`) USING BTREE,
  KEY `idx_feedback_account_time` (`account_id`,`create_time`) USING BTREE,
  KEY `idx_feedback_status_time` (`status`,`create_time`) USING BTREE,
  KEY `idx_feedback_type_time` (`type`,`create_time`) USING BTREE,
  KEY `fk_feedback_handler` (`handler_account_id`),
  CONSTRAINT `fk_feedback_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_feedback_handler` FOREIGN KEY (`handler_account_id`) REFERENCES `account` (`account_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feedback`
--

LOCK TABLES `feedback` WRITE;
/*!40000 ALTER TABLE `feedback` DISABLE KEYS */;
INSERT INTO `feedback` VALUES (1,2,'SUGGESTION','傻逼网站，赶紧倒闭了算了','RESOLVED',1,'你滚吧','2026-06-23 17:35:49','2026-06-22 21:32:45','2026-06-23 17:35:49');
/*!40000 ALTER TABLE `feedback` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `follow_message`
--

DROP TABLE IF EXISTS `follow_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `follow_message` (
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
  KEY `idx_follow_message_account_time` (`account_id`,`create_time`),
  KEY `idx_follow_message_from_account` (`from_account_id`),
  KEY `idx_follow_message_thread` (`thread_id`),
  KEY `idx_follow_message_topic` (`topic_id`),
  CONSTRAINT `follow_message_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `follow_message_ibfk_2` FOREIGN KEY (`from_account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `follow_message`
--

LOCK TABLES `follow_message` WRITE;
/*!40000 ALTER TABLE `follow_message` DISABLE KEYS */;
INSERT INTO `follow_message` VALUES (1,2,1,92,1,'/threads/92','哇哦哦哦','哇哦哦哦','2026-06-24 17:48:06'),(2,1,2,93,1,'/threads/93','测试一下标签','测试一下标签','2026-07-20 10:01:48');
/*!40000 ALTER TABLE `follow_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `history`
--

DROP TABLE IF EXISTS `history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `history` (
  `history_id` int NOT NULL,
  `thread_id` int DEFAULT NULL,
  `account_id` int DEFAULT NULL,
  `visit_time` datetime DEFAULT NULL,
  PRIMARY KEY (`history_id`) USING BTREE,
  KEY `thread_id` (`thread_id`) USING BTREE,
  KEY `account_id` (`account_id`) USING BTREE,
  CONSTRAINT `history_ibfk_1` FOREIGN KEY (`thread_id`) REFERENCES `thread` (`thread_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `history_ibfk_2` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `history`
--

LOCK TABLES `history` WRITE;
/*!40000 ALTER TABLE `history` DISABLE KEYS */;
/*!40000 ALTER TABLE `history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `image_asset`
--

DROP TABLE IF EXISTS `image_asset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `image_asset` (
  `asset_id` int NOT NULL AUTO_INCREMENT,
  `account_id` int DEFAULT NULL COMMENT '上传者账号ID，可为空以兼容历史自动收编',
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
  `asset_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'STICKER',
  `visibility` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `favorite_count` int NOT NULL DEFAULT '0' COMMENT '已添加该表情包的用户数',
  `use_count` int NOT NULL DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`asset_id`) USING BTREE,
  UNIQUE KEY `uk_image_asset_url` (`url`) USING BTREE,
  KEY `idx_image_asset_status` (`status`) USING BTREE,
  KEY `idx_image_asset_account_status` (`account_id`,`asset_type`,`status`) USING BTREE,
  KEY `idx_image_asset_asset_type` (`asset_type`) USING BTREE,
  CONSTRAINT `fk_image_asset_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `image_asset`
--

LOCK TABLES `image_asset` WRITE;
/*!40000 ALTER TABLE `image_asset` DISABLE KEYS */;
INSERT INTO `image_asset` VALUES (2,1,'nineforum/image-assets/1/803b242a105d43698cc774ad94aed6c8.webp','image-assets/1/803b242a105d43698cc774ad94aed6c8.webp','jpg','webp','image/webp',27786,398,398,'bc212bce5bf6aee791dc7db6cd4a5c1eb1ae3c2ad7c8e94e5ad8662618e251f3','UPLOAD','STICKER','PRIVATE','ACTIVE',0,2,'2026-05-08 14:26:46','2026-05-08 06:52:38'),(3,1,'nineforum/image-assets/1/446fabe713a8464797947ecf9705f7ab.webp','image-assets/1/446fabe713a8464797947ecf9705f7ab.webp','png','webp','image/webp',43442,512,512,'11c8413255e6d34ff7d394242e2180f005335c1f65385f35f787b82f3913eb01','UPLOAD','STICKER','PRIVATE','ACTIVE',0,1,'2026-05-08 14:51:26','2026-05-22 08:06:31'),(4,1,'nineforum/posts/85/c2b451a22ac64c6eb31f8350b1f328c2.jpeg','posts/85/c2b451a22ac64c6eb31f8350b1f328c2.jpeg','jpeg','jpeg','image/jpeg',8745282,3000,4791,'30ff4829020970398562a906a414389c113edf1c1a53c2e5adead39f6450b889','CONTENT','IMAGE','PUBLIC','ACTIVE',0,1,'2026-05-08 14:52:38','2026-05-08 06:52:38'),(5,1,'nineforum/image-assets/1/2cf7aaf05edc43d5a35cef44e6b4cd73.webp','image-assets/1/2cf7aaf05edc43d5a35cef44e6b4cd73.webp','jpg','webp','image/webp',24164,512,512,'ccc82a26f478b113a962b0ddf1010a7c66a3a33c2e4117afe90c30e8d01a677f','UPLOAD','STICKER','PRIVATE','ACTIVE',0,0,'2026-05-08 15:37:49','2026-05-08 15:37:49'),(6,1,'nineforum/stickers/1/7d3e460be61f4e8591e1c1b8d191bcbe.webp','stickers/1/7d3e460be61f4e8591e1c1b8d191bcbe.webp','jpg','webp','image/webp',27786,398,398,'bc212bce5bf6aee791dc7db6cd4a5c1eb1ae3c2ad7c8e94e5ad8662618e251f3','UPLOAD','STICKER','PRIVATE','ACTIVE',0,0,'2026-05-08 17:03:45','2026-05-09 09:35:29'),(7,2,'nineforum/stickers/2/8108714f48be42feaab00e59b5220f59.webp','stickers/2/8108714f48be42feaab00e59b5220f59.webp','jpg','webp','image/webp',27786,398,398,'bc212bce5bf6aee791dc7db6cd4a5c1eb1ae3c2ad7c8e94e5ad8662618e251f3','UPLOAD','STICKER','PRIVATE','ACTIVE',2,0,'2026-05-09 17:07:24','2026-05-09 09:34:10');
/*!40000 ALTER TABLE `image_asset` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `image_asset_favorite`
--

DROP TABLE IF EXISTS `image_asset_favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `image_asset_favorite` (
  `favorite_id` int NOT NULL AUTO_INCREMENT,
  `account_id` int NOT NULL,
  `asset_id` int NOT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`favorite_id`) USING BTREE,
  UNIQUE KEY `uk_image_asset_favorite` (`account_id`,`asset_id`) USING BTREE,
  KEY `idx_image_asset_favorite_asset` (`asset_id`) USING BTREE,
  CONSTRAINT `fk_image_asset_favorite_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_image_asset_favorite_asset` FOREIGN KEY (`asset_id`) REFERENCES `image_asset` (`asset_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `image_asset_favorite`
--

LOCK TABLES `image_asset_favorite` WRITE;
/*!40000 ALTER TABLE `image_asset_favorite` DISABLE KEYS */;
INSERT INTO `image_asset_favorite` VALUES (2,2,7,'2026-05-09 17:07:24'),(4,1,7,'2026-05-09 17:34:10');
/*!40000 ALTER TABLE `image_asset_favorite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `like_thread`
--

DROP TABLE IF EXISTS `like_thread`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `like_thread` (
  `like_id` int NOT NULL AUTO_INCREMENT COMMENT '喜欢ID',
  `account_id` int DEFAULT NULL COMMENT '谁喜欢',
  `thread_id` int DEFAULT NULL COMMENT '喜欢的帖子',
  PRIMARY KEY (`like_id`) USING BTREE,
  KEY `account_id` (`account_id`) USING BTREE,
  CONSTRAINT `like_thread_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `like_thread`
--

LOCK TABLES `like_thread` WRITE;
/*!40000 ALTER TABLE `like_thread` DISABLE KEYS */;
INSERT INTO `like_thread` VALUES (23,1,72),(26,1,75),(29,1,79),(33,2,86),(34,1,86);
/*!40000 ALTER TABLE `like_thread` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mention_message`
--

DROP TABLE IF EXISTS `mention_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mention_message` (
  `mention_message_id` int NOT NULL AUTO_INCREMENT COMMENT '@消息ID',
  `account_id` int NOT NULL COMMENT '被@的用户ID',
  `from_account_id` int NOT NULL COMMENT '发起@的用户ID',
  `source_type` varchar(16) NOT NULL COMMENT '@来源类型：post/comment/reply',
  `source_id` int NOT NULL COMMENT '@来源ID',
  `thread_id` int NOT NULL COMMENT '所属主题/帖子ID',
  `path` varchar(255) NOT NULL COMMENT '跳转路径',
  `content_summary` varchar(255) NOT NULL DEFAULT '' COMMENT '内容摘要',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`mention_message_id`) USING BTREE,
  KEY `idx_mention_message_account_time` (`account_id`,`create_time` DESC),
  KEY `idx_mention_message_from_account` (`from_account_id`),
  KEY `idx_mention_message_source` (`source_type`,`source_id`),
  KEY `idx_mention_message_thread` (`thread_id`),
  CONSTRAINT `fk_mention_message_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_mention_message_from_account` FOREIGN KEY (`from_account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='@消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mention_message`
--

LOCK TABLES `mention_message` WRITE;
/*!40000 ALTER TABLE `mention_message` DISABLE KEYS */;
INSERT INTO `mention_message` VALUES (1,1,2,'POST',93,85,'/threads/85#post-93','@鹿目まどか 你好','2026-05-06 13:59:29'),(2,1,2,'POST',94,85,'/threads/85#post-94','@鹿目まどか 123','2026-05-06 20:20:27'),(3,2,1,'POST',95,83,'/threads/83#post-95','@ayorね hELLO','2026-05-07 12:51:38'),(4,2,1,'POST',96,83,'/threads/83#post-96','@ayorね','2026-05-07 12:52:03');
/*!40000 ALTER TABLE `mention_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `passkey_credential`
--

DROP TABLE IF EXISTS `passkey_credential`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `passkey_credential` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account_id` int NOT NULL,
  `credential_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_handle` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `attestation_object` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `client_data_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `signature_count` bigint NOT NULL DEFAULT '0',
  `transports` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `backup_eligible` tinyint DEFAULT '0',
  `backup_state` tinyint DEFAULT '0',
  `uv_initialized` tinyint DEFAULT '0',
  `label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_used_at` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_passkey_credential_id` (`credential_id`) USING BTREE,
  KEY `idx_passkey_account_id` (`account_id`) USING BTREE,
  CONSTRAINT `fk_passkey_credential_account` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `passkey_credential`
--

LOCK TABLES `passkey_credential` WRITE;
/*!40000 ALTER TABLE `passkey_credential` DISABLE KEYS */;
INSERT INTO `passkey_credential` VALUES (6,1,'jSSJJUIPsSv6fkx_rWRsf6baFjjfuKbW8KyLUprA2v0','MQ','o2NmbXRkbm9uZWdhdHRTdG10oGhhdXRoRGF0YVikSZYN5YgOjGh0NBcPZHZgW4_krrmihjLHmVzzuoMdl2NFAAAAAJ3dGBevWkZyork-PdlQAKkAII0kiSVCD7Er-n5Mf61kbH-m2hY437im1vCsi1KawNr9pQECAyYgASFYILvqpoRA7lyOYW3R7rQ5g5yO3erSXaF1H2FlRd4TnkKpIlggd-vFbOk3tH5qzK3MYhynFf5jWNm_shDTvMNYntfGQ0E','eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoiSVBHRDVBdU5obm1hYmpqY1YzbVNTTnItZnFpc1MtdFdycEVrb3VTUWpZMCIsIm9yaWdpbiI6Imh0dHA6Ly9sb2NhbGhvc3Q6MTAwNzEiLCJjcm9zc09yaWdpbiI6ZmFsc2V9',6,'internal',0,0,1,'我的电脑','2026-05-28 10:03:03','2026-05-07 12:49:17','2026-05-28 10:03:03');
/*!40000 ALTER TABLE `passkey_credential` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permission`
--

DROP TABLE IF EXISTS `permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permission` (
  `permission_id` int NOT NULL AUTO_INCREMENT COMMENT '权限id',
  `role_id` int NOT NULL COMMENT '哪些权能者拥有权限',
  `permission` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限标识',
  PRIMARY KEY (`permission_id` DESC) USING BTREE,
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission`),
  CONSTRAINT `permission_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permission`
--

LOCK TABLES `permission` WRITE;
/*!40000 ALTER TABLE `permission` DISABLE KEYS */;
INSERT INTO `permission` VALUES (29,1,'ASSIGN_ROLE'),(35,1,'BAN_ACCOUNT'),(38,1,'CREATE_THEME'),(41,1,'CREATE_TOPIC'),(33,1,'DELETE_ACCOUNT'),(50,1,'DELETE_POST'),(46,1,'DELETE_TAG'),(40,1,'DELETE_THEME'),(49,1,'DELETE_THREAD'),(43,1,'DELETE_TOPIC'),(52,1,'HANDLE_REPORT'),(37,1,'HANDLE_USER_VIOLATION'),(44,1,'INSERT_TAG'),(31,1,'MANAGE_ACCOUNT'),(64,1,'MANAGE_COLLECT'),(62,1,'MANAGE_CONVERSATION'),(63,1,'MANAGE_CONVERSATION_MESSAGE'),(66,1,'MANAGE_HISTORY'),(55,1,'MANAGE_IMAGE_ASSET'),(65,1,'MANAGE_LIKE'),(57,1,'MANAGE_PAGE_BROADCAST'),(28,1,'MANAGE_PERMISSION'),(67,1,'MANAGE_ROLE'),(60,1,'MANAGE_STAT'),(58,1,'MANAGE_TOPIC_CHAT'),(56,1,'MANAGE_USER_BROADCAST'),(54,1,'MODERATE_CONTENT'),(34,1,'MUTE_ACCOUNT'),(61,1,'REPAIR_DATA'),(36,1,'RESTORE_ACCOUNT'),(30,1,'REVOKE_ROLE'),(48,1,'SET_ANNOUNCEMENT'),(51,1,'SET_THREAD_SELECTED'),(32,1,'UPDATE_ACCOUNT'),(45,1,'UPDATE_TAG'),(39,1,'UPDATE_THEME'),(47,1,'UPDATE_THREAD_TAG'),(42,1,'UPDATE_TOPIC'),(59,1,'VIEW_DASHBOARD'),(53,1,'VIEW_REPORT'),(25,4,'DELETE_POST'),(21,4,'DELETE_TAG'),(24,4,'DELETE_THREAD'),(19,4,'INSERT_TAG'),(23,4,'SET_ANNOUNCEMENT'),(26,4,'SET_THREAD_SELECTED'),(20,4,'UPDATE_TAG'),(22,4,'UPDATE_THREAD_TAG');
/*!40000 ALTER TABLE `permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permission_operation_log`
--

DROP TABLE IF EXISTS `permission_operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permission_operation_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限操作日志ID',
  `user_id` int NOT NULL COMMENT '操作者用户ID',
  `action` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `target_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作对象类型',
  `target_id` bigint DEFAULT NULL COMMENT '操作对象ID',
  `method` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行方法',
  `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '请求参数JSON',
  `duration_ms` bigint NOT NULL COMMENT '执行耗时毫秒',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`log_id`) USING BTREE,
  KEY `idx_permission_operation_log_create_time` (`create_time`) USING BTREE,
  KEY `idx_permission_operation_log_user_action` (`user_id`,`action`) USING BTREE,
  KEY `idx_permission_operation_log_target` (`target_type`,`target_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permission_operation_log`
--

LOCK TABLES `permission_operation_log` WRITE;
/*!40000 ALTER TABLE `permission_operation_log` DISABLE KEYS */;
INSERT INTO `permission_operation_log` VALUES (7,1,'UPDATE_THREAD_TAG','thread',86,'PermThreadController.updateTag','{\"threadId\":86,\"topicId\":1,\"tagId\":2}',33,'2026-05-26 17:24:18'),(8,2,'UPDATE_THREAD_TAG','thread',84,'PermThreadController.updateTag','{\"threadId\":84,\"topicId\":1,\"tagId\":1}',11,'2026-05-26 17:29:16'),(9,1,'SET_GLOBAL_ANNOUNCEMENT','thread',86,'PermThreadController.setGlobalAnnouncement','{\"threadId\":86}',114,'2026-06-15 20:07:06'),(10,2,'UPDATE_THREAD_TAG','thread',92,'PermThreadController.updateTag','{\"threadId\":92,\"topicId\":1,\"tagId\":2}',41,'2026-07-20 10:01:35');
/*!40000 ALTER TABLE `permission_operation_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post`
--

DROP TABLE IF EXISTS `post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post` (
  `post_id` int NOT NULL AUTO_INCREMENT COMMENT '楼层ID',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '内容',
  `account_id` int DEFAULT NULL COMMENT '账户ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `thread_id` int DEFAULT NULL COMMENT '回复的帖子',
  `reply_to` int DEFAULT NULL,
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除',
  `topic_id` int DEFAULT NULL,
  PRIMARY KEY (`post_id`) USING BTREE,
  KEY `account_id` (`account_id`) USING BTREE,
  KEY `thread_id` (`thread_id`) USING BTREE,
  KEY `idx_post_reply_to` (`reply_to`),
  CONSTRAINT `db_post_ibfk_3` FOREIGN KEY (`reply_to`) REFERENCES `post` (`post_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `post_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `post_ibfk_2` FOREIGN KEY (`thread_id`) REFERENCES `thread` (`thread_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=132 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post`
--

LOCK TABLES `post` WRITE;
/*!40000 ALTER TABLE `post` DISABLE KEYS */;
INSERT INTO `post` VALUES (79,'{\"type\":\"doc\",\"content\":[{\"type\":\"image\",\"attrs\":{\"src\":\"nineforum/posts/82/99568f031f26498ebd31958529039670.png\"}},{\"type\":\"paragraph\"}]}',1,'2026-04-27 11:11:58',NULL,82,NULL,0,1),(80,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"我是傻逼\"}]},{\"type\":\"paragraph\"}]}',1,'2026-04-27 11:19:54',NULL,82,NULL,0,1),(81,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"ASHIAhiasud\"}]},{\"type\":\"paragraph\"}]}',2,'2026-04-29 13:51:25',NULL,82,NULL,0,1),(82,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"mention\",\"attrs\":{\"id\":\"2\",\"label\":\"ayorね\",\"mentionSuggestionChar\":\"@\"}},{\"type\":\"text\",\"text\":\" 你好\"}]},{\"type\":\"paragraph\"}]}',1,'2026-05-06 12:25:34',NULL,83,NULL,0,1),(83,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"mention\",\"attrs\":{\"id\":\"2\",\"label\":\"ayorね\",\"mentionSuggestionChar\":\"@\"}},{\"type\":\"text\",\"text\":\" 123\"}]}]}',1,'2026-05-06 13:03:05',NULL,83,NULL,0,1),(84,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"hello\"}]},{\"type\":\"paragraph\"}]}',2,'2026-05-06 13:10:25',NULL,83,NULL,0,1),(85,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"aaa\"}]}]}',2,'2026-05-06 13:10:34',NULL,83,NULL,0,1),(86,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"mention\",\"attrs\":{\"id\":\"1\",\"label\":\"鹿目まどか\",\"mentionSuggestionChar\":\"@\"}},{\"type\":\"text\",\"text\":\" 我草\"}]},{\"type\":\"paragraph\"}]}',2,'2026-05-06 13:10:58',NULL,84,NULL,0,1),(88,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"www\"}]}]}',2,'2026-05-06 13:43:56',NULL,84,NULL,0,1),(89,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"www\"}]}]}',2,'2026-05-06 13:44:14',NULL,83,NULL,0,1),(90,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"heelo\"}]}]}',1,'2026-05-06 13:47:34',NULL,84,NULL,0,1),(91,'{\"type\":\"doc\",\"content\":[{\"type\":\"heading\",\"attrs\":{\"level\":2},\"content\":[{\"type\":\"text\",\"text\":\"2222\"}]},{\"type\":\"paragraph\"}]}',2,'2026-05-06 13:51:33',NULL,85,NULL,0,1),(92,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"mention\",\"attrs\":{\"id\":\"1\",\"label\":\"鹿目まどか\",\"mentionSuggestionChar\":\"@\"}},{\"type\":\"text\",\"text\":\" 你妈逼\"}]}]}',2,'2026-05-06 13:52:17',NULL,85,NULL,0,1),(93,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"mention\",\"attrs\":{\"id\":\"1\",\"label\":\"鹿目まどか\",\"mentionSuggestionChar\":\"@\"}},{\"type\":\"text\",\"text\":\" 你好\"}]}]}',2,'2026-05-06 13:59:29',NULL,85,NULL,0,1),(94,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"mention\",\"attrs\":{\"id\":\"1\",\"label\":\"鹿目まどか\",\"mentionSuggestionChar\":\"@\"}},{\"type\":\"text\",\"text\":\" 123\"}]}]}',2,'2026-05-06 20:20:27',NULL,85,NULL,0,1),(95,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"mention\",\"attrs\":{\"id\":\"2\",\"label\":\"ayorね\",\"mentionSuggestionChar\":\"@\"}},{\"type\":\"text\",\"text\":\" \"},{\"type\":\"text\",\"marks\":[{\"type\":\"bold\"}],\"text\":\"hELLO\"}]}]}',1,'2026-05-07 12:51:38',NULL,83,NULL,0,1),(96,'{\"type\":\"doc\",\"content\":[{\"type\":\"image\",\"attrs\":{\"src\":\"nineforum/posts/83/8d396b8ab3904b31a33d9be802411936.png\"}},{\"type\":\"paragraph\",\"content\":[{\"type\":\"mention\",\"attrs\":{\"id\":\"2\",\"label\":\"ayorね\",\"mentionSuggestionChar\":\"@\"}},{\"type\":\"text\",\"text\":\" \"}]}]}',1,'2026-05-07 12:52:03',NULL,83,NULL,0,1),(97,'{\"type\":\"doc\",\"content\":[{\"type\":\"image\",\"attrs\":{\"src\":\"http://localhost:9000/nineforum/image-assets/1/803b242a105d43698cc774ad94aed6c8.webp\"}},{\"type\":\"paragraph\"}]}',1,'2026-05-08 14:26:48',NULL,85,NULL,0,1),(98,'{\"type\":\"doc\",\"content\":[{\"type\":\"image\",\"attrs\":{\"src\":\"http://localhost:9000/nineforum/image-assets/1/446fabe713a8464797947ecf9705f7ab.webp\"}},{\"type\":\"paragraph\"}]}',1,'2026-05-08 14:51:28',NULL,85,NULL,0,1),(99,'{\"type\":\"doc\",\"content\":[{\"type\":\"image\",\"attrs\":{\"src\":\"nineforum/posts/85/c2b451a22ac64c6eb31f8350b1f328c2.jpeg\"}},{\"type\":\"image\",\"attrs\":{\"src\":\"http://localhost:9000/nineforum/image-assets/1/803b242a105d43698cc774ad94aed6c8.webp\"}},{\"type\":\"paragraph\"}]}',1,'2026-05-08 14:52:38',NULL,85,NULL,0,1),(100,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"sticker\",\"attrs\":{\"src\":\"http://localhost:9000/nineforum/image-assets/1/446fabe713a8464797947ecf9705f7ab.webp\"}}]}]}',1,'2026-05-08 15:05:05',NULL,85,NULL,0,1),(101,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"sticker\",\"attrs\":{\"src\":\"http://localhost:9000/nineforum/stickers/1/7d3e460be61f4e8591e1c1b8d191bcbe.webp\"}}]}]}',1,'2026-05-08 17:03:49',NULL,85,NULL,0,1),(102,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"sticker\",\"attrs\":{\"src\":\"http://localhost:9000/nineforum/stickers/1/7d3e460be61f4e8591e1c1b8d191bcbe.webp\"}},{\"type\":\"text\",\"text\":\"你好啊\"}]}]}',1,'2026-05-08 17:04:43',NULL,85,NULL,0,1),(103,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"测试\"}]}]}',1,'2026-05-09 08:15:52',NULL,85,NULL,0,1),(104,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"老天爷啊\"}]}]}',1,'2026-05-09 08:17:24',NULL,85,NULL,0,1),(105,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"taaaaaa\"}]}]}',1,'2026-05-09 08:24:03',NULL,85,NULL,0,1),(106,'{\"type\":\"doc\",\"content\":[{\"type\":\"sticker\",\"attrs\":{\"src\":\"http://localhost:9000/nineforum/stickers/2/8108714f48be42feaab00e59b5220f59.webp\"}},{\"type\":\"paragraph\"}]}',2,'2026-05-09 17:07:25',NULL,85,NULL,0,1),(107,'{\"type\":\"doc\",\"content\":[{\"type\":\"sticker\",\"attrs\":{\"src\":\"http://localhost:9000/nineforum/stickers/1/7d3e460be61f4e8591e1c1b8d191bcbe.webp\"}},{\"type\":\"paragraph\"}]}',1,'2026-05-09 17:34:18',NULL,85,NULL,0,1),(108,'{\"type\":\"doc\",\"content\":[{\"type\":\"sticker\",\"attrs\":{\"src\":\"http://localhost:9000/nineforum/image-assets/1/446fabe713a8464797947ecf9705f7ab.webp\"}},{\"type\":\"paragraph\"}]}',1,'2026-05-09 17:34:20',NULL,85,NULL,0,1),(109,'{\"type\":\"doc\",\"content\":[{\"type\":\"sticker\",\"attrs\":{\"src\":\"http://localhost:9000/nineforum/stickers/2/8108714f48be42feaab00e59b5220f59.webp\"}},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"乐乐\"}]}]}',1,'2026-05-20 14:43:06',NULL,85,NULL,0,1),(110,'{\"type\":\"doc\",\"content\":[{\"type\":\"sticker\",\"attrs\":{\"src\":\"http://localhost:9000/nineforum/stickers/2/8108714f48be42feaab00e59b5220f59.webp\"}},{\"type\":\"paragraph\"}]}',1,'2026-05-20 14:43:28',NULL,85,NULL,0,1),(111,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"123\"}]}]}',1,'2026-05-20 14:43:30',NULL,85,NULL,0,1),(112,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"123\"}]}]}',1,'2026-05-20 14:43:33',NULL,85,NULL,0,1),(113,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"123\"}]}]}',1,'2026-05-20 14:44:47',NULL,85,NULL,0,1),(114,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"2222\"}]}]}',1,'2026-05-20 14:45:00',NULL,85,NULL,0,1),(115,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"123\"}]}]}',1,'2026-05-20 14:55:56',NULL,85,NULL,0,1),(116,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"123\"}]}]}',2,'2026-05-20 14:56:23',NULL,85,NULL,0,1),(117,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"aaaa\"}]}]}',1,'2026-05-20 14:56:34',NULL,84,NULL,0,1),(118,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"hello there\"}]}]}',2,'2026-05-20 19:38:43',NULL,85,NULL,0,1),(119,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Hello There\"}]}]}',2,'2026-05-20 19:38:55',NULL,85,NULL,0,1),(120,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Hello\"}]}]}',2,'2026-05-25 10:40:37',NULL,86,NULL,1,1),(121,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"你可以不玩\"}]}]}',1,'2026-05-25 10:41:00','2026-06-16 17:20:31',86,NULL,0,1),(122,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"测试\"}]},{\"type\":\"paragraph\"}]}',1,'2026-06-24 17:31:20',NULL,86,NULL,0,1),(123,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"测试2\"}]}]}',1,'2026-06-24 17:32:01',NULL,86,NULL,0,1),(124,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"greet\"}]}]}',1,'2026-06-24 17:36:04',NULL,87,NULL,0,1),(125,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"你妈\"}]}]}',1,'2026-06-24 17:39:34',NULL,89,NULL,0,1),(126,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"啊啊啊\"}]}]}',1,'2026-06-24 17:42:18',NULL,91,NULL,0,1),(127,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"啊啊啊啊\"}]}]}',2,'2026-06-24 17:42:23',NULL,91,NULL,0,1),(128,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"123\"}]}]}',2,'2026-06-24 18:20:33',NULL,92,NULL,0,1),(129,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"我不知道\"}]}]}',2,'2026-06-24 18:20:39','2026-06-24 18:23:15',92,128,0,1),(130,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"aaa\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"a\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"a\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"a\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"a\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"a\"}]},{\"type\":\"paragraph\"},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"a\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"a\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"a\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"a\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"a\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"aa\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"a\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"a\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"a\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"a\"}]},{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"a\"}]},{\"type\":\"paragraph\"}]}',2,'2026-06-26 12:21:02',NULL,91,NULL,0,1),(131,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"aaa\"},{\"type\":\"hardBreak\"},{\"type\":\"hardBreak\"},{\"type\":\"hardBreak\"},{\"type\":\"hardBreak\"},{\"type\":\"hardBreak\"},{\"type\":\"hardBreak\"},{\"type\":\"hardBreak\"},{\"type\":\"hardBreak\"},{\"type\":\"hardBreak\"},{\"type\":\"hardBreak\"},{\"type\":\"hardBreak\"},{\"type\":\"hardBreak\"},{\"type\":\"hardBreak\"},{\"type\":\"hardBreak\"},{\"type\":\"hardBreak\"},{\"type\":\"text\",\"text\":\"aaa\"}]}]}',2,'2026-07-18 14:36:11',NULL,92,NULL,0,1);
/*!40000 ALTER TABLE `post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post_edit_history`
--

DROP TABLE IF EXISTS `post_edit_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_edit_history` (
  `history_id` int NOT NULL AUTO_INCREMENT COMMENT '编辑历史ID',
  `post_id` int NOT NULL COMMENT '回复ID',
  `editor_account_id` int NOT NULL COMMENT '编辑者账号ID',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '编辑前内容快照(TipTap JSON)',
  `edit_time` datetime NOT NULL COMMENT '本次编辑发生时间',
  PRIMARY KEY (`history_id`) USING BTREE,
  KEY `idx_post_id` (`post_id`) USING BTREE,
  KEY `idx_editor_account_id` (`editor_account_id`) USING BTREE,
  CONSTRAINT `db_post_edit_history_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `db_post_edit_history_ibfk_2` FOREIGN KEY (`editor_account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_edit_history`
--

LOCK TABLES `post_edit_history` WRITE;
/*!40000 ALTER TABLE `post_edit_history` DISABLE KEYS */;
INSERT INTO `post_edit_history` VALUES (1,121,1,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Hello There\"}]}]}','2026-06-16 17:20:31'),(2,129,2,'{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"我不知道啊\"}]}]}','2026-06-24 18:23:15');
/*!40000 ALTER TABLE `post_edit_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `privacy`
--

DROP TABLE IF EXISTS `privacy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `privacy` (
  `private_id` int NOT NULL COMMENT '隐私表, 用来管理用户的隐私设置',
  `account_id` int NOT NULL COMMENT '用户ID',
  `is_activity_show` tinyint DEFAULT NULL COMMENT '是否显示动态',
  `is_like_show` tinyint DEFAULT NULL COMMENT '是否显示喜欢的帖',
  `is_collect_show` tinyint DEFAULT NULL COMMENT '是否显示收藏的帖',
  `is_private_message_allow` tinyint DEFAULT NULL COMMENT '是否允许私信',
  PRIMARY KEY (`private_id` DESC) USING BTREE,
  KEY `account_id` (`account_id`) USING BTREE,
  CONSTRAINT `privacy_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `privacy`
--

LOCK TABLES `privacy` WRITE;
/*!40000 ALTER TABLE `privacy` DISABLE KEYS */;
/*!40000 ALTER TABLE `privacy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `report`
--

DROP TABLE IF EXISTS `report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `report` (
  `report_id` int NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `reporter_account_id` int NOT NULL COMMENT '举报人账号ID',
  `reported_account_id` int NOT NULL COMMENT '被举报人账号ID',
  `target_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报对象类型',
  `target_id` int NOT NULL COMMENT '举报对象ID',
  `report_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报类型代码',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报描述',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报状态',
  `handler_account_id` int DEFAULT NULL COMMENT '处理管理员账号ID',
  `handle_note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '处理备注',
  `handled_at` datetime DEFAULT NULL COMMENT '处理时间',
  `reported_username_snapshot` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '被举报用户名快照',
  `target_summary_snapshot` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '被举报对象摘要快照',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`report_id`) USING BTREE,
  KEY `idx_report_target` (`target_type`,`target_id`) USING BTREE,
  KEY `idx_report_reporter` (`reporter_account_id`) USING BTREE,
  KEY `idx_report_reported` (`reported_account_id`) USING BTREE,
  KEY `idx_report_status` (`status`) USING BTREE,
  CONSTRAINT `report_ibfk_1` FOREIGN KEY (`reporter_account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `report_ibfk_2` FOREIGN KEY (`reported_account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `report`
--

LOCK TABLES `report` WRITE;
/*!40000 ALTER TABLE `report` DISABLE KEYS */;
INSERT INTO `report` VALUES (1,1,2,'USER',2,'HARASSMENT','你妈死了你妈死了你妈死了你妈死了你妈死了','RESOLVED',1,'呵呵','2026-05-04 21:33:10','ayorね','ayorね','2026-05-04 20:51:18','2026-05-04 21:33:10');
/*!40000 ALTER TABLE `report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `role_id` int NOT NULL AUTO_INCREMENT COMMENT '权能id',
  `role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '权能称呼',
  `priority` int DEFAULT NULL COMMENT '权限等级, 数字越大权限越高\r\n',
  `topic_id` int DEFAULT NULL COMMENT '权限作用域',
  `role_nick` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,'OWNER',9999,NULL,'OWNER'),(3,'USER',0,NULL,'用户'),(4,'MODERATOR',1,1,'闲聊版主');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shop_item`
--

DROP TABLE IF EXISTS `shop_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shop_item` (
  `item_id` int NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称（title 类型即头衔文本）',
  `item_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品关键字（唯一，前端素材映射用）',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商品描述',
  `item_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型：badge/avatar_frame/title',
  `decoration_id` int DEFAULT NULL COMMENT '绑定的装扮ID（可空，空则前端回退 item_key 硬编码渲染）',
  `price` bigint NOT NULL COMMENT '售价（Credit，整数）',
  `stock` bigint NOT NULL DEFAULT '-1' COMMENT '库存，-1=不限量',
  `purchase_limit` int NOT NULL DEFAULT '0' COMMENT '每人限购数量，0=不限购（预留堆叠道具，装饰类天然限购1件）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1=上架，2=下架',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`item_id`) USING BTREE,
  UNIQUE KEY `uk_shop_item_item_key` (`item_key`),
  KEY `idx_shop_item_status_type` (`status`,`item_type`),
  KEY `idx_shop_item_decoration` (`decoration_id`),
  CONSTRAINT `shop_item_ibfk_decoration` FOREIGN KEY (`decoration_id`) REFERENCES `decoration` (`decoration_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_shop_item_price` CHECK ((`price` >= 0)),
  CONSTRAINT `chk_shop_item_stock` CHECK ((`stock` >= -1))
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商城商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shop_item`
--

LOCK TABLES `shop_item` WRITE;
/*!40000 ALTER TABLE `shop_item` DISABLE KEYS */;
INSERT INTO `shop_item` VALUES (6,'黄金头像框','golden_badget_test',NULL,'avatar_frame',6,1,-1,1,1,0,'2026-07-31 03:48:39','2026-07-31 03:48:46'),(7,'房主有神器','badget_owner_test',NULL,'badge',7,99999,-1,1,1,0,'2026-07-31 03:51:16','2026-08-17 14:08:37');
/*!40000 ALTER TABLE `shop_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shop_order`
--

DROP TABLE IF EXISTS `shop_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shop_order` (
  `order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `account_id` int NOT NULL COMMENT '买家账号ID',
  `item_id` int NOT NULL COMMENT '商品ID',
  `price` bigint NOT NULL COMMENT '成交单价快照',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '购买数量',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1=成功，2=已退款（预留）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`order_id`) USING BTREE,
  KEY `idx_shop_order_account_time` (`account_id`,`create_time`),
  KEY `idx_shop_order_item_time` (`item_id`,`create_time`),
  CONSTRAINT `shop_order_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `shop_order_ibfk_2` FOREIGN KEY (`item_id`) REFERENCES `shop_item` (`item_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商城购买记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shop_order`
--

LOCK TABLES `shop_order` WRITE;
/*!40000 ALTER TABLE `shop_order` DISABLE KEYS */;
INSERT INTO `shop_order` VALUES (6,1,6,1,1,1,'2026-07-31 03:48:46'),(7,1,7,99999,1,1,'2026-08-17 14:08:37');
/*!40000 ALTER TABLE `shop_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_message`
--

DROP TABLE IF EXISTS `system_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_message` (
  `system_message_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `account_id` int DEFAULT NULL,
  PRIMARY KEY (`system_message_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=172 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_message`
--

LOCK TABLES `system_message` WRITE;
/*!40000 ALTER TABLE `system_message` DISABLE KEYS */;
INSERT INTO `system_message` VALUES (1,'手办模玩|冬季温馨小提示 ','温馨提醒：购买手办模型的用户，请您在打开物流包装后，先透过内包装透明袋检查商品是否完好。若您发现有任何质量问题，请勿再打开内包装，即刻联络客服为您核实！pvc材质受冷容易变脆，组装时也请务必格外小心，如果难以插组，可用吹风机适当吹热后再组装~','2025-11-07 14:05:00',1),(2,'手办模玩|冬季温馨小提示 ','温馨提醒：购买手办模型的用户，请您在打开物流包装后，先透过内包装透明袋检查商品是否完好。若您发现有任何质量问题，请勿再打开内包装，即刻联络客服为您核实！pvc材质受冷容易变脆，组装时也请务必格外小心，如果难以插组，可用吹风机适当吹热后再组装~','2025-11-07 14:06:08',1),(148,'1234465aa','123','2025-11-12 19:36:34',1),(149,'1234465aa','123','2025-11-12 19:37:40',1),(150,'1234465aa','123','2025-11-12 19:37:40',1),(151,'1234465aaaa','123','2025-11-12 19:37:43',1),(152,'1234465aaaa','123','2025-11-12 19:38:08',1),(153,'1234465aaaa','123','2025-11-12 19:38:08',1),(154,'1234465aaaa','123','2025-11-12 19:38:08',1),(155,'1234465aaaa','123','2025-11-12 19:38:08',1),(156,'1234465aaaa','123','2025-11-12 19:38:12',1),(157,'1234465aaaa','123','2025-11-12 19:38:14',1),(158,'1234465aaaa','123','2025-11-12 19:38:14',1),(159,'1234465aaaa','123','2025-11-12 19:38:14',1),(160,'1234465aaaa','123','2025-11-12 19:38:16',1),(161,'1234465aaaa','123','2025-11-12 19:38:17',1),(162,'1234465aaaa','123','2025-11-12 19:38:17',1),(163,'123','22','2025-11-13 11:37:19',1),(164,'用户违规处理','您的用户名违反了社区规定，我们已经帮您清除了，请重新设置。\n','2025-11-27 15:08:19',3),(165,'用户违规处理','您的用户名违反了社区规定，我们已经帮您清除了，请重新设置。\n','2025-11-27 15:10:03',3),(166,'用户违规处理','您的用户名违反了社区规定，我们已经帮您清除了，请重新设置。\n','2025-11-27 15:19:03',3),(167,'用户违规处理','您的用户名违反了社区规定，我们已经帮您清除了，请重新设置。\n','2025-11-27 15:23:17',3),(168,'账号状态变更','您的账号已被封禁。处理备注：呵呵。','2026-05-04 21:33:10',2),(169,'举报处理结果','您提交的举报已处理：ayorね。','2026-05-04 21:33:10',1),(170,'你好','你好','2026-05-18 08:39:43',1),(171,'你好','你好','2026-05-18 09:04:14',1);
/*!40000 ALTER TABLE `system_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tag`
--

DROP TABLE IF EXISTS `tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tag` (
  `tag_id` int NOT NULL AUTO_INCREMENT COMMENT '标签id',
  `tag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '标签内容',
  `create_time` datetime DEFAULT NULL,
  `topic_id` int DEFAULT NULL,
  PRIMARY KEY (`tag_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tag`
--

LOCK TABLES `tag` WRITE;
/*!40000 ALTER TABLE `tag` DISABLE KEYS */;
INSERT INTO `tag` VALUES (1,'闲聊','2025-10-13 08:18:33',1),(2,'帮助','2025-10-13 15:13:52',1),(3,'测试','2025-10-14 14:58:23',2),(4,'学习','2025-10-15 20:51:28',1);
/*!40000 ALTER TABLE `tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `theme`
--

DROP TABLE IF EXISTS `theme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `theme` (
  `theme_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`theme_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `theme`
--

LOCK TABLES `theme` WRITE;
/*!40000 ALTER TABLE `theme` DISABLE KEYS */;
INSERT INTO `theme` VALUES (1,'综合',0),(3,'资源分享',0);
/*!40000 ALTER TABLE `theme` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thread`
--

DROP TABLE IF EXISTS `thread`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `thread` (
  `thread_id` int NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '帖子标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '内容',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `view_count` int DEFAULT '0' COMMENT '观看量',
  `post_count` int DEFAULT '0' COMMENT '回复量',
  `like_count` int DEFAULT '0' COMMENT '点赞量',
  `collect_count` int DEFAULT '0' COMMENT '收藏数量\r\n',
  `topic_id` int DEFAULT NULL COMMENT '主题ID',
  `tag_id` int DEFAULT NULL COMMENT '标签ID',
  `account_id` int DEFAULT NULL COMMENT '帖子作者ID',
  `is_muted` tinyint DEFAULT '0' COMMENT '帖子是否禁止发言',
  `is_selected` tinyint DEFAULT '0' COMMENT '帖子是否加精',
  `is_deleted` tinyint DEFAULT '0' COMMENT '帖子是否被删除',
  PRIMARY KEY (`thread_id`) USING BTREE,
  KEY `account_id` (`account_id`) USING BTREE,
  KEY `tag_id` (`tag_id`) USING BTREE,
  KEY `topic_id` (`topic_id`) USING BTREE,
  CONSTRAINT `thread_ibfk_1` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`tag_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `thread_ibfk_2` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `thread_ibfk_3` FOREIGN KEY (`topic_id`) REFERENCES `topic` (`topic_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=94 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thread`
--

LOCK TABLES `thread` WRITE;
/*!40000 ALTER TABLE `thread` DISABLE KEYS */;
INSERT INTO `thread` VALUES (82,'大家好啊','{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"大家好啊\"}]},{\"type\":\"paragraph\"}]}','2026-04-27 11:03:40',NULL,14,3,0,0,1,NULL,1,0,0,0),(83,'Saki Saki Saki','{\"type\":\"doc\",\"content\":[{\"type\":\"image\",\"attrs\":{\"src\":\"nineforum/threads/1/3c474623a0ab44bf85809d98f31771b6.png\"}},{\"type\":\"paragraph\"}]}','2026-04-27 11:17:16',NULL,11,7,0,0,1,NULL,1,0,0,0),(84,'测试','{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"测试\"}]}]}','2026-05-06 13:10:48',NULL,6,4,0,0,1,1,2,0,0,0),(85,'测试啊','{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"测试啊\"}]}]}','2026-05-06 13:51:19',NULL,57,26,0,0,1,1,1,0,0,0),(86,'今天天气不错','{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"我就要玩\"}]}]}','2026-05-21 08:54:36','2026-06-13 17:45:33',45,4,2,0,1,2,1,0,0,0),(87,'测试','{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"123\"}]}]}','2026-06-24 17:32:26',NULL,6,1,0,0,1,NULL,2,0,0,0),(88,'实时通讯真不错','{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"实时通讯真不错\"}]}]}','2026-06-24 17:37:01',NULL,0,0,0,0,1,NULL,1,0,0,0),(89,'123','{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"123\"}]}]}','2026-06-24 17:38:42',NULL,2,1,0,0,1,NULL,1,0,0,0),(90,'123','{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"1231\"}]}]}','2026-06-24 17:41:02',NULL,0,0,0,0,1,NULL,1,0,0,0),(91,'晓美焰最可爱','{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"晓美焰最可爱\"}]}]}','2026-06-24 17:41:20',NULL,6,3,0,0,1,NULL,1,0,0,0),(92,'哇哦哦哦','{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"哇哦哦哦\"}]}]}','2026-06-24 17:48:06',NULL,16,3,0,0,1,2,1,0,0,0),(93,'测试一下标签','{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"测试一下标签\"}]}]}','2026-07-20 10:01:48',NULL,2,0,0,0,1,2,2,0,0,0);
/*!40000 ALTER TABLE `thread` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thread_edit_history`
--

DROP TABLE IF EXISTS `thread_edit_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `thread_edit_history` (
  `history_id` int NOT NULL AUTO_INCREMENT COMMENT '编辑历史ID',
  `thread_id` int NOT NULL COMMENT '帖子ID',
  `editor_account_id` int NOT NULL COMMENT '编辑者账号ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '编辑前标题快照',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '编辑前内容快照(TipTap JSON)',
  `edit_time` datetime NOT NULL COMMENT '本次编辑发生时间',
  PRIMARY KEY (`history_id`) USING BTREE,
  KEY `idx_thread_id` (`thread_id`) USING BTREE,
  KEY `idx_editor_account_id` (`editor_account_id`) USING BTREE,
  CONSTRAINT `db_thread_edit_history_ibfk_1` FOREIGN KEY (`thread_id`) REFERENCES `thread` (`thread_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `db_thread_edit_history_ibfk_2` FOREIGN KEY (`editor_account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thread_edit_history`
--

LOCK TABLES `thread_edit_history` WRITE;
/*!40000 ALTER TABLE `thread_edit_history` DISABLE KEYS */;
INSERT INTO `thread_edit_history` VALUES (1,86,1,'今天天气不错','{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"大家想去哪里玩啊\"}]}]}','2026-06-13 17:45:08'),(2,86,1,'今天天气不错','{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"别玩了\"}]}]}','2026-06-13 17:45:33');
/*!40000 ALTER TABLE `thread_edit_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `topic`
--

DROP TABLE IF EXISTS `topic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `topic` (
  `topic_id` int NOT NULL AUTO_INCREMENT COMMENT '话题ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标题',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '话题封面',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `theme_id` int DEFAULT NULL COMMENT '主题ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`topic_id`) USING BTREE,
  KEY `theme_id` (`theme_id`) USING BTREE,
  CONSTRAINT `topic_ibfk_1` FOREIGN KEY (`theme_id`) REFERENCES `theme` (`theme_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `topic`
--

LOCK TABLES `topic` WRITE;
/*!40000 ALTER TABLE `topic` DISABLE KEYS */;
INSERT INTO `topic` VALUES (1,'综合讨论','nineforum/topic/43aab9ef88c348c9818aa3efeb8ecd06.png','大家一起来聊天','2025-10-11 14:16:13',1,0),(14,'技术资源分享','nineforum/topic/647a3af8ead84d86897e3a2a808f0605.png',NULL,'2025-10-15 21:06:43',3,0),(17,'test','nineforum/topic/6c6d770497eb4570804137f5c54f37d0.jpg',NULL,'2025-10-24 09:10:48',3,1),(18,'adsa','nineforum/topic/31d86d0a103f443f987556dab813c435.png',NULL,'2025-11-09 15:12:39',3,1),(19,'ass','nineforum/topic/bdaa3a74467a4c68b3655bde785b9362.png',NULL,'2025-11-09 15:15:31',3,1),(20,'asd','nineforum/topic/3b9270efd44e44e0a9b9e849d87e44b5.png',NULL,'2025-11-09 15:17:08',3,1),(21,'asda','nineforum/topic/7480c34b4d8f41d9bb1be385768d025d.jpg',NULL,'2025-11-09 15:22:16',3,1),(22,'测试','nineforum/topic/81adb0e4bfd04a60b378c435037ac4e8.png','测试','2026-05-01 19:42:26',1,1),(23,'测试 Plus','nineforum/topic/398e60bd11a4408d9b163509495055a1.png','测试 Plus','2026-05-04 16:54:45',1,0);
/*!40000 ALTER TABLE `topic` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `topic_chat`
--

DROP TABLE IF EXISTS `topic_chat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `topic_chat` (
  `topic_chat_id` int NOT NULL,
  `topic_id` int DEFAULT NULL,
  `account_id` int DEFAULT NULL,
  `content` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`topic_chat_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `topic_chat`
--

LOCK TABLES `topic_chat` WRITE;
/*!40000 ALTER TABLE `topic_chat` DISABLE KEYS */;
/*!40000 ALTER TABLE `topic_chat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `topic_stat`
--

DROP TABLE IF EXISTS `topic_stat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `topic_stat` (
  `topic_stat_id` int NOT NULL AUTO_INCREMENT COMMENT '话题统计ID',
  `topic_id` int NOT NULL COMMENT '话题ID',
  `thread_count` int NOT NULL DEFAULT '0' COMMENT '统计有多少帖子',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '统计观看量',
  PRIMARY KEY (`topic_stat_id`) USING BTREE,
  KEY `topic_id` (`topic_id`) USING BTREE,
  CONSTRAINT `topic_stat_ibfk_1` FOREIGN KEY (`topic_id`) REFERENCES `topic` (`topic_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `topic_stat`
--

LOCK TABLES `topic_stat` WRITE;
/*!40000 ALTER TABLE `topic_stat` DISABLE KEYS */;
INSERT INTO `topic_stat` VALUES (1,1,12,165),(2,14,0,0),(3,17,0,0),(4,18,0,0),(5,19,0,0),(6,20,0,0),(7,21,0,0),(8,22,0,0),(9,23,0,0);
/*!40000 ALTER TABLE `topic_stat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_item`
--

DROP TABLE IF EXISTS `user_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_item` (
  `user_item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '背包记录ID',
  `account_id` int NOT NULL COMMENT '账号ID',
  `item_id` int NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '持有数量（装饰类恒为1，预留堆叠道具）',
  `is_equipped` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已装备',
  `acquire_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
  PRIMARY KEY (`user_item_id`) USING BTREE,
  UNIQUE KEY `uk_user_item` (`account_id`,`item_id`),
  KEY `idx_user_item_equipped` (`account_id`,`is_equipped`),
  KEY `user_item_ibfk_2` (`item_id`),
  CONSTRAINT `user_item_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `user_item_ibfk_2` FOREIGN KEY (`item_id`) REFERENCES `shop_item` (`item_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户背包表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_item`
--

LOCK TABLES `user_item` WRITE;
/*!40000 ALTER TABLE `user_item` DISABLE KEYS */;
INSERT INTO `user_item` VALUES (6,1,6,1,1,'2026-07-31 03:48:46'),(7,1,7,1,1,'2026-08-17 14:08:37');
/*!40000 ALTER TABLE `user_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_privacy_setting`
--

DROP TABLE IF EXISTS `user_privacy_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_privacy_setting` (
  `account_id` int NOT NULL,
  `profile_visibility` varchar(32) NOT NULL DEFAULT 'PUBLIC',
  `liked_threads_visibility` varchar(32) NOT NULL DEFAULT 'PUBLIC',
  `collected_threads_visibility` varchar(32) NOT NULL DEFAULT 'PRIVATE',
  `follow_list_visibility` varchar(32) NOT NULL DEFAULT 'PUBLIC',
  `follower_list_visibility` varchar(32) NOT NULL DEFAULT 'PUBLIC',
  `birthday_visibility` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PRIVATE',
  `dm_permission` varchar(32) NOT NULL DEFAULT 'EVERYONE',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`account_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_privacy_setting`
--

LOCK TABLES `user_privacy_setting` WRITE;
/*!40000 ALTER TABLE `user_privacy_setting` DISABLE KEYS */;
INSERT INTO `user_privacy_setting` VALUES (1,'PUBLIC','PUBLIC','PUBLIC','PUBLIC','PUBLIC','PRIVATE','EVERYONE','2026-04-28 19:28:44','2026-06-16 14:55:04'),(2,'PUBLIC','PUBLIC','PUBLIC','PUBLIC','PUBLIC','PRIVATE','EVERYONE','2026-04-28 11:59:13','2026-06-16 16:41:40'),(3,'PUBLIC','PUBLIC','PRIVATE','PUBLIC','PUBLIC','PRIVATE','EVERYONE','2026-05-04 09:22:31','2026-05-04 09:22:31'),(8,'PUBLIC','PUBLIC','PRIVATE','PUBLIC','PUBLIC','PRIVATE','EVERYONE','2026-05-04 09:22:31','2026-05-04 09:22:31'),(9,'PUBLIC','PUBLIC','PRIVATE','PUBLIC','PUBLIC','PRIVATE','EVERYONE','2026-04-29 13:53:16','2026-04-30 00:25:27');
/*!40000 ALTER TABLE `user_privacy_setting` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_relation`
--

DROP TABLE IF EXISTS `user_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_relation` (
  `relation_id` bigint NOT NULL AUTO_INCREMENT,
  `from_account_id` int NOT NULL,
  `to_account_id` int NOT NULL,
  `relation_type` varchar(32) NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`relation_id`) USING BTREE,
  UNIQUE KEY `uk_user_relation_pair_type` (`from_account_id`,`to_account_id`,`relation_type`),
  KEY `idx_user_relation_from_type_status` (`from_account_id`,`relation_type`,`status`),
  KEY `idx_user_relation_to_type_status` (`to_account_id`,`relation_type`,`status`),
  CONSTRAINT `chk_user_relation_no_self` CHECK ((`from_account_id` <> `to_account_id`))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_relation`
--

LOCK TABLES `user_relation` WRITE;
/*!40000 ALTER TABLE `user_relation` DISABLE KEYS */;
INSERT INTO `user_relation` VALUES (1,2,1,'FOLLOW','ACTIVE','2026-04-28 20:00:56','2026-07-19 22:14:50'),(2,1,2,'FOLLOW','ACTIVE','2026-05-28 11:03:19','2026-06-26 10:45:27'),(3,1,2,'BLOCK','INACTIVE','2026-05-28 11:11:58','2026-06-16 18:22:19');
/*!40000 ALTER TABLE `user_relation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'nine_forum'
--

--
-- Dumping routines for database 'nine_forum'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-20  2:58:28
