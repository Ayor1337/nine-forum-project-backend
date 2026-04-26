# Web App RESTful API 文档

本文档描述 `web/web-app` 新版 RESTful 接口。旧版接口文档已保留在 `docs/LEGACY_API.md`。

## 基础约定

- 基础路径：`/api`
- 参数命名：Path 与 Query 参数保持 `snake_case`
- 请求体：除特别说明外，使用 `Content-Type: application/json`
- 鉴权：登录成功后使用响应中的 JWT，通常放入 `Authorization` 请求头
- 响应模型：继续使用现有 `Result<T>` 与 `PageEntity<T>`，不使用 HTTP status 表达业务状态

## 通用响应

除注册邮箱验证接口外，响应结构为：

```json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
```

分页结构 `PageEntity<T>`：

```json
{
  "totalSize": 100,
  "data": []
}
```

常见业务状态码：

| code | message | 说明 |
| --- | --- | --- |
| `200` | 成功 | 请求成功 |
| `201` | 失败 | 业务处理失败 |
| `203` | 请求参数验证有误 / 请求参数内容有误 | 参数校验或缺少参数 |
| `401` | 未认证 / 认证异常信息 | 未登录或 Token 无效 |
| `403` | 权限不足 | 无访问权限 |
| `400` | 退出失败 / 参数校验失败 | 登出或校验失败 |

## 鉴权与公开接口

公开认证接口：

- `POST /api/auth/register-verifications`
- `GET /api/auth/register-verifications`
- `POST /api/auth/registrations`
- `POST /api/auth/login`

公开资源接口：
- `GET /api/users/{user_id}`
- `GET /api/themes`
- `GET /api/themes/topics`
- `GET /api/themes/{theme_id}/topics`
- `GET /api/topics/{topic_id}/tags`
- `GET /api/topics/{topic_id}/threads`
- `GET /api/users/{user_id}/threads`
- `GET /api/threads/{thread_id}`
- `GET /api/topics/{topic_id}/announcements`
- `GET /api/threads/{thread_id}/posts`
- `GET /api/threads/{thread_id}/likes/count`
- `GET /api/users/{user_id}/liked-threads`
- `GET /api/threads/{thread_id}/collections/count`
- `GET /api/users/{user_id}/collected-threads`
- `GET /api/search/users`
- `GET /api/search/hot-keywords`
- `GET /api/topics/{topic_id}/chat-messages`
- `GET /api/topics/{topic_id}/breadcrumb`
- `GET /api/threads/{thread_id}/breadcrumb`

其余接口默认需要认证。带 `moderation` 或 `@PreAuthorize` 的接口还需要对应角色或权限。

## 认证

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| POST | `/api/auth/register-verifications` | 发送注册验证邮件 | 公开 |
| GET | `/api/auth/register-verifications` | 校验注册邮箱 Token | 公开 |
| POST | `/api/auth/registrations` | 注册账号 | 公开 |
| POST | `/api/auth/login` | 登录 | 公开 |
| DELETE | `/api/auth/logout` | 登出 | 登录 |

### POST `/api/auth/register-verifications`

请求体 `RegDTO`：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `email` | String | 是 | 邮箱格式 |

响应：`Result<String>`。

### GET `/api/auth/register-verifications`

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `email` | String | 是 |
| `token` | String | 是 |

响应：纯文本 `验证成功` 或 `验证失败`。

### POST `/api/auth/registrations`

请求体 `AccountDTO`：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `username` | String | 是 | 长度 4-16，仅字母、数字、下划线 |
| `password` | String | 是 | 长度 6-16，仅字母、数字、下划线 |
| `nickname` | String | 是 | 长度 3-20 |
| `token` | String | 是 | 注册验证 Token |

响应：`Result<Void>`。

### POST `/api/auth/login`

登录由 Spring Security `formLogin` 提供。

表单参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `username` | String | 是 |
| `password` | String | 是 |

响应：`Result<AuthorizeVO>`，字段包含 `username`、`role`、`token`、`expire`。

### DELETE `/api/auth/logout`

请求头：

| Header | 必填 | 说明 |
| --- | --- | --- |
| `Authorization` | 是 | 当前 JWT |

响应：`Result<Void>`。

## 用户与统计

