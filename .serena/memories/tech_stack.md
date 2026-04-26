# 技术栈

## 核心框架
- **Spring Boot**: 3.5.5
- **Java**: 17
- **Maven**: 项目构建和依赖管理

## Web 层
- **Spring MVC**: RESTful API 开发
- **Spring Security**: 安全认证和授权
- **Spring WebSocket**: 实时通信 (STOMP 协议)

## 数据层
- **MySQL**: 关系型数据库 (端口: 16033)
- **MyBatis-Plus**: 3.5.14 - ORM 框架
- **Redis**: 缓存和会话管理 (端口: 16379)
- **Elasticsearch**: 搜索引擎 (端口: 9200)

## 消息队列
- **RabbitMQ**: 异步任务处理 (端口: 5672, virtual-host: /nine_forum)
  - 用于邮件发送等异步操作
  - 手动确认模式

## 文件存储
- **MinIO**: 分布式对象存储 (端口: 9000)
  - Bucket: nineforum
  - 用于用户头像、横幅等文件上传

## 认证与安全
- **JWT**: 4.3.0 - JSON Web Token 认证
  - 过期时间: 7天
  - Secret Key: kenkouniyokunai (生产环境需更换)
- **Spring Security**: 基于角色的访问控制

## 邮件服务
- **Spring Mail**: 163邮箱 SMTP 发送

## 工具库
- **Lombok**: 1.18.38 - 减少样板代码
- **Fastjson2**: 2.0.54 - JSON 处理
- **Knife4j**: 4.5.0 - API 文档 (可能用于 Swagger)

## 开发工具
- **Maven Wrapper**: mvnw / mvnw.cmd
- **Git**: 版本控制
