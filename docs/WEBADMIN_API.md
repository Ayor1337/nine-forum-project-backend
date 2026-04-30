# Web Admin RESTful API 文档

本文档根据 `web/web-admin/src/main/java/com/ayor/controller` 的当前实现整理。

## 基础约定

- 基础路径：`/api`
- 资源命名：复数名词，多个单词使用下划线
- Path 参数：使用驼峰命名，如 `accountId`、`conversationId`
- Query 参数：沿用现有 `snake_case`，如 `page_num`、`page_size`、`theme_id`
- 请求体格式：除特别说明外，均为 `application/json`
- 响应格式：继续使用 `Result<T>` 与 `PageEntity<T>`

通用响应：

```json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
```

分页响应 `PageEntity<T>`：

```json
{
  "totalSize": 100,
  "data": []
}
```

## 账户与账户统计

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/accounts` | 分页获取用户列表，支持 `query` `status` `role_id` |
| GET | `/api/accounts/{accountId}` | 获取单个用户详情 |
| GET | `/api/accounts/options` | 获取用户下拉选项 |
| PUT | `/api/accounts/{accountId}` | 更新用户 |
| DELETE | `/api/accounts/{accountId}` | 删除用户 |
| POST | `/api/accounts/{accountId}/violations` | 提交用户违规处理 |
| GET | `/api/account_stats` | 分页获取用户统计 |
| PUT | `/api/account_stats/{statId}` | 更新用户统计 |

### GET `/api/accounts`

Query 参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `query` | String | 否 | 搜索关键词 |
| `status` | Integer | 否 | 用户状态 |
| `role_id` | Integer | 否 | 角色筛选 |
| `page_num` | Integer | 否 | 页码，默认 `1` |
| `page_size` | Integer | 否 | 每页数量，默认 `10` |

响应：

- 未传 `role_id`：`Result<PageEntity<AccountVO>>`
- 传 `role_id`：`Result<PageEntity<AccountVO>>`

### GET `/api/accounts/{accountId}`

Path 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `accountId` | Integer | 是 |

响应：`Result<AccountVO>`

### GET `/api/accounts/options`

Query 参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `query` | String | 否 | 可选搜索关键词 |

响应：`Result<List<AccountVO>>`

### PUT `/api/accounts/{accountId}`

Path 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `accountId` | Integer | 是 |

请求体：`AccountDTO`

| 字段 | 类型 | 必填 |
| --- | --- | --- |
| `accountId` | Integer | 否，最终以路径为准 |
| `status` | Integer | 否 |
| `roleId` | Integer | 否 |
| `isDeleted` | Boolean | 否 |

响应：`Result<Void>`

### POST `/api/accounts/{accountId}/violations`

Path 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `accountId` | Integer | 是 |

Query 参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `type` | String | 是 | 违规类型 |

响应：`Result<Void>`

### GET `/api/account_stats`

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `account_id` | Integer | 否 |
| `page_num` | Integer | 是 |
| `page_size` | Integer | 否，默认 `10` |

响应：`Result<PageEntity<AccountStat>>`

`AccountStat` 返回/更新字段补充包含：`threadCount`、`postCount`、`replyCount`、`likedCount`、`collectedCount`、`followingCount`、`followerCount`、`accountId`

### PUT `/api/account_stats/{statId}`

响应：`Result<Void>`

## 角色、权限、广播

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/roles` | 获取角色列表 |
| POST | `/api/roles` | 创建角色 |
| PUT | `/api/roles/{roleId}` | 更新角色 |
| DELETE | `/api/roles/{roleId}` | 删除角色 |
| GET | `/api/permissions` | 获取权限列表，支持 `role_id` |
| POST | `/api/permissions` | 创建权限 |
| PUT | `/api/permissions/{permissionId}` | 更新权限 |
| DELETE | `/api/permissions/{permissionId}` | 删除权限 |
| POST | `/api/user_broadcasts` | 发送用户广播 |

`RoleDTO` 字段：`roleId`、`roleName`、`roleNick`、`priority`、`topicId`

`Permission` 请求体沿用实体字段。

`POST /api/user_broadcasts` 请求体：`UserSystemMessage<String>`

