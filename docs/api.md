# API 说明

本文档维护接口调用约定和控制器分组索引，不维护完整离线端点表。具体参数、响应模型和注解说明以运行时 OpenAPI / Knife4j 为准。

## 服务入口

| 应用 | 默认端口 | 基础地址 |
| --- | --- | --- |
| 用户端 `web-app` | `9966` | `http://localhost:9966` |
| 管理端 `web-admin` | `9977` | `http://localhost:9977` |

用户端配置了 OpenAPI 分组，常用入口：

- Knife4j：`/doc.html`
- Swagger UI：`/swagger-ui.html`
- OpenAPI JSON：`/v3/api-docs`

管理端引入了 SpringDoc / Knife4j 依赖，接口明细可在应用启动后以实际暴露结果为准。

## 统一响应结构

HTTP 接口统一返回 `Result<T>`：

```json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
```

常见业务码来自 `common/src/main/java/com/ayor/result/ResultCodeEnum.java`：

| code | 含义 |
| --- | --- |
| `200` | 成功，也用于退出成功。 |
| `201` | 通用失败。 |
| `202` | 参数不正确。 |
| `203` | 服务异常。 |
| `204` | 数据异常。 |
| `205` | 非法请求。 |
| `400` | 参数校验失败或退出失败。 |
| `401` | 未认证。 |
| `402` | 数据不存在。 |
| `403` | 权限不足。 |
| `601` | token 过期。 |
| `602` | token 非法。 |

注意：部分认证失败响应会以 HTTP 200 返回，但业务 `code` 为 `401`。调用方应以响应体业务码作为主要判断依据。

## 认证约定

- 登录接口签发 JWT，后续请求通过 `Authorization` Header 携带。
- 用户端登录路径为 `/api/auth/login`，退出路径为 `/api/auth/logout`。
- 管理端登录路径同样为 `/api/auth/login`，退出路径为 `/api/auth/logout`。
- 用户端还支持 Passkey/WebAuthn 登录，入口在 `/api/passkeys` 分组下。

## 用户端接口分组

| 分组 | 控制器范围 | 说明 |
| --- | --- | --- |
| 认证授权 | `AuthorizeController`、`PasskeyController` | 注册验证、登录、Passkey 注册和登录。 |
| 用户与关系 | `UserController`、`UserSearchController` | 当前用户、资料、隐私、关注、拉黑、用户搜索、会话管理。 |
| 主题与话题 | `ThemeController`、`TopicController`、`TagController`、`BreadController` | 主题、话题、标签、面包屑。 |
| 帖子与回复 | `ThreadController`、`PostController` | 帖子列表、详情、回复、编辑历史、公告、浏览、举报。 |
| 互动 | `LikeController`、`CollectController` | 点赞、取消点赞、收藏、取消收藏、用户点赞/收藏列表。 |
| 通知消息 | `NotificationController`、`SystemMessageController`、`MentionMessageController`、`FollowMessageController` | 未读统计、系统消息、提及消息、关注动态消息。 |
| 私信与聊天 | `ConversationController`、`ChatBoardController` | 私信会话、会话消息、聊天室历史和发送。 |
| 搜索 | `SearchController` | 帖子搜索、用户搜索、搜索历史、热词。 |
| 资源与反馈 | `StickerController`、`FeedbackController`、`PageBroadcastController` | 贴纸、意见反馈、页面广播。 |
| 权限操作 | `controller/permission/*` | 主题、话题、标签、帖子等权限相关管理动作。 |

### 私信接口约定

- `GET /api/conversations` 返回当前用户会话列表，排序为 `pinned desc, updateTime desc`。`ConversationVO` 包含 `conversationId`、`userInfo`、`updateTime`、`lastMessageId`、`lastMessageContent`、`lastMessageTime`、`lastMessageSenderId`、`pinned`、`partnerOnline`。
- `PUT /api/conversations/{conversation_id}/pin` 设置当前用户对会话的置顶状态，请求体为 `{ "pinned": true|false }`，成功返回当前用户视角的 `ConversationVO` 并推送会话列表项更新。
- `POST /api/conversations/{conversation_id}/messages` 发送纯文本私信，只使用请求体 `content`。服务端会 `trim` 内容，空内容或超过 1000 字符返回业务失败。
- `GET /api/conversations/{conversation_id}/messages?page_num=1` 每页返回 20 条消息。`ConversationMessageVO` 包含 `conversationId`、`isDeleted`、`deletedBySender`、`displayContent`；已撤回消息不会返回原始 `content`。
- `DELETE /api/conversations/{conversation_id}/messages/{message_id}/recall` 撤回当前用户在该会话内 2 分钟内发送的消息。撤回后消息记录保留，摘要显示为“消息已撤回”。
- 用户端撤回能力只使用上述受权限约束接口；后台管理端消息删除接口不作为普通用户撤回入口。

## 管理端接口分组

| 分组 | 控制器范围 | 说明 |
| --- | --- | --- |
| 仪表盘 | `DashboardController`、`AccountStatController`、`TopicStatController` | 概览、动态、账号统计、话题统计。 |
| 账号与权限 | `AccountController`、`RoleController`、`PermissionController`、`PermissionOperationLogController` | 账号、角色、权限、权限操作日志。 |
| 内容管理 | `ThemeController`、`TopicController`、`ThreadController`、`PostController`、`TagController` | 主题、话题、帖子、回复、标签管理。 |
| 运营治理 | `ReportController`、`FeedbackController`、`BroadcastController`、`PageBroadcastController` | 举报、反馈、用户广播、页面广播。 |
| 资源与互动 | `ImageAssetController`、`LikeController`、`CollectController`、`HistoryController` | 图片资源、点赞、收藏、历史。 |
| 会话与聊天室 | `ConversationController`、`ConversationMessageController`、`ChatboardHistoryController`、`TopicChatController` | 私信会话、消息、聊天室历史、话题聊天。 |
| 商城与装扮 | `ShopController`、`DecorationController` | 商品与购买记录管理、低代码装扮设计发布与素材上传。 |
| 数据修复 | `DataRepairController` | 缺失关联数据修复。 |

## 维护要求

- 新增 Controller 或调整路径后，更新本文件的分组索引。
- 新增统一错误码后，更新业务码表。
- 新增认证方式、Header 或响应包装方式后，更新本文件和 `auth-and-permission.md`。
