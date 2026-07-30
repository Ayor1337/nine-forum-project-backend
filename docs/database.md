# 数据库说明

MySQL 是 NineForum 的主业务数据库。完整本地初始化 schema 主要位于 `.docker/image/mysql/nine_forum_schema.sql`，增量 SQL 片段位于 `docs/sql`。

## SQL 文件

| 文件 | 用途 |
| --- | --- |
| `.docker/image/mysql/nine_forum_schema.sql` | 本地完整初始化 schema，包含主要表、约束和部分初始数据。 |
| `docs/sql/20260624_follow_message.sql` | 关注动态消息表相关增量 SQL。 |
| `docs/sql/20260725_credit.sql` | Credit 货币余额表与流水表相关增量 SQL。 |
| `docs/sql/20260725_shop.sql` | 商城商品表、用户背包表与购买记录表相关增量 SQL。 |
| `docs/sql/20260725_shop_item_key.sql` | 商品表新增唯一关键字 `item_key` 的增量 SQL。 |
| `docs/sql/20260730_decoration.sql` | 装扮设计表 `decoration` 与商品表绑定列 `decoration_id` 的增量 SQL。 |

当前 Docker Compose 使用 `mysql:latest` 镜像，未直接启用 `.docker/image/mysql/Dockerfile` 构建。首次启动容器后如需完整表结构，需要手动导入 schema，或调整 Compose 改用自定义镜像。

## 核心表分组

| 分组 | 表 |
| --- | --- |
| 账号与资料 | `account`、`account_info`、`account_login_session`、`account_stat`、`user_privacy_setting`、`privacy` |
| 角色权限 | `role`、`permission`、`permission_operation_log` |
| 内容结构 | `theme`、`topic`、`topic_stat`、`tag`、`thread`、`thread_edit_history`、`post`、`post_edit_history` |
| 互动行为 | `like_thread`、`collect`、`history`、`user_relation` |
| 通知治理 | `mention_message`、`follow_message`、`report`、`feedback`、`dashboard_activity` |
| 私信聊天 | `conversation`、`conversation_message`、`conversation_user_setting` |
| 图片资源 | `image_asset`、`image_asset_favorite`、`content_image_ref` |
| 安全认证 | `passkey_credential` |
| 公告 | `announcements` |
| 货币 | `credit_account`、`credit_transaction` |
| 商城 | `shop_item`、`user_item`、`shop_order` |
| 装扮 | `decoration` |

## 主要关系

- `account.role_id` 关联 `role.role_id`。
- `permission.role_id` 关联 `role.role_id`，并通过 `(role_id, permission)` 保持角色权限唯一。
- `topic.theme_id` 关联 `theme.theme_id`。
- `thread.topic_id`、`thread.tag_id`、`thread.account_id` 分别关联话题、标签和作者账号。
- `post.thread_id`、`post.account_id` 分别关联帖子和回复作者；`post.reply_to` 可为空，引用被回复的 `post.post_id`，业务层限制只能引用同一帖子下的未删除回复。
- `thread_edit_history`、`post_edit_history` 保存内容编辑历史，并记录编辑者账号。
- `follow_message` 保存被关注者发布主题帖后生成的关注动态消息。
- `conversation` 保存两名用户之间的私信会话，`alpha_account_id` 固定存较小账号 ID、`beta_account_id` 固定存较大账号 ID，`hidden` 表示每一方是否从列表隐藏。
- `conversation_message` 保存私信纯文本消息，用户撤回时保留记录并设置 `is_deleted = 1`，同时清空 `content`。
- `conversation_user_setting` 保存账号维度的会话设置，本轮使用 `pinned` 实现跨设备置顶，不迁移现有 `conversation.hidden`。
- `like_thread`、`collect`、`history` 保存用户与帖子的互动关系。
- `user_relation` 通过 `(from_account_id, to_account_id, relation_type)` 保持关注/拉黑关系唯一。
- `image_asset_favorite` 通过 `(account_id, asset_id)` 保持用户收藏图片唯一。
- `content_image_ref` 通过 `(asset_id, content_type, content_id)` 记录图片被内容引用的位置。
- `passkey_credential.account_id` 关联账号，`credential_id` 唯一。
- `credit_account.account_id` 关联账号，一行代表一个账号的 Credit 余额，`balance` 通过 CHECK 约束保证非负，业务层懒创建。
- `credit_transaction` 保存 Credit 变动流水，`delta` 为正表示发放、为负表示扣减，`balance_after` 记录变动后余额快照，`operator_id` 记录操作管理员账号。
- `shop_item` 保存商城商品，`item_type` 区分装饰类型（badge/avatar_frame/title），`item_key` 为唯一关键字（前端素材映射用，后端不存图片）；`stock = -1` 表示不限量，`status` 表达上架/下架；`decoration_id` 可空，绑定已发布装扮后用户端按 `decoration.published_config` 渲染，为空时前端回退 `item_key` 硬编码映射。
- `decoration` 保存低代码平台设计的装扮，`type` 复用装饰类型（badge/avatar_frame/title），`status` 表达 DRAFT/PUBLISHED/ARCHIVED 流转；`draft_config` 为编辑中的结构化 JSON 配置，发布时复制到 `published_config` 供用户端读取；`version` 为乐观锁版本号。
- `user_item` 保存用户背包，`uk_user_item` 保证同一用户同一装饰仅持有一件，`is_equipped` 标记装备状态，勋章/头像框/头衔同类型同时只能装备一件（业务层保证）。
- `shop_order` 保存购买记录，`price` 为成交单价快照，商品改价/下架/删除不影响历史订单；购买扣款通过 `credit_transaction` 的 `purchase` 变动类型入账。

