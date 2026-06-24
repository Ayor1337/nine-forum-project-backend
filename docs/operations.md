# 运维与排障说明

本文档记录本地依赖、初始化、检查项和生产部署注意事项。当前项目没有独立部署脚本，运行方式以 Maven Wrapper 和 `.docker/docker-compose.yaml` 为准。

## 本地依赖启动

在仓库根目录启动依赖服务：

```bash
docker compose -f .docker/docker-compose.yaml up -d
```

停止依赖服务：

```bash
docker compose -f .docker/docker-compose.yaml down
```

Compose 包含：

| 服务 | 本地端口 | 说明 |
| --- | --- | --- |
| MySQL | `16033` | 主业务库，库名 `nine_forum`。 |
| Redis | `16379` | 缓存和临时状态。 |
| MinIO API | `9000` | 对象存储 API。 |
| MinIO Console | `9001` | 对象存储控制台。 |
| RabbitMQ | `5672` | AMQP 服务。 |
| Elasticsearch | `9200` / `9300` | 搜索服务。 |
| Kibana | `5601` | Elasticsearch 可视化。 |

## 数据库初始化

完整 schema 位于：

```text
.docker/image/mysql/nine_forum_schema.sql
```

当前 Compose 默认使用 `mysql:latest`，不会自动通过该 Dockerfile 导入 schema。首次准备环境时可选择：

- 手动导入 `.docker/image/mysql/nine_forum_schema.sql`。
- 修改 Compose 使用 `.docker/image/mysql/Dockerfile` 构建镜像。
- 对已有环境执行 `docs/sql` 中的增量 SQL。

## 启动应用

用户端：

```bash
./mvnw -pl web/web-app -am spring-boot:run
```

管理端：

```bash
./mvnw -pl web/web-admin -am spring-boot:run
```

Windows PowerShell 使用 `.\mvnw.cmd`。

## 服务检查

| 组件 | 检查项 |
| --- | --- |
| MySQL | 端口 `16033` 可连接，库 `nine_forum` 存在，schema 已导入。 |
| Redis | 端口 `16379` 可连接，应用配置的 database 可用。 |
| MinIO | API `9000` 可访问，Console `9001` 可登录，bucket `nineforum` 存在。 |
| RabbitMQ | 端口 `5672` 可连接，virtual host `/nine_forum` 存在，队列和交换机创建成功。 |
| Elasticsearch | `http://localhost:9200` 可访问，IK Analyzer 插件安装成功。 |
| WebSocket | 用户端 `/chatboard`、`/chat`、`/system` 可连接；管理端 `/reports` 可连接。 |

## 常见问题

### 应用启动后数据库错误

- 确认 MySQL 容器健康。
- 确认 `.docker/image/mysql/nine_forum_schema.sql` 已导入。
- 确认 `spring.datasource.url`、用户名和密码使用当前环境值。

### RabbitMQ 连接失败

- 确认 RabbitMQ 容器启动。
- 确认 virtual host `/nine_forum` 已创建。
- 确认应用配置中的用户名、密码和端口匹配当前环境。

### MinIO 上传失败

- 确认 MinIO API 地址可访问。
- 确认 bucket `nineforum` 存在。
- 确认 access key 和 secret key 通过安全方式注入且有效。

### 搜索不可用

- 确认 Elasticsearch `9200` 可访问。
- 确认 Compose 安装 IK Analyzer 没有失败。
- 检查索引初始化或重建逻辑，重点查看 `ESIndexService` 和相关定时/服务调用。

### 实时通知不可用

- 确认 STOMP 端点可连接。
- 确认 JWT 或握手认证参数符合拦截器要求。
- 检查 RabbitMQ 队列是否有堆积，确认监听器正常消费。

## 数据修复与索引维护

- 管理端提供 `DataRepairController`，用于缺失关联数据修复。
- 用户端存在 `ESIndexService`，用于 Elasticsearch 索引相关维护。
- 执行修复前应备份数据库，并在测试环境验证影响范围。

## 生产部署注意事项

- 不要复用本地默认密码、JWT 密钥、SMTP 授权码、MinIO 密钥或 RabbitMQ 凭据。
- 使用外部配置管理敏感 key，并按环境区分 MySQL、Redis、RabbitMQ、MinIO、Elasticsearch 和邮件配置。
- 管理端当前安全配置在授权阶段 `permitAll()`，生产前必须明确访问控制策略。
- 对 RabbitMQ 消费、定时任务、搜索索引、MinIO 上传和登录失败率建立日志和监控。
- 数据库变更应同时维护完整 schema 和增量 SQL，并保留回滚方案。