## 主题、话题、标签与统计

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/themes` | 分页获取主题列表 |
| POST | `/api/themes` | 创建主题 |
| PUT | `/api/themes/{themeId}` | 更新主题 |
| DELETE | `/api/themes/{themeId}` | 删除主题 |
| GET | `/api/topics` | 分页获取话题列表，支持 `theme_id` |
| GET | `/api/topics/options` | 分页获取话题选项 |
| POST | `/api/topics` | 创建话题 |
| PUT | `/api/topics/{topicId}` | 更新话题 |
| DELETE | `/api/topics/{topicId}` | 删除话题 |
| GET | `/api/tags` | 获取标签列表或分页结果 |
| POST | `/api/tags` | 创建标签 |
| PUT | `/api/tags/{tagId}` | 更新标签 |
| DELETE | `/api/tags/{tagId}` | 删除标签 |
| GET | `/api/topic_stats` | 分页获取话题统计 |
| PUT | `/api/topic_stats/{statId}` | 更新话题统计 |

### GET `/api/themes`

Query 参数：`page_num`、`page_size`

响应：`Result<PageEntity<ThemeVO>>`

`ThemeDTO` 字段：`themeId`、`title`、`isDeleted`

### GET `/api/topics`

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `page_num` | Integer | 是 |
| `page_size` | Integer | 否，默认 `10` |
| `theme_id` | Integer | 否 |

响应：`Result<PageEntity<TopicVO>>`

`TopicDTO` 字段：`topicId`、`title`、`coverUrl`、`description`、`createTime`、`themeId`、`isDeleted`

### GET `/api/topics/options`

Query 参数：`page_num`、`page_size`

响应：`Result<PageEntity<TopicVO>>`

### GET `/api/tags`

Query 参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `topic_id` | Integer | 否 | 话题筛选 |
| `page_num` | Integer | 否 | 传入时返回分页结果 |
| `page_size` | Integer | 否 | 默认 `10` |

响应：

- 不传 `page_num`：`Result<List<Tag>>`
- 传 `page_num`：`Result<PageEntity<Tag>>`

### GET `/api/topic_stats`

Query 参数：`topic_id`、`page_num`、`page_size`

响应：`Result<PageEntity<TopicStat>>`

## 帖子、回复、点赞、收藏

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/threads` | 分页获取帖子列表 |
| POST | `/api/threads` | 创建帖子 |
| PUT | `/api/threads/{threadId}` | 更新帖子 |
| DELETE | `/api/threads/{threadId}` | 删除帖子 |
| GET | `/api/posts` | 分页获取回复列表，支持 `thread_id` 或 `account_id` |
| GET | `/api/posts/{postId}` | 获取回复详情 |
| POST | `/api/posts` | 创建回复 |
| PUT | `/api/posts/{postId}` | 更新回复 |
| DELETE | `/api/posts/{postId}` | 删除回复 |
| GET | `/api/likes` | 分页获取点赞记录 |
| DELETE | `/api/likes/{likeId}` | 删除点赞记录 |
| GET | `/api/collects` | 分页获取收藏记录 |
| DELETE | `/api/collects/{collectId}` | 删除收藏记录 |

### GET `/api/threads`

Query 参数：`page_num`、`page_size`

响应：`Result<PageEntity<ThreadTableVO>>`

`ThreadDTO` 字段：`threadId`、`title`、`content`、`createTime`、`updateTime`、`viewCount`、`postCount`、`likeCount`、`collectCount`、`topicId`、`tagId`、`accountId`、`isMuted`、`isSelected`、`isDeleted`、`isAnnouncement`

### GET `/api/posts`

Query 参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `thread_id` | Integer | 否 | 与 `account_id` 二选一 |
| `account_id` | Integer | 否 | 与 `thread_id` 二选一 |
| `page_num` | Integer | 是 | 页码 |
| `page_size` | Integer | 否 | 默认 `10` |

响应：`Result<PageEntity<Post>>`

当 `thread_id` 与 `account_id` 都未传时，返回参数错误。

### GET `/api/likes`

Query 参数：`thread_id`、`account_id`、`page_num`、`page_size`

响应：`Result<PageEntity<LikeThread>>`

### GET `/api/collects`

Query 参数：`thread_id`、`account_id`、`page_num`、`page_size`

响应：`Result<PageEntity<Collect>>`

## 会话与聊天记录

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/conversations` | 分页获取会话列表 |
| DELETE | `/api/conversations/{conversationId}` | 删除会话 |
| GET | `/api/conversations/{conversationId}/messages` | 分页获取会话消息 |
| DELETE | `/api/conversation_messages/{messageId}` | 删除会话消息 |
| GET | `/api/topic_chats` | 分页获取话题聊天记录 |
| DELETE | `/api/topic_chats/{topicChatId}` | 删除话题聊天记录 |
| GET | `/api/chatboard_histories` | 分页获取聊天板历史 |
| DELETE | `/api/chatboard_histories/{historyId}` | 删除聊天板历史 |

### GET `/api/conversations`

Query 参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `alpha_account_id` | Integer | 否 |
| `beta_account_id` | Integer | 否 |
| `page_num` | Integer | 是 |
| `page_size` | Integer | 否，默认 `10` |

响应：`Result<PageEntity<Conversation>>`

### GET `/api/conversations/{conversationId}/messages`

Query 参数：`page_num`、`page_size`

响应：`Result<PageEntity<ConversationMessage>>`

### GET `/api/topic_chats`

Query 参数：`topic_id`、`page_num`、`page_size`

响应：`Result<PageEntity<TopicChat>>`

### GET `/api/chatboard_histories`

Query 参数：`topic_id`、`page_num`、`page_size`

响应：`Result<PageEntity<ChatboardHistory>>`
