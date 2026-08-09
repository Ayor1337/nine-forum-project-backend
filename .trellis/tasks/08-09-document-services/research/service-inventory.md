# Service 调查清单

## 调查口径

- 调查日期：2026-08-09。
- 只统计 `src/main/java` 中的生产代码；测试类用于验证职责，但不计入服务总数。
- `com.ayor.service` 下的接口/抽象与对应 `impl` 合并为一个逻辑 service。
- 用户端和管理端的同名 service 分别统计。
- `common` 中明确以 `Service` 命名的 Spring Bean 纳入。
- service 包外标注 `org.springframework.stereotype.Service` 的类纳入辅助 Spring 服务。

## 数量结论

| 模块 | 常规逻辑 service | 包外 `@Service` | 合计 |
| --- | ---: | ---: | ---: |
| `web-app` | 44 | 2 | 46 |
| `web-admin` | 29 | 1 | 30 |
| `common` | 2 | 0 | 2 |
| 总计 | 75 | 3 | 78 |

## `web-app` 常规 service（44）

`AccountService`、`AccountStatService`、`AuthorizationService`、`AuthorizeService`、`BroadcastService`、`CacheInvalidationService`、`ChatboardHistoryService`、`ChatUnreadService`、`CollectService`、`ConversationMessageService`、`ConversationService`、`CreditService`、`ESIndexService`、`FeedbackService`、`FollowMessageService`、`ForumRealtimeService`、`ImageAssetService`、`LikeThreadService`、`MentionMessageService`、`MessageUnreadService`、`PageBroadcastQueryService`、`PasskeyRequestStore`、`PasskeyService`、`PasskeyWebAuthnAdapter`、`PermissionService`、`PostService`、`PresenceService`、`PrivacyPolicyService`、`ReportService`、`RoleService`、`SearchService`、`ShopService`、`SystemMessageService`、`TagService`、`ThemeService`、`ThreaddService`、`TopicChatService`、`TopicService`、`TopicStatService`、`UserLoginSessionService`、`UserPrivacySettingService`、`UserProfileService`、`UserRelationService`、`UserSearchService`。

证据路径：

- `web/web-app/src/main/java/com/ayor/service/*.java`
- `web/web-app/src/main/java/com/ayor/service/impl/*.java`

## `web-admin` 常规 service（29）

`AccountService`、`AccountStatService`、`ChatboardHistoryService`、`CollectService`、`ConversationMessageService`、`ConversationService`、`CreditService`、`DashboardService`、`DashboardStatisticsService`、`DataRepairService`、`DecorationService`、`FeedbackService`、`HistoryService`、`ImageAssetService`、`LikeService`、`PageBroadcastService`、`PermissionOperationLogService`、`PermissionService`、`PostService`、`ReportService`、`RoleService`、`ShopService`、`TagService`、`ThemeService`、`ThreaddService`、`TopicChatService`、`TopicService`、`TopicStatService`、`UserBroadcastService`。

证据路径：

- `web/web-admin/src/main/java/com/ayor/service/*.java`
- `web/web-admin/src/main/java/com/ayor/service/impl/*.java`

## `common` 服务（2）

- `ImageStorageService`：编排图片处理与 MinIO 写入，分别支持正文图和贴纸处理模式。
- `MinioService`：封装桶创建、对象上传/读取/删除、平台对象 URL 规范化。

证据路径：

- `common/src/main/java/com/ayor/image/ImageStorageService.java`
- `common/src/main/java/com/ayor/minio/MinioService.java`

## 包外 Spring 服务（3）

- `web-app:EsIndexSyncProducer`：在事务提交后发送主题/帖子 ES 索引同步消息。
- `web-admin:EsIndexSyncProducer`：除主题/帖子同步外，还支持全量重建消息。
- `web-app:ESIndexManager`：按资源 JSON 创建 Elasticsearch 索引并检查 mapping 缺失字段。

证据路径：

- `web/web-app/src/main/java/com/ayor/mq/EsIndexSyncProducer.java`
- `web/web-admin/src/main/java/com/ayor/mq/EsIndexSyncProducer.java`
- `web/web-app/src/main/java/com/ayor/search/ESIndexManager.java`

## 结构与依赖观察

- 多数持久化 service 继承 MyBatis-Plus `ServiceImpl<Mapper, Entity>` 并实现对应接口。
- 用户端 service 更偏业务编排，常组合 Mapper、Redis、RabbitMQ、WebSocket、Elasticsearch 和其他 service。
- 管理端多数 service 提供 CRUD、分页、审核、权限管理或运维操作；`Dashboard*`、`DataRepairService` 等承担聚合与维护职责。
- Redis 主要承载未读数、在线状态、临时 Passkey 请求、页面广播与缓存失效；RabbitMQ 主要承载授权、举报/广播及 ES 索引同步；WebSocket 主要承载聊天和实时通知。
- `PermissionService`、`RoleService`、`TopicChatService` 等少数用户端接口没有自定义方法，只暴露继承的泛型服务能力；文档必须明确这一点。

## 文档事实来源优先级

1. service 接口公开方法和实现类业务分支；
2. 实现类依赖的 Mapper、Redis、MQ、WebSocket、外部存储和其他 service；
3. Controller、Listener/Consumer、定时任务等调用方；
4. 对应单元测试；
5. README 仅用于模块背景，不替代源码证据。
