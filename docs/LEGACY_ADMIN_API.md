# Web Admin Legacy API 迁移文档

本文档记录 `web/web-admin` 旧接口到新版 RESTful 接口的迁移关系。

## 迁移规则

- 基础路径保持 `/api`
- 资源名统一改为复数名词
- 多单词资源统一使用下划线
- 更新接口统一改为 `PUT /api/<resources>/{id}`
- 动作型接口改为子资源或独立资源
- Query 参数继续保留 `snake_case`

## 接口迁移表

| 方法 | 旧路径 | 新路径 | 参数变化 | 备注 |
| --- | --- | --- | --- | --- |
| GET | `/api/account/list` | `/api/accounts` | 保留 `query` `status` `page_num` `page_size` | 用户分页列表 |
| GET | `/api/account/get_account_by_id` | `/api/accounts/{accountId}` | `account_id` 从 query 改为 path | 用户详情 |
| GET | `/api/account/get_account_by_role_id` | `/api/accounts` | `role_id` 合并为 query 过滤条件 | 按角色筛选用户 |
| GET | `/api/account/list_user_options` | `/api/accounts/options` | 无 | 用户下拉选项 |
| POST | `/api/account/submit_violation` | `/api/accounts/{accountId}/violations` | `accountId` 从 query 改为 path | 用户违规处理 |
| PUT | `/api/account/update` | `/api/accounts/{accountId}` | `accountId` 从请求体主键改为 path 主键 | 更新用户 |
| DELETE | `/api/account/{account_id}` | `/api/accounts/{accountId}` | 路径变量改名 | 删除用户 |
| GET | `/api/account_stat/list` | `/api/account_stats` | 保留 `account_id` `page_num` `page_size` | 用户统计列表 |
| PUT | `/api/account_stat/{stat_id}` | `/api/account_stats/{statId}` | 路径变量改名 | 更新用户统计 |
| POST | `/api/broadcast/user` | `/api/user_broadcasts` | 无 | 发送用户广播 |
| GET | `/api/chatboard/history` | `/api/chatboard_histories` | 保留 `topic_id` `page_num` `page_size` | 聊天板历史列表 |
| DELETE | `/api/chatboard/history/{history_id}` | `/api/chatboard_histories/{historyId}` | 路径变量改名 | 删除聊天板历史 |
| GET | `/api/collect/list` | `/api/collects` | 保留 `thread_id` `account_id` `page_num` `page_size` | 收藏记录列表 |
| DELETE | `/api/collect/{collect_id}` | `/api/collects/{collectId}` | 路径变量改名 | 删除收藏记录 |
| GET | `/api/conversation/list` | `/api/conversations` | 保留 `alpha_account_id` `beta_account_id` `page_num` `page_size` | 会话列表 |
| DELETE | `/api/conversation/{conversation_id}` | `/api/conversations/{conversationId}` | 路径变量改名 | 删除会话 |
| GET | `/api/conversation/{conversation_id}/messages` | `/api/conversations/{conversationId}/messages` | 路径变量改名 | 会话消息列表 |
| DELETE | `/api/conversation/message/{message_id}` | `/api/conversation_messages/{messageId}` | 改为独立消息资源 | 删除会话消息 |
| GET | `/api/like/list` | `/api/likes` | 保留 `thread_id` `account_id` `page_num` `page_size` | 点赞记录列表 |
| DELETE | `/api/like/{like_id}` | `/api/likes/{likeId}` | 路径变量改名 | 删除点赞记录 |
| GET | `/api/permission/list` | `/api/permissions` | 保留 `role_id` | 权限列表 |
| POST | `/api/permission` | `/api/permissions` | 无 | 创建权限 |
| PUT | `/api/permission/{permission_id}` | `/api/permissions/{permissionId}` | 路径变量改名 | 更新权限 |
| DELETE | `/api/permission/{permission_id}` | `/api/permissions/{permissionId}` | 路径变量改名 | 删除权限 |
| GET | `/api/post/list` | `/api/posts` | `thread_id` 保留为 query 过滤条件 | 按帖子查询回复 |
| GET | `/api/post/list_by_account` | `/api/posts` | `account_id` 保留为 query 过滤条件 | 按用户查询回复 |
| GET | `/api/post/{post_id}` | `/api/posts/{postId}` | 路径变量改名 | 回复详情 |
| POST | `/api/post` | `/api/posts` | 无 | 创建回复 |
| PUT | `/api/post/{post_id}` | `/api/posts/{postId}` | 路径变量改名 | 更新回复 |
| DELETE | `/api/post/{post_id}` | `/api/posts/{postId}` | 路径变量改名 | 删除回复 |
| GET | `/api/role/list` | `/api/roles` | 无 | 角色列表 |
| POST | `/api/role` | `/api/roles` | 无 | 创建角色 |
| PUT | `/api/role` | `/api/roles/{roleId}` | `roleId` 从请求体主键改为 path 主键 | 更新角色 |
| DELETE | `/api/role/{role_id}` | `/api/roles/{roleId}` | 路径变量改名 | 删除角色 |
| GET | `/api/tag/list` | `/api/tags` | 不传 `page_num` 时返回列表 | 标签列表 |
| GET | `/api/tag/page` | `/api/tags` | 传 `page_num` 时返回分页 | 标签分页列表 |
| POST | `/api/tag` | `/api/tags` | 无 | 创建标签 |
| PUT | `/api/tag/{tag_id}` | `/api/tags/{tagId}` | 路径变量改名 | 更新标签 |
| DELETE | `/api/tag/{tag_id}` | `/api/tags/{tagId}` | 路径变量改名 | 删除标签 |
| GET | `/api/theme/get_themes` | `/api/themes` | 保留 `page_num` `page_size` | 主题分页列表 |
| POST | `/api/theme` | `/api/themes` | 无 | 创建主题 |
| PUT | `/api/theme` | `/api/themes/{themeId}` | `themeId` 从请求体主键改为 path 主键 | 更新主题 |
| DELETE | `/api/theme/{theme_id}` | `/api/themes/{themeId}` | 路径变量改名 | 删除主题 |
| GET | `/api/thread/get_threads` | `/api/threads` | 保留 `page_num` `page_size` | 帖子分页列表 |
| POST | `/api/thread` | `/api/threads` | 无 | 创建帖子 |
| PUT | `/api/thread` | `/api/threads/{threadId}` | `threadId` 从请求体主键改为 path 主键 | 更新帖子 |
| DELETE | `/api/thread/{thread_id}` | `/api/threads/{threadId}` | 路径变量改名 | 删除帖子 |
| GET | `/api/topic_chat/list` | `/api/topic_chats` | 保留 `topic_id` `page_num` `page_size` | 话题聊天记录列表 |
| DELETE | `/api/topic_chat/{topic_chat_id}` | `/api/topic_chats/{topicChatId}` | 路径变量改名 | 删除话题聊天记录 |
| GET | `/api/topic/get_topics_by_theme_id` | `/api/topics` | `theme_id` 合并为 query 过滤条件 | 按主题筛选话题 |
| GET | `/api/topic/list` | `/api/topics` | 保留 `page_num` `page_size` | 话题分页列表 |
| GET | `/api/topic/list_options` | `/api/topics/options` | 无 | 话题选项列表 |
| POST | `/api/topic` | `/api/topics` | 无 | 创建话题 |
| PUT | `/api/topic` | `/api/topics/{topicId}` | `topicId` 从请求体主键改为 path 主键 | 更新话题 |
| DELETE | `/api/topic/{topic_id}` | `/api/topics/{topicId}` | 路径变量改名 | 删除话题 |
| GET | `/api/topic_stat/list` | `/api/topic_stats` | 保留 `topic_id` `page_num` `page_size` | 话题统计列表 |
| PUT | `/api/topic_stat/{stat_id}` | `/api/topic_stats/{statId}` | 路径变量改名 | 更新话题统计 |