## 索引与约束关注点

- 唯一约束用于防止重复关系，例如角色权限、图片收藏、用户关系、Passkey credential。
- `mention_message` 按账号和时间、来源类型和来源 ID 建索引，服务于消息分页和去重查询。
- `follow_message` 按账号和时间、发帖人和帖子 ID 建索引，服务于关注动态分页和排查。
- `conversation` 按双方账号与更新时间建索引，服务于会话列表排序。
- `conversation_message` 按会话、创建时间和消息 ID 建索引，服务于历史分页与最新摘要查询。
- `conversation_user_setting` 通过 `(conversation_id, account_id)` 保持单用户单会话设置唯一，并按账号、置顶状态和更新时间建索引。
- `credit_account` 通过 `chk_credit_account_balance` 约束保证余额非负，扣减时业务层使用行锁与余额校验防止透支。
- `credit_transaction` 按账号和时间、操作管理员和时间建索引，服务于用户端流水分页和管理端排查。
- `shop_item` 按状态和类型建索引，服务于用户端在售商品分页，`uk_shop_item_item_key` 保证商品关键字全局唯一。
- `decoration` 通过 `uk_decoration_key` 保证装扮关键字全局唯一，按状态和类型建索引，服务于管理端装扮分页。
- `user_item` 通过 `(account_id, item_id)` 保持用户持有唯一，并按账号和装备状态建索引，服务于个人页装饰展示。
- `shop_order` 按账号和时间、商品和时间建索引，服务于用户购买记录分页和管理端排查。
- 多数外键使用 `ON DELETE RESTRICT`，删除业务数据前需要先处理依赖数据。
- 部分图片和会话相关表使用 `ON DELETE CASCADE` 或 `ON DELETE SET NULL`，实现资源或账号删除后的引用处理。

## 状态与软删除

项目中多张表使用 `is_deleted`、`status`、`visibility`、`scope` 等字段表达业务状态。删除接口不一定意味着物理删除，具体行为应以对应 Service 实现为准。新增字段或状态枚举时，需要同步更新 Java 枚举、DTO/VO、Mapper SQL 和本文档。

## 变更流程

1. 修改完整 schema：更新 `.docker/image/mysql/nine_forum_schema.sql`，保证新环境可以一次性初始化。
2. 添加增量 SQL：将上线所需 DDL/DML 放入 `docs/sql`，文件名使用业务名或日期加业务名。
3. 更新模型：同步 POJO、DTO、VO、Mapper XML/接口和测试。
4. 更新文档：表分组、关系、索引、状态字段变化需要同步写入本文档。