| 方法 | 路径 | 说明 | 响应 |
| --- | --- | --- | --- |
| GET | `/api/users/me` | 获取当前用户信息 | `Result<UserInfoVO>` |
| GET | `/api/users/{user_id}` | 获取指定用户信息 | `Result<UserInfoVO>` |
| PUT | `/api/users/me/avatar` | 更新当前用户头像 | `Result<Void>` |
| PUT | `/api/users/me/banner` | 更新当前用户横幅图 | `Result<Void>` |
| PUT | `/api/users/me/profile` | 更新当前用户个人资料 | `Result<Void>` |
| POST | `/api/users/me/password` | 通过旧密码更新当前账号密码 | `Result<Void>` |
| GET | `/api/users/me/stats` | 获取当前用户统计 | `Result<AccountStatVO>` |

头像和横幅请求体 `Base64Upload`：

| 字段 | 类型 | 必填 |
| --- | --- | --- |
| `base64` | String | 是 |
| `fileName` | String | 是 |

个人资料请求体 `AccountProfileDTO`：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `nickname` | String | 否 | 长度 3-20 |
| `bio` | String | 否 | 长度 6-50 |
| `avatar` | `Base64Upload` | 否 | 传入时同步更新头像 |

密码修改请求体 `PasswordChangeDTO`：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `oldPassword` | String | 是 | 当前密码 |
| `newPassword` | String | 否 | 长度 6-16，仅字母、数字、下划线 |

## 主题、话题、标签

| 方法 | 路径 | 说明 | 鉴权 | 响应 |
| --- | --- | --- | --- | --- |
| GET | `/api/themes` | 获取主题列表 | 公开 | `Result<List<ThemeVO>>` |
| POST | `/api/themes` | 新增主题 | `ROLE_OWNER` | `Result<Void>` |
| GET | `/api/themes/topics` | 获取主题及话题树 | 公开 | `Result<List<ThemeTopicVO>>` |
| GET | `/api/themes/{theme_id}/topics` | 获取主题下话题 | 公开 | `Result<List<TopicVO>>` |
| POST | `/api/topics` | 新增话题 | `ROLE_OWNER` | `Result<Void>` |
| PUT | `/api/topics/{topic_id}` | 更新话题 | `ROLE_OWNER` | `Result<Void>` |
| DELETE | `/api/topics/{topic_id}` | 删除话题 | `ROLE_OWNER` | `Result<Void>` |
| GET | `/api/topics/{topic_id}/tags` | 获取话题标签 | 公开 | `Result<List<TagVO>>` |
| POST | `/api/topics/{topic_id}/tags` | 新增话题标签 | `ROLE_OWNER` 或 `PERM_INSERT_TAG + TOPIC_{topic_id}` | `Result<Void>` |

`ThemeDTO`：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `title` | String | 是 | 长度 1-10 |

`TopicDTO`：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `topicId` | Integer | 否 | 更新时由路径 `{topic_id}` 覆盖 |
| `title` | String | 是 | 长度 1-10 |
| `cover` | Base64Upload | 是 | 封面 |
| `description` | String | 否 | 最大 20 字符 |
| `themeId` | Integer | 是 | 所属主题 ID |

`TagDTO`：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `tag` | String | 是 | 标签名 |
| `topicId` | Integer | 否 | 由路径 `{topic_id}` 覆盖 |

## 帖子、公告、浏览、回复

| 方法 | 路径 | 说明 | 鉴权 | 响应 |
| --- | --- | --- | --- | --- |
| GET | `/api/topics/{topic_id}/threads` | 按话题分页获取帖子 | 公开 | `Result<PageEntity<ThreadVO>>` |
| GET | `/api/users/{user_id}/threads` | 按用户分页获取帖子 | 公开 | `Result<PageEntity<ThreadVO>>` |
| GET | `/api/threads/{thread_id}` | 获取帖子详情 | 公开 | `Result<ThreadVO>` |
| POST | `/api/threads` | 发布帖子 | 登录 | `Result<Void>` |
| DELETE | `/api/threads/{thread_id}` | 删除自己的帖子 | 登录 | `Result<Void>` |
| POST | `/api/threads/{thread_id}/views` | 增加浏览数 | 登录 | `Result<Void>` |
| GET | `/api/topics/{topic_id}/announcements` | 获取话题公告帖 | 公开 | `Result<List<AnnouncementVO>>` |
| PUT | `/api/topics/{topic_id}/announcements/{thread_id}` | 设置公告帖 | 版主权限 | `Result<Void>` |
| DELETE | `/api/topics/{topic_id}/announcements/{thread_id}` | 取消公告帖 | 版主权限 | `Result<Void>` |
| GET | `/api/threads/{thread_id}/posts` | 获取回复列表 | 公开 | `Result<List<PostVO>>` |
| POST | `/api/threads/{thread_id}/posts` | 发布回复 | 登录 | `Result<Void>` |
| DELETE | `/api/posts/{post_id}` | 删除自己的回复 | 登录 | `Result<Void>` |
| GET | `/api/posts/reply-messages` | 获取当前用户回复消息 | 登录 | `Result<PageEntity<ReplyMessageVO>>` |

