# NineForum 后端文档索引

本目录保存后端工程交接文档。根目录 `README.md` 负责项目简介、本地启动和常用命令；本目录文档负责补充架构、接口、配置、安全、数据库、消息、测试和运维细节。

## 阅读顺序

| 顺序 | 文档 | 用途 |
| --- | --- | --- |
| 1 | [architecture.md](architecture.md) | 了解模块边界、业务域和主要数据流。 |
| 2 | [configuration.md](configuration.md) | 了解本地配置项、外部依赖和敏感配置治理要求。 |
| 3 | [api.md](api.md) | 了解接口入口、统一响应、认证方式和接口分组。 |
| 4 | [auth-and-permission.md](auth-and-permission.md) | 了解认证、JWT、Passkey、公开接口和权限行为。 |
| 5 | [database.md](database.md) | 了解表分组、关键关系、初始化 SQL 和增量 SQL 管理。 |
| 6 | [messaging.md](messaging.md) | 了解 RabbitMQ、WebSocket/STOMP、通知链路和定时任务。 |
| 7 | [testing.md](testing.md) | 了解测试命令、已有覆盖范围和新增测试建议。 |
| 8 | [operations.md](operations.md) | 了解本地依赖、初始化、排障和生产部署注意事项。 |

## 目录约定

- `docs/*.md`：面向开发和交接的中文文档。
- `docs/sql/*.sql`：增量 SQL 片段，当前包含公告和意见反馈相关 SQL。
- `.docker/image/mysql/nine_forum_schema.sql`：本地完整初始化 schema 的主要来源。

## 维护原则

- 文档必须以当前代码、配置和 SQL 为准，不记录猜测性设计。
- API 明细以运行时 OpenAPI / Knife4j 为准，`docs/api.md` 只维护分组索引和调用约定。
- 配置文档只记录配置 key 和用途，不复制真实生产凭据或令牌。
- 修改控制器、安全过滤器、RabbitMQ 队列、STOMP 目的地、数据库表结构时，应同步更新对应文档。
