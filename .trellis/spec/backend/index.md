# NineForum 后端开发规范

本规范描述当前仓库的 Java 17 / Spring Boot 3.5.5 后端实现方式。根 `pom.xml` 聚合 `common`、`model` 和 `web`；其中 `web` 再分为可独立启动的用户端 `web-app` 与管理端 `web-admin`。

## 模块边界

| 模块 | 责任 | 代码依据 |
| --- | --- | --- |
| `common` | 可复用的结果封装、配置、JWT、图片和 MinIO 工具 | `common/src/main/java/com/ayor/result/Result.java`、`common/src/main/java/com/ayor/image/` |
| `model` | 两个 Web 应用共享的 POJO、DTO、VO、枚举、消息和 MyBatis 类型处理器 | `model/src/main/java/com/ayor/entity/`、`model/src/main/java/com/ayor/type/` |
| `web/web-app` | 用户端论坛 API、WebSocket、RabbitMQ、缓存与 Elasticsearch 流程 | `web/web-app/src/main/java/com/ayor/` |
| `web/web-admin` | 管理端 API、管理统计与后台任务 | `web/web-admin/src/main/java/com/ayor/` |

新增能力应先判断是否需要被两个 Web 应用复用：共享数据模型放入 `model`，通用基础设施放入 `common`，端点、业务编排、Mapper 和端侧配置留在对应 Web 应用。不要让 `common` 反向依赖 `web-*`，也不要让一个 Web 应用直接引用另一个 Web 应用的实现。

## 主题导航

| 规范 | 适用问题 |
| --- | --- |
| [目录结构](./directory-structure.md) | 功能代码、模型、配置与资源应放在哪里 |
| [数据库](./database-guidelines.md) | MyBatis-Plus、Mapper、SQL、事务与模式变更 |
| [错误处理](./error-handling.md) | `Result<T>`、业务失败、参数与安全异常的响应方式 |
| [日志](./logging-guidelines.md) | Lombok SLF4J 的已有日志方式与敏感信息边界 |
| [本地配置与秘密](./configuration-secrets.md) | 本地 `application.yml`、安全示例、Git 忽略与历史检查契约 |
| [质量](./quality-guidelines.md) | Maven 验证、单元/契约测试与评审重点 |
| [用户端 STOMP 安全](./websocket-security.md) | 用户端 WebSocket 握手、CONNECT、订阅与发送目的地授权 |
| [注册验证邮件安全](./registration-verification-security.md) | 公开注册邮件的幂等、Redis 配额、JWT TTL、429 与一次性消费合同 |
| [图片上传资源安全](./image-upload-security.md) | Base64、请求体、ImageIO 元数据、像素/帧数与并发处理边界 |
| [公开图片资产与表情包](./image-assets.md) | 图片/Sticker 的公开复用、`ACTIVE`/`DISABLED` 状态契约与 schema 同步 |
| [每日签到](./daily-check-in.md) | 签到 API、东京业务日期、Credit 发放与数据库唯一约束 |
| [帖子正文图片](./thread-content-images.md) | 帖子图片数量限制、列表投影与失败副作用边界 |
| [批量用户头像查询](./batch-user-avatars.md) | 用户 ID 列表、轻量头像映射、去重顺序与参数错误合同 |

## 基础验证

优先使用仓库内置 Wrapper，并从根目录验证受影响模块：

```powershell
.\mvnw.cmd test
.\mvnw.cmd -pl web/web-app -am test
.\mvnw.cmd -pl web/web-admin -am test
```

最后两条适用于改动只落在一个 Web 应用时；`-am` 会同时构建其依赖模块。