分页 Query：

| 参数 | 类型 | 必填 | 默认值 |
| --- | --- | --- | --- |
| `page_num` | Integer | 是 | - |
| `page_size` | Integer | 否 | `10`，回复消息为 `7` |

`GET /api/users/{user_id}/threads` 使用 `page` 与 `page_size`。

`ThreadDTO`：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `title` | String | 是 | 长度 1-50 |
| `content` | String | 是 | - |
| `topicId` | Integer | 是 | 话题 ID |

`PostDTO`：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `content` | String | 是 | 回复内容 |
| `threadId` | Integer | 否 | 由路径 `{thread_id}` 覆盖 |

## 版主管理接口

| 方法 | 路径 | 说明 | 权限 | 请求 |
| --- | --- | --- | --- | --- |
| PUT | `/api/moderation/threads/{thread_id}/tag` | 更新帖子标签 | `ROLE_OWNER` 或 `PERM_UPDATE_TAG + TOPIC_{topic_id}` | Query `topic_id`，Body `TagUpdateDTO` |
| DELETE | `/api/moderation/threads/{thread_id}/tag` | 删除帖子标签 | `ROLE_OWNER` 或 `PERM_UPDATE_TAG + TOPIC_{topic_id}` | Query `topic_id` |
| DELETE | `/api/moderation/threads/{thread_id}` | 权限删除帖子 | `ROLE_OWNER` 或 `PERM_sDELETE_THREAD + TOPIC_{topic_id}` | Query `topic_id` |
| DELETE | `/api/moderation/posts/{post_id}` | 权限删除回复 | `ROLE_OWNER` | 无 |

`TagUpdateDTO`：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `tagId` | Integer | 是 | 标签 ID |
| `topicId` | Integer | 否 | 由 Query `topic_id` 覆盖 |
| `threadId` | Integer | 否 | 由路径 `{thread_id}` 覆盖 |

## 点赞与收藏

| 方法 | 路径 | 说明 | 鉴权 | 响应 |
| --- | --- | --- | --- | --- |
| POST | `/api/threads/{thread_id}/likes` | 点赞帖子 | 登录 | `Result<Void>` |
| DELETE | `/api/threads/{thread_id}/likes` | 取消点赞 | 登录 | `Result<Void>` |
| GET | `/api/threads/{thread_id}/likes/me` | 当前用户是否点赞 | 登录 | `Result<Boolean>` |
| GET | `/api/threads/{thread_id}/likes/count` | 帖子点赞数 | 公开 | `Result<Integer>` |
| GET | `/api/users/{user_id}/liked-threads` | 用户点赞过的帖子 | 公开 | `Result<PageEntity<ThreadVO>>` |
| POST | `/api/threads/{thread_id}/collections` | 收藏帖子 | 登录 | `Result<Void>` |
| DELETE | `/api/threads/{thread_id}/collections` | 取消收藏 | 登录 | `Result<Void>` |
| GET | `/api/threads/{thread_id}/collections/me` | 当前用户是否收藏 | 登录 | `Result<Boolean>` |
| GET | `/api/threads/{thread_id}/collections/count` | 帖子收藏数 | 公开 | `Result<Integer>` |
| GET | `/api/users/{user_id}/collected-threads` | 用户收藏的帖子 | 公开 | `Result<PageEntity<ThreadVO>>` |

列表 Query：`page`、`page_size`，均必填。

## 搜索

| 方法 | 路径 | 说明 | 鉴权 | 响应 |
| --- | --- | --- | --- | --- |
| GET | `/api/search/threads` | 搜索帖子 | 登录 | `Result<PageEntity<ThreadDoc>>` |
| GET | `/api/search/users` | 搜索用户，当前实现为 TODO | 公开 | `Result<Void>` |
| GET | `/api/search/history` | 获取当前用户搜索历史 | 登录 | `Result<Set<String>>` |
| GET | `/api/search/history/query` | 查询搜索历史，当前实现返回全部历史 | 登录 | `Result<Set<String>>` |
| DELETE | `/api/search/history` | 删除搜索历史 | 登录 | `Result<Void>` |
| GET | `/api/search/hot-keywords` | 获取热门关键词 | 公开 | `Result<List<HotKeywordVO>>` |

搜索帖子 Query：

