# NineForum 后端

NineForum 是一个基于 Spring Boot 的论坛后端项目，提供面向用户端和管理端的 API 服务。项目采用多模块 Maven 结构，将通用能力、领域模型和 Web 应用拆分管理，便于独立维护和复用。

## 技术栈

- Java 17
- Spring Boot 3.5.5
- Maven Wrapper
- Spring Web / Validation / AOP / Security
- MyBatis-Plus
- MySQL
- Redis
- RabbitMQ
- MinIO
- Elasticsearch / Kibana
- WebSocket / STOMP
- WebAuthn4J
- Lombok

## 模块结构

```text
.
├── common            # 通用工具、配置、结果封装、JWT、MinIO、图片处理等
├── model             # 公共实体、DTO、VO、枚举和文档模型
├── web
│   ├── web-app       # 用户端论坛 API
│   └── web-admin     # 管理端 API
├── .docker           # 本地依赖服务的 Docker Compose 与初始化资源
├── pom.xml           # Maven 聚合工程
└── mvnw              # Maven Wrapper
```

## 主要能力

### 用户端 `web-app`

- 账号认证、JWT 授权、Passkey / WebAuthn
- 用户资料、用户搜索、关注关系与隐私设置
- 主题、话题、帖子、标签、收藏、点赞
- 站内通知、系统消息、提及消息、未读消息
- 私信会话、聊天室、WebSocket 推送
- 搜索、热词、内容图片、贴纸与公告

默认端口：`9966`

### 管理端 `web-admin`

- 仪表盘与统计数据
- 用户、角色、权限与权限操作日志
- 主题、话题、帖子、标签、收藏、点赞管理
- 举报处理、广播、页面公告、图片资源管理
- 会话消息、聊天室历史、数据修复任务

默认端口：`9977`

## 环境要求

- JDK 17
- Docker 与 Docker Compose
- Bash 或兼容 Shell

建议使用仓库内置的 Maven Wrapper，避免本机 Maven 版本差异。

## 本地依赖服务

启动 MySQL、Redis、MinIO、RabbitMQ、Elasticsearch 和 Kibana：

```bash
docker compose -f .docker/docker-compose.yaml up -d
```

默认端口如下：

| 服务 | 端口 | 说明 |
| --- | --- | --- |
| MySQL | `16033` | 数据库名 `nine_forum` |
| Redis | `16379` | 缓存与临时数据 |
| MinIO API | `9000` | 对象存储 API |
| MinIO Console | `9001` | 对象存储控制台 |
| RabbitMQ | `5672` | 消息队列 |
| Elasticsearch | `9200` / `9300` | 搜索服务 |
| Kibana | `5601` | Elasticsearch 可视化 |

MySQL 初始化 SQL 位于：

```text
.docker/image/mysql/nine_forum_schema.sql
```

当前 Compose 文件默认使用 `mysql:latest` 镜像；如果需要容器首次启动时自动导入初始化 SQL，需要改用 `.docker/image/mysql/Dockerfile` 构建镜像，或手动导入该 SQL。

## 配置说明

用户端配置：

```text
web/web-app/src/main/resources/application.yml
```

管理端配置：

```text
web/web-admin/src/main/resources/application.yml
```

这些配置包含数据库、Redis、MinIO、RabbitMQ、JWT、邮件、WebAuthn 和 Elasticsearch 等本地开发参数。提交前不要写入真实生产凭据；生产环境应通过外部配置、环境变量或部署平台注入敏感信息。

## 构建与测试

在仓库根目录执行：

```bash
./mvnw clean test
```

打包全部模块：

```bash
./mvnw clean package
```

仅构建用户端模块及其依赖：

```bash
./mvnw -pl web/web-app -am package
```

仅构建管理端模块及其依赖：

```bash
./mvnw -pl web/web-admin -am package
```

## 启动应用

启动用户端服务：

```bash
./mvnw -pl web/web-app -am spring-boot:run
```

启动管理端服务：

```bash
./mvnw -pl web/web-admin -am spring-boot:run
```

启动后访问：

- 用户端 API：`http://localhost:9966`
- 管理端 API：`http://localhost:9977`

## 常用开发命令

```bash
# 查看所有模块是否能通过测试
./mvnw clean test

# 启动本地依赖
docker compose -f .docker/docker-compose.yaml up -d

# 停止本地依赖
docker compose -f .docker/docker-compose.yaml down

# 只运行 web-app 测试
./mvnw -pl web/web-app -am test

# 只运行 web-admin 测试
./mvnw -pl web/web-admin -am test
```
