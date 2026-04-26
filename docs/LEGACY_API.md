# Web App API 文档

本文档根据 `web/web-app/src/main/java/com/ayor/controller` 与相关 DTO/VO 类型整理。

## 基础约定

- 基础路径：所有 Controller 接口均以 `/api` 开头。
- 请求体格式：除特别说明外，JSON 请求体使用 `Content-Type: application/json`。
- 鉴权方式：登录后通过响应中的 JWT 访问受保护接口，通常放入 `Authorization` 请求头。
- 放行规则：`/api/auth/**`、`/api/*/info/**` 以及 `/chat`、`/chatboard`、`/system` 在当前安全配置中放行；其他接口默认需要认证。
- 权限规则：带 `/perm/` 或 `@PreAuthorize` 的接口还需要对应角色或权限。

## 通用响应

除 `/api/auth/verify` 外，接口统一返回：

```json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | Integer | 状态码，成功通常为 `200` |
| `message` | String | 返回信息 |
| `data` | Any | 返回数据，可能不存在或为 `null` |

常见状态码：

| code | message | 说明 |
| --- | --- | --- |
| `200` | 成功 | 请求成功 |
| `201` | 失败 | 业务处理失败 |
| `203` | 请求参数验证有误 / 请求参数内容有误 | 参数校验或缺少参数 |
| `401` | 未认证 / 认证异常信息 | 未登录或 Token 无效 |
| `403` | 权限不足 | 无访问权限 |
| `400` | 退出失败 / 参数校验失败 | 登出或校验失败 |

分页响应 `PageEntity<T>`：

```json
{
  "totalSize": 100,
  "data": []
}
```

## 认证接口

### POST `/api/auth/register_verify`

发送注册验证邮件。

请求体 `RegDTO`：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `email` | String | 是 | 邮箱格式 |

响应：`Result<String>`，`data` 为授权 Token 或相关字符串。

### POST `/api/auth/register`

注册账号。

请求体 `AccountDTO`：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `username` | String | 是 | 长度 4-16，仅字母、数字、下划线 |
| `password` | String | 是 | 长度 6-16，仅字母、数字、下划线 |
| `nickname` | String | 是 | 长度 3-20 |
| `token` | String | 是 | 注册验证 Token |

响应：`Result<Void>`。

### GET `/api/auth/verify`

校验注册邮箱 Token。

Query 参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `email` | String | 是 | 邮箱 |
| `token` | String | 是 | 验证 Token |

响应：纯文本 `验证成功` 或 `验证失败`。

### POST `/api/auth/login`

登录接口由 Spring Security `formLogin` 配置提供。

表单参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | String | 是 | 用户名 |
| `password` | String | 是 | 密码 |

响应：`Result<AuthorizeVO>`。

`AuthorizeVO` 字段：`username`、`role`、`token`、`expire`。

### POST `/api/auth/logout`

登出接口由 Spring Security 配置提供。

请求头：

| Header | 必填 | 说明 |
| --- | --- | --- |
| `Authorization` | 是 | 当前 JWT |

响应：`Result<Void>`，成功信息为 `退出成功`。

## 用户接口

### GET `/api/user/info`

获取当前登录用户信息。放行路径匹配 `/api/*/info/**`，但业务仍读取当前安全上下文。

响应：`Result<UserInfoVO>`。

### GET `/api/user/info/by_user_id`

按用户 ID 获取用户信息。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `user_id` | String | 是 |

响应：`Result<UserInfoVO>`。

### PUT `/api/user/update_avatar`

更新当前登录用户头像。

请求体 `Base64Upload`：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `base64` | String | 是 | Base64 文件内容 |
| `fileName` | String | 是 | 文件名 |

响应：`Result<Void>`。

### PUT `/api/user/update_banner`

更新当前登录用户横幅图。

请求体：`Base64Upload`。

响应：`Result<Void>`。

## 统计接口

### GET/POST `/api/stat/info`

获取当前登录用户统计信息。该方法使用 `@RequestMapping`，未限制 HTTP 方法。

响应：`Result<AccountStatVO>`。

## 主题接口

### GET `/api/theme/info/list`

获取主题列表。

响应：`Result<List<ThemeVO>>`。

### GET `/api/theme/info/list_themes_contains_topics`

获取主题及其下的话题列表。

响应：`Result<List<ThemeTopicVO>>`。

### PUT `/api/theme/insert`

新增主题。需要 `ROLE_OWNER`。

请求体 `ThemeDTO`：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `title` | String | 是 | 长度 1-10 |

响应：`Result<Void>`。

## 话题接口

### GET `/api/topic/info/list_by_theme_id`

获取指定主题下的话题列表。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `theme_id` | String | 是 |

响应：`Result<List<TopicVO>>`。

### PUT `/api/topic/insert`

新增话题。需要 `ROLE_OWNER`。

请求体 `TopicDTO`：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `topicId` | Integer | 否 | 更新时使用 |
| `title` | String | 是 | 长度 1-10 |
| `cover` | Base64Upload | 是 | 封面 |
| `description` | String | 否 | 最大 20 字符 |
| `themeId` | Integer | 是 | 所属主题 ID |

响应：`Result<Void>`。

### PUT `/api/topic/update`

更新话题。需要 `ROLE_OWNER`。

请求体：`TopicDTO`。

响应：`Result<Void>`。

### DELETE `/api/topic/delete`

删除话题。需要 `ROLE_OWNER`。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `topic_id` | String | 是 |

响应：`Result<Void>`。

## 标签接口

### GET `/api/tag/info/list_by_topic`

获取话题下的标签列表。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `topic_id` | Integer | 是 |

响应：`Result<List<TagVO>>`。

### PUT `/api/tag/perm/insert_new_tag`

新增标签。需要 `ROLE_OWNER`，或同时具备 `PERM_INSERT_TAG` 与 `TOPIC_{topicId}`。

请求体 `TagDTO`：

| 字段 | 类型 | 必填 |
| --- | --- | --- |
| `tag` | String | 是 |
| `topicId` | Integer | 是 |

响应：`Result<Void>`。

## 帖子接口

### GET `/api/thread/info/topic`

按话题分页获取帖子。

Query 参数：

| 参数 | 类型 | 必填 | 默认值 |
| --- | --- | --- | --- |
| `page_num` | Integer | 是 | - |
| `page_size` | Integer | 否 | `10` |
| `topic_id` | Integer | 是 | - |

响应：`Result<PageEntity<ThreadVO>>`。

### GET `/api/thread/info/user`

按用户分页获取帖子。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `user_id` | Integer | 是 |
| `page` | Integer | 是 |
| `page_size` | Integer | 是 |

响应：`Result<PageEntity<ThreadVO>>`。

### GET `/api/thread/info`

按帖子 ID 获取帖子详情。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `thread_id` | Integer | 是 |

响应：`Result<ThreadVO>`。

### GET `/api/thread/info/announcement`

获取话题公告帖。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `topic_id` | String | 是 |

响应：`Result<List<AnnouncementVO>>`。

### POST `/api/thread/post_thread`

发布帖子。

请求体 `ThreadDTO`：

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `title` | String | 是 | 长度 1-50 |
| `content` | String | 是 | - |
| `topicId` | Integer | 是 | - |

响应：`Result<Void>`。

### DELETE `/api/thread/remove_thread`

删除当前用户自己的帖子。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `thread_id` | Integer | 是 |

响应：`Result<Void>`。

### POST `/api/thread/perm/update_tag`

更新帖子标签。需要 `ROLE_OWNER`，或同时具备 `PERM_UPDATE_TAG` 与 `TOPIC_{topicId}`。

请求体 `TagUpdateDTO`：

| 字段 | 类型 | 必填 |
| --- | --- | --- |
| `tagId` | Integer | 是 |
| `topicId` | Integer | 是 |
| `threadId` | Integer | 是 |

响应：`Result<Void>`。

### POST `/api/thread/perm/delete_tag`

删除帖子标签。需要 `ROLE_OWNER`，或同时具备 `PERM_UPDATE_TAG` 与 `TOPIC_{topicId}`。

请求体 `Konekuto`：

| 字段 | 类型 | 必填 |
| --- | --- | --- |
| `threadId` | Integer | 是 |
| `topicId` | Integer | 是 |
| `accountId` | Integer | 否 |

响应：`Result<Void>`。

### POST `/api/thread/perm/set_announcement`

设置公告帖。需要 `ROLE_OWNER`，或同时具备 `PERM_UPDATE_TAG` 与 `TOPIC_{topicId}`。

请求体：`Konekuto`。

响应：`Result<Void>`。

### POST `/api/thread/perm/unset_announcement`

取消公告帖。需要 `ROLE_OWNER`，或同时具备 `PERM_UPDATE_TAG` 与 `TOPIC_{topicId}`。

请求体：`Konekuto`。

响应：`Result<Void>`。

### POST `/api/thread/perm/remove_thread`

按权限删除帖子。代码中权限字符串为 `PERM_sDELETE_THREAD`，并要求 `TOPIC_{topicId}`，或 `ROLE_OWNER`。

请求体：`Konekuto`。

响应：`Result<Void>`。

### POST `/api/thread/view`

增加帖子浏览数。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `thread_id` | Integer | 是 |

响应：`Result<Void>`。

## 回复接口

### GET `/api/post/info/thread`

获取帖子下的回复列表。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `thread_id` | String | 是 |

响应：`Result<List<PostVO>>`。

### POST `/api/post/post`

发布回复。

请求体 `PostDTO`：

| 字段 | 类型 | 必填 |
| --- | --- | --- |
| `content` | String | 是 |
| `threadId` | Integer | 是 |

响应：`Result<Void>`。

### DELETE `/api/post/delete`

删除当前用户自己的回复。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `post_id` | Integer | 是 |

响应：`Result<Void>`。

### DELETE `/api/post/perm/delete`

按权限删除回复。需要 `ROLE_OWNER`。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `post_id` | Integer | 是 |

响应：`Result<Void>`。

### GET `/api/post/message/list`

获取当前登录用户的回复消息。

Query 参数：

| 参数 | 类型 | 必填 | 默认值 |
| --- | --- | --- | --- |
| `page_num` | Integer | 是 | - |
| `page_size` | Integer | 否 | `7` |

响应：`Result<PageEntity<ReplyMessageVO>>`。

## 点赞接口

### POST `/api/like/like_thread`

点赞帖子。

Query 参数：`thread_id` Integer，必填。

响应：`Result<Void>`。

### POST `/api/like/unlike_thread`

取消点赞帖子。

Query 参数：`thread_id` Integer，必填。

响应：`Result<Void>`。

### GET `/api/like/info/is_like`

查询当前登录用户是否已点赞帖子。

Query 参数：`thread_id` Integer，必填。

响应：`Result<Boolean>`。

### GET `/api/like/info/get_like_count`

获取帖子点赞数。

Query 参数：`thread_id` Integer，必填。

响应：`Result<Integer>`。

### GET `/api/like/get_likes`

获取用户点赞过的帖子。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `user_id` | Integer | 是 |
| `page` | Integer | 是 |
| `page_size` | Integer | 是 |

响应：`Result<PageEntity<ThreadVO>>`。

## 收藏接口

### POST `/api/collect/collect_thread`

收藏帖子。

Query 参数：`thread_id` Integer，必填。

响应：`Result<Void>`。

### POST `/api/collect/uncollect_thread`

取消收藏帖子。

Query 参数：`thread_id` Integer，必填。

响应：`Result<Void>`。

### GET `/api/collect/info/is_collect`

查询当前登录用户是否已收藏帖子。

Query 参数：`thread_id` Integer，必填。

响应：`Result<Boolean>`。

### GET `/api/collect/info/get_collect_count`

获取帖子收藏数。

Query 参数：`thread_id` Integer，必填。

响应：`Result<Integer>`。

### GET `/api/collect/get_collects`

获取用户收藏的帖子。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `user_id` | Integer | 是 |
| `page` | Integer | 是 |
| `page_size` | Integer | 是 |

响应：`Result<PageEntity<ThreadVO>>`。

## 搜索接口

### GET `/api/search/info/query`

搜索帖子。

Query 参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `query` | String | 是 | - | 搜索关键词 |
| `onlyThreadTopic` | boolean | 否 | `false` | 是否只搜索主题帖 |
| `topicId` | Integer | 否 | - | 话题 ID |
| `enableHistory` | boolean | 否 | `true` | 是否记录搜索历史 |
| `startTime` | Long | 否 | - | 起始时间戳 |
| `endTime` | Long | 否 | - | 结束时间戳 |
| `order` | String | 否 | `rel` | 排序方式 |
| `pageNum` | int | 否 | `1` | 页码 |
| `pageSize` | int | 否 | `10` | 每页数量 |

响应：`Result<PageEntity<ThreadDoc>>`。

### GET `/api/search/info/query/user`

搜索用户。当前 Controller 中为 TODO，直接返回 `Result.ok()`。

Query 参数：

| 参数 | 类型 | 必填 | 默认值 |
| --- | --- | --- | --- |
| `query` | String | 是 | - |
| `onlyThreadTopic` | boolean | 否 | `false` |
| `topicId` | Integer | 否 | - |
| `enableHistory` | boolean | 否 | `true` |
| `duration` | int | 否 | `7` |
| `pageNum` | int | 否 | `1` |
| `pageSize` | int | 否 | `10` |

响应：`Result<Void>`。

### GET `/api/search/history`

获取当前登录用户搜索历史。

响应：`Result<Set<String>>`。

### GET `/api/search/query/history`

按关键词获取搜索历史。当前实现未使用 `query` 参数，返回全部历史。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `query` | String | 否 |

响应：`Result<Set<String>>`。

### DELETE `/api/search/history`

删除当前登录用户搜索历史；传入 `query` 时删除指定关键词，否则清空全部。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `query` | String | 否 |

响应：`Result<Void>`。

### GET `/api/search/info/hot_keyword`

获取热门搜索关键词。

Query 参数：

| 参数 | 类型 | 必填 | 默认值 |
| --- | --- | --- | --- |
| `size` | int | 否 | `10` |
| `duration` | int | 否 | `7` |

响应：`Result<List<HotKeywordVO>>`。

## 私信接口

### POST `/api/conversation/new`

按用户名创建会话。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `username` | String | 是 |

响应：`Result<Void>`。

### GET `/api/conversation/talk`

按目标账号获取会话。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `accountId` | Integer | 是 |

响应：`Result<ConversationVO>`。

### POST `/api/conversation/hide`

隐藏会话。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `conversationId` | Integer | 是 |

响应：`Result<Void>`。

### GET `/api/conversation/list`

获取当前登录用户会话列表。

响应：`Result<List<ConversationVO>>`。

### POST `/api/conversation/send`

发送私信。

请求体 `ConversationMessageDTO`：

| 字段 | 类型 | 必填 |
| --- | --- | --- |
| `conversationId` | Integer | 是 |
| `content` | String | 是 |
| `toUserId` | Integer | 是 |

响应：`Result<Void>`。

### GET `/api/conversation/message/list`

获取会话消息列表。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `conversationId` | Integer | 是 |
| `page_num` | Integer | 是 |

响应：`Result<PageEntity<ConversationMessageVO>>`。

### GET `/api/conversation/message/unread`

获取当前登录用户的未读私信列表。

响应：`Result<List<ChatUnread>>`。

### GET `/api/conversation/message/read`

清除会话未读数。

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `conversationId` | Integer | 是 |
| `fromUserId` | Integer | 是 |

响应：`Result<Void>`。

## 聊天板接口

### POST `/api/chat/send`

发送话题聊天板消息。

请求体 `ChatBoardMessage`：

| 字段 | 类型 | 必填 |
| --- | --- | --- |
| `topicId` | Integer | 是 |
| `content` | String | 是 |

响应：`Result<Void>`。

### GET `/api/chat/info/history`

分页获取话题聊天板历史。

Query 参数：

| 参数 | 类型 | 必填 | 默认值 |
| --- | --- | --- | --- |
| `topic_id` | Integer | 是 | - |
| `page_num` | Integer | 否 | `1` |
| `page_size` | Integer | 否 | `10` |

响应：`Result<PageEntity<ChatboardHistoryVO>>`。

## 系统消息接口

### GET `/api/system/message/list`

获取当前登录用户系统消息。

Query 参数：

| 参数 | 类型 | 必填 | 默认值 |
| --- | --- | --- | --- |
| `page_num` | Integer | 是 | - |
| `page_size` | Integer | 否 | `7` |

响应：`Result<PageEntity<SystemMessageVO>>`。

## 通知接口

### GET `/api/notif/remaining_message_unread`

获取当前登录用户未读消息数量。

Query 参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `type` | String | 否 | 消息类型；为空时返回总未读 |

响应：`Result<MessageUnread>`。

## 面包屑接口

### GET `/api/bread/info/topic_bread`

获取话题名称。

Query 参数：`topic_id` Integer，必填。

响应：`Result<String>`。

### GET `/api/bread/info/thread_bread`

获取帖子标题。

Query 参数：`thread_id` Integer，必填。

响应：`Result<String>`。

## 主要数据结构

### Base64Upload

| 字段 | 类型 |
| --- | --- |
| `base64` | String |
| `fileName` | String |

### UserInfoVO

| 字段 | 类型 |
| --- | --- |
| `accountId` | Integer |
| `username` | String |
| `nickname` | String |
| `avatarUrl` | String |
| `bannerUrl` | String |
| `bio` | String |
| `status` | String |
| `permission` | UserPermissionVO |
| `createTime` | Date |
| `updateTime` | Date |

### UserPermissionVO

| 字段 | 类型 |
| --- | --- |
| `accountId` | Integer |
| `roleName` | String |
| `roleNick` | String |
| `topicId` | Integer |
| `permissions` | List<String> |

### AccountStatVO

| 字段 | 类型 |
| --- | --- |
| `accountStatId` | Integer |
| `threadCount` | Integer |
| `postCount` | Integer |
| `replyCount` | Integer |
| `likedCount` | Integer |
| `collectedCount` | Integer |
| `accountId` | Integer |

### ThemeVO

| 字段 | 类型 |
| --- | --- |
| `themeId` | Integer |
| `title` | String |

### ThemeTopicVO

| 字段 | 类型 |
| --- | --- |
| `themeId` | Integer |
| `title` | String |
| `topics` | List<TopicVO> |

### TopicVO

| 字段 | 类型 |
| --- | --- |
| `topicId` | Integer |
| `title` | String |
| `coverUrl` | String |
| `threadCount` | Integer |
| `viewCount` | Integer |
| `description` | String |
| `createTime` | String |
| `themeId` | Integer |

### TagVO

| 字段 | 类型 |
| --- | --- |
| `tagId` | Integer |
| `tag` | String |
| `topicId` | Integer |
| `createTime` | Date |

### ThreadVO

| 字段 | 类型 |
| --- | --- |
| `threadId` | Integer |
| `title` | String |
| `imageUrls` | List<String> |
| `content` | String |
| `createTime` | Date |
| `viewCount` | Integer |
| `postCount` | Integer |
| `likeCount` | Integer |
| `collectCount` | Integer |
| `tagName` | String |
| `accountId` | Integer |
| `topicId` | Integer |
| `accountName` | String |
| `avatarUrl` | String |
| `Tag` | TagVO |

### AnnouncementVO

| 字段 | 类型 |
| --- | --- |
| `threadId` | Integer |
| `topicId` | Integer |
| `title` | String |

### PostVO

| 字段 | 类型 |
| --- | --- |
| `postId` | Integer |
| `content` | String |
| `accountId` | Integer |
| `createTime` | Date |
| `updateTime` | Date |
| `avatarUrl` | String |
| `nickname` | String |
| `threadId` | Integer |
| `topicId` | Integer |

### ReplyMessageVO

| 字段 | 类型 |
| --- | --- |
| `postId` | Integer |
| `content` | String |
| `createTime` | Date |
| `threadId` | Integer |
| `topicId` | Integer |
| `threadTitle` | String |
| `nickname` | String |

### ThreadDoc

| 字段 | 类型 |
| --- | --- |
| `id` | String |
| `threadId` | Integer |
| `topicId` | Integer |
| `title` | String |
| `content` | String |
| `viewCount` | Integer |
| `likeCount` | Integer |
| `collectCount` | Integer |
| `createTime` | Date |
| `updateTime` | Date |
| `isThreadTopic` | Boolean |

### HotKeywordVO

| 字段 | 类型 |
| --- | --- |
| `keyword` | String |
| `count` | Long |

### ConversationVO

| 字段 | 类型 |
| --- | --- |
| `conversationId` | Integer |
| `userInfo` | UserInfoVO |
| `updateTime` | Date |

### ConversationMessageVO

| 字段 | 类型 |
| --- | --- |
| `conversationMessageId` | Integer |
| `content` | String |
| `accountId` | Integer |
| `avatarUrl` | String |
| `createTime` | Date |
| `updateTime` | Date |
| `isEdit` | Boolean |

### ChatUnread

| 字段 | 类型 |
| --- | --- |
| `conversationId` | Integer |
| `fromUserId` | Integer |
| `unread` | Long |

### ChatboardHistoryVO

| 字段 | 类型 |
| --- | --- |
| `chatboardHistoryId` | Integer |
| `accountId` | Integer |
| `nickname` | String |
| `avatarUrl` | String |
| `bannerUrl` | String |
| `topicId` | Integer |
| `content` | String |
| `createTime` | Date |

### SystemMessageVO

| 字段 | 类型 |
| --- | --- |
| `systemMessageId` | Integer |
| `title` | String |
| `content` | String |
| `createTime` | Date |

### MessageUnread

| 字段 | 类型 |
| --- | --- |
| `unread` | Long |