| 参数 | 类型 | 必填 | 默认值 |
| --- | --- | --- | --- |
| `query` | String | 是 | - |
| `only_thread_topic` | boolean | 否 | `false` |
| `topic_id` | Integer | 否 | - |
| `enable_history` | boolean | 否 | `true` |
| `start_time` | Long | 否 | - |
| `end_time` | Long | 否 | - |
| `order` | String | 否 | `rel` |
| `page_num` | int | 否 | `1` |
| `page_size` | int | 否 | `10` |

热门关键词 Query：`size` 默认 `10`，`duration` 默认 `7`。

## 会话与消息

| 方法 | 路径 | 说明 | 响应 |
| --- | --- | --- | --- |
| GET | `/api/conversations` | 获取当前用户会话列表 | `Result<List<ConversationVO>>` |
| POST | `/api/conversations` | 按用户名创建会话 | `Result<Void>` |
| GET | `/api/conversations/with-user/{account_id}` | 按目标账号获取会话 | `Result<ConversationVO>` |
| DELETE | `/api/conversations/{conversation_id}` | 隐藏会话 | `Result<Void>` |
| GET | `/api/conversations/{conversation_id}/messages` | 获取会话消息 | `Result<PageEntity<ConversationMessageVO>>` |
| POST | `/api/conversations/{conversation_id}/messages` | 发送私信 | `Result<Void>` |
| GET | `/api/conversations/unread-messages` | 获取未读私信列表 | `Result<List<ChatUnread>>` |
| DELETE | `/api/conversations/{conversation_id}/unread-messages` | 清除会话未读数 | `Result<Void>` |

全部会话接口均需要登录。

创建会话 Query：`username` String，必填。

消息列表 Query：`page_num` Integer，必填。

清除未读 Query：`from_user_id` Integer，必填。

发送私信请求体 `ConversationMessageDTO`：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `conversationId` | Integer | 否 | 由路径 `{conversation_id}` 覆盖 |
| `content` | String | 是 | 消息内容 |
| `toUserId` | Integer | 是 | 接收者 ID |

## 聊天板、系统消息、通知、面包屑

| 方法 | 路径 | 说明 | 鉴权 | 响应 |
| --- | --- | --- | --- | --- |
| GET | `/api/topics/{topic_id}/chat-messages` | 获取话题聊天板历史 | 公开 | `Result<PageEntity<ChatboardHistoryVO>>` |
| POST | `/api/topics/{topic_id}/chat-messages` | 发送话题聊天板消息 | 登录 | `Result<Void>` |
| GET | `/api/system-messages` | 获取当前用户系统消息 | 登录 | `Result<PageEntity<SystemMessageVO>>` |
| GET | `/api/notifications/unread-count` | 获取当前用户未读消息数 | 登录 | `Result<MessageUnread>` |
| GET | `/api/topics/{topic_id}/breadcrumb` | 获取话题面包屑名称 | 公开 | `Result<String>` |
| GET | `/api/threads/{thread_id}/breadcrumb` | 获取帖子面包屑标题 | 公开 | `Result<String>` |

聊天板列表 Query：`page_num` 默认 `1`，`page_size` 默认 `10`。

发送聊天板消息请求体 `ChatBoardMessage`：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `topicId` | Integer | 否 | 由路径 `{topic_id}` 覆盖 |
| `content` | String | 是 | 消息内容 |

系统消息 Query：`page_num` 必填，`page_size` 默认 `7`。

通知 Query：`type` 可选；为空时返回总未读。

## 旧接口迁移对照

