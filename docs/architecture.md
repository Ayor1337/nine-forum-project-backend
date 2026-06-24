# 架构说明

NineForum 后端是 Java 17、Maven 多模块 Spring Boot 项目，提供用户端论坛 API 和管理端 API。项目通过 `common`、`model`、`web/web-app`、`web/web-admin` 划分公共能力、领域模型和两个运行应用。

## 模块边界

| 模块 | 职责 |
| --- | --- |
| `common` | 通用结果封装、JWT 工具、MinIO 配置与服务、图片处理、文本工具、安全工具等可复用能力。 |
| `model` | 公共实体、DTO、VO、枚举、消息对象、类型处理器和部分资源配置。 |
| `web/web-app` | 面向用户端的论坛 API，覆盖认证、用户资料、主题话题、帖子回复、点赞收藏、私信、通知、搜索、贴纸、页面广播等。 |
| `web/web-admin` | 面向管理端的 API，覆盖仪表盘、账号、角色权限、话题内容管理、举报处理、广播、反馈、图片资源、数据修复等。 |

依赖方向以根 `pom.xml` 和 `web/pom.xml` 为准：`web` 依赖 `common` 和 `model`，两个 Web 应用共享公共模型和工具，但各自维护 Controller、Service、Mapper、配置和测试。

## 运行时组件

- `web-app` 默认监听 `9966`，入口类为 `com.ayor.WebAppApplication`。
- `web-admin` 默认监听 `9977`，入口类为 `com.ayor.WebAdminApplication`。
- MySQL 保存账号、内容、关系、统计、权限、举报、反馈、图片资源等业务数据。
- Redis 用于缓存、JWT 失效信息、Passkey 临时 challenge、未读消息等临时状态。
- RabbitMQ 承担邮件、举报、系统广播、页面广播等异步消息。
- MinIO 保存头像、横幅、内容图片、贴纸等对象资源。
- Elasticsearch 保存帖子搜索文档和搜索日志，Kibana 用于本地检索调试。
- WebSocket/STOMP 提供聊天室、系统通知、私信未读、页面广播和管理端举报推送。

## 核心业务域

- 账号与认证：注册、登录、JWT、登录会话、Passkey/WebAuthn、密码修改。
- 用户关系：资料、隐私设置、关注、拉黑、用户搜索。
- 论坛内容：主题、话题、标签、帖子、回复、编辑历史、公告、浏览统计。
- 互动与通知：点赞、收藏、提及、回复消息、系统消息、未读计数、私信。
- 内容治理：举报、封禁/违规广播、权限操作日志、管理端审核。
- 资源与搜索：图片资源、贴纸、内容图片引用、帖子搜索、热词和搜索历史。
- 后台运营：仪表盘、活动统计、页面广播、反馈处理、数据修复。

## 主要数据流

### 普通 HTTP 查询

Controller 接收请求后调用 Service，Service 通过 Mapper 访问 MySQL 或通过 Repository 访问 Elasticsearch，最终统一返回 `Result<T>`。分页数据使用 `PageEntity<T>` 包装。

### 发帖、回复与互动

用户端写操作经过 JWT 认证和业务校验后写入 MySQL。部分服务会触发统计更新、未读消息、提及通知或 WebSocket 推送；搜索相关能力会同步或定时维护 Elasticsearch 数据。

### 注册邮件验证

用户端注册验证接口把邮件消息发送到 `mail.direct` 交换机，routing key 为 `mail`，由 `mail.queue` 的监听器处理实际邮件发送。验证完成后会通过 STOMP `/verify/{jwtId}` 推送验证结果。

### 举报流转

用户端创建举报后发送 `report.direct` / `report.created` 消息。管理端监听 `report.queue`，并通过管理端 STOMP `/topic/reports` 推送新举报信息。

### 广播和页面广播

管理端用户广播通过 `broadcast.direct` / `broadcast` 发送到 `broadcast.queue`，用户端监听后转为系统通知。页面广播通过 `page-broadcast.direct` / `page-broadcast.changed` 发送到 `page-broadcast.queue`，用户端监听后推送到对应页面广播目的地。

## 设计约束

- Controller 只组织请求和响应，业务规则应留在 Service。
- `common` 和 `model` 应保持通用，不反向依赖具体 Web 模块。
- 新增异步能力时，需要同步维护 RabbitMQ 配置、监听器、消息对象和 `docs/messaging.md`。
- 新增表结构时，优先更新完整 schema，并把增量 SQL 放入 `docs/sql`。