| 旧接口 | 新接口 |
| --- | --- |
| `POST /api/auth/register_verify` | `POST /api/auth/register-verifications` |
| `GET /api/auth/verify` | `GET /api/auth/register-verifications` |
| `POST /api/auth/register` | `POST /api/auth/registrations` |
| `POST /api/auth/login` | `POST /api/auth/login` |
| `POST /api/auth/logout` | `DELETE /api/auth/logout` |
| `GET /api/user/info` | `GET /api/users/me` |
| `GET /api/user/info/by_user_id` | `GET /api/users/{user_id}` |
| `PUT /api/user/update_avatar` | `PUT /api/users/me/avatar` |
| `PUT /api/user/update_banner` | `PUT /api/users/me/banner` |
| `GET/POST /api/stat/info` | `GET /api/users/me/stats` |
| `GET /api/theme/info/list` | `GET /api/themes` |
| `GET /api/theme/info/list_themes_contains_topics` | `GET /api/themes/topics` |
| `PUT /api/theme/insert` | `POST /api/themes` |
| `GET /api/topic/info/list_by_theme_id` | `GET /api/themes/{theme_id}/topics` |
| `PUT /api/topic/insert` | `POST /api/topics` |
| `PUT /api/topic/update` | `PUT /api/topics/{topic_id}` |
| `DELETE /api/topic/delete` | `DELETE /api/topics/{topic_id}` |
| `GET /api/tag/info/list_by_topic` | `GET /api/topics/{topic_id}/tags` |
| `PUT /api/tag/perm/insert_new_tag` | `POST /api/topics/{topic_id}/tags` |
| `GET /api/thread/info/topic` | `GET /api/topics/{topic_id}/threads` |
| `GET /api/thread/info/user` | `GET /api/users/{user_id}/threads` |
| `GET /api/thread/info` | `GET /api/threads/{thread_id}` |
| `GET /api/thread/info/announcement` | `GET /api/topics/{topic_id}/announcements` |
| `POST /api/thread/post_thread` | `POST /api/threads` |
| `DELETE /api/thread/remove_thread` | `DELETE /api/threads/{thread_id}` |
| `POST /api/thread/view` | `POST /api/threads/{thread_id}/views` |
| `POST /api/thread/perm/update_tag` | `PUT /api/moderation/threads/{thread_id}/tag` |
| `POST /api/thread/perm/delete_tag` | `DELETE /api/moderation/threads/{thread_id}/tag` |
| `POST /api/thread/perm/set_announcement` | `PUT /api/topics/{topic_id}/announcements/{thread_id}` |
| `POST /api/thread/perm/unset_announcement` | `DELETE /api/topics/{topic_id}/announcements/{thread_id}` |
| `POST /api/thread/perm/remove_thread` | `DELETE /api/moderation/threads/{thread_id}` |
| `GET /api/post/info/thread` | `GET /api/threads/{thread_id}/posts` |
| `POST /api/post/post` | `POST /api/threads/{thread_id}/posts` |
| `DELETE /api/post/delete` | `DELETE /api/posts/{post_id}` |
| `DELETE /api/post/perm/delete` | `DELETE /api/moderation/posts/{post_id}` |
| `GET /api/post/message/list` | `GET /api/posts/reply-messages` |
| `POST /api/like/like_thread` | `POST /api/threads/{thread_id}/likes` |
| `POST /api/like/unlike_thread` | `DELETE /api/threads/{thread_id}/likes` |
| `GET /api/like/info/is_like` | `GET /api/threads/{thread_id}/likes/me` |
| `GET /api/like/info/get_like_count` | `GET /api/threads/{thread_id}/likes/count` |
| `GET /api/like/get_likes` | `GET /api/users/{user_id}/liked-threads` |
| `POST /api/collect/collect_thread` | `POST /api/threads/{thread_id}/collections` |
| `POST /api/collect/uncollect_thread` | `DELETE /api/threads/{thread_id}/collections` |
| `GET /api/collect/info/is_collect` | `GET /api/threads/{thread_id}/collections/me` |
| `GET /api/collect/info/get_collect_count` | `GET /api/threads/{thread_id}/collections/count` |
| `GET /api/collect/get_collects` | `GET /api/users/{user_id}/collected-threads` |
| `GET /api/search/info/query` | `GET /api/search/threads` |
| `GET /api/search/info/query/user` | `GET /api/search/users` |
| `GET /api/search/history` | `GET /api/search/history` |
| `GET /api/search/query/history` | `GET /api/search/history/query` |
| `DELETE /api/search/history` | `DELETE /api/search/history` |
| `GET /api/search/info/hot_keyword` | `GET /api/search/hot-keywords` |
| `POST /api/conversation/new` | `POST /api/conversations` |
| `GET /api/conversation/talk` | `GET /api/conversations/with-user/{account_id}` |
| `POST /api/conversation/hide` | `DELETE /api/conversations/{conversation_id}` |
| `GET /api/conversation/list` | `GET /api/conversations` |
| `POST /api/conversation/send` | `POST /api/conversations/{conversation_id}/messages` |
| `GET /api/conversation/message/list` | `GET /api/conversations/{conversation_id}/messages` |
| `GET /api/conversation/message/unread` | `GET /api/conversations/unread-messages` |
| `GET /api/conversation/message/read` | `DELETE /api/conversations/{conversation_id}/unread-messages` |
| `POST /api/chat/send` | `POST /api/topics/{topic_id}/chat-messages` |
| `GET /api/chat/info/history` | `GET /api/topics/{topic_id}/chat-messages` |
| `GET /api/system/message/list` | `GET /api/system-messages` |
| `GET /api/notif/remaining_message_unread` | `GET /api/notifications/unread-count` |
| `GET /api/bread/info/topic_bread` | `GET /api/topics/{topic_id}/breadcrumb` |
| `GET /api/bread/info/thread_bread` | `GET /api/threads/{thread_id}/breadcrumb` |
