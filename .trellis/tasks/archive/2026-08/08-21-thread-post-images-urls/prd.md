# 主题与评论独立图片字段

## 目标

将主题（thread）和评论（post）的图片从 TipTap 正文中拆出，使用数据库字段 `images_urls` 持久化，并在用户端 API 以 `imageUrls: string[]` 返回。这样正文只承载富文本节点，图片以独立数组交付给前端。

## 背景与已确认事实

- `GET /api/threads/{threadId}` 返回 `ThreadVO`，其 `imageUrls` 已定义但 `ThreaddServiceImpl#getThreadById` 未赋值（`web/web-app/src/main/java/com/ayor/service/impl/ThreaddServiceImpl.java:185`）。
- `GET /api/threads/{threadId}/posts` 返回 `PostVO`，该 VO 目前没有 `imageUrls`（`web/web-app/src/main/java/com/ayor/entity/vo/PostVO.java:16`）。
- 当前主题和评论均将 TipTap `image` 节点中的 Base64 图片转换并写回正文（主题：`ThreaddServiceImpl.java:486`；评论：`PostServiceImpl.java:210`），读取时也从正文提取图片 URL。
- 图片资源引用由 `ImageAssetServiceImpl#syncContentRefs` 从 TipTap 正文提取 URL，因此改为独立字段后必须同步调整。
- 用户明确要求不兼容旧的 TipTap 图片节点格式；历史正文不回填到新字段。

## 范围

### 包含

1. 为 `thread` 和 `post` 增加 `images_urls` JSON 数组列，并在共享 POJO 中映射为 `List<String>`。
2. 为主题创建/编辑 DTO 和评论创建/编辑 DTO 增加 `imageUrls`；主题详情、主题列表以及评论列表项（包括 `replyTo`）返回该字段。
3. 新建或编辑主题/评论时拒绝任何包含 TipTap `image` 节点的正文；图片 URL 仅从独立字段保存。
4. 将图片资源引用同步改为消费独立 URL 列表，并保持主题最多 7 张图片的现有业务上限；评论不新增数量上限。
5. 更新新环境初始化 schema，并提供存量环境增量 SQL。增量 SQL 仅新增列，不从历史 TipTap 正文提取或回填图片。
6. 补充针对输入拒绝、持久化/投影和详情/评论列表响应的回归测试。

### 不包含

- 不迁移或继续读取历史正文中的 TipTap `image` 节点。
- 不改变现有独立图片上传 API、MinIO 存储策略或图片资源权限规则。
- 不为 `thread_edit_history`、`post_edit_history` 增加图片快照字段；本次仅变更当前主题和评论实体。
- 不修改前端代码或前端 TypeScript 类型；前端由调用方另行同步。

## 关键决策

- HTTP 字段使用既有 Java/Jackson 命名 `imageUrls`；数据库列使用用户指定的 `images_urls`。
- 新正文发现任意 `type=image` 即拒绝，而非丢弃、转换或从中回填，落实“不兼容旧格式”。
- `images_urls` 为 JSON 数组，空图片集合持久化为空数组；读取始终以数组返回。
- 保留主题的 7 张图片限制，将约束从 TipTap 节点数量平移至 `imageUrls`；评论保持现有无专门数量限制的行为。

## 验收标准

- [ ] `GET /api/threads/{threadId}` 的 `data.imageUrls` 等于该主题 `images_urls` 的完整有序数组；无图时为 `[]`。
- [ ] `GET /api/threads/{threadId}/posts` 的每个 `data.items[*].imageUrls` 等于该评论的完整有序数组；被回复评论对象存在时也携带该字段。
- [ ] 创建或编辑主题/评论提交的 `content` 含任意 TipTap `image` 节点时被拒绝，且不进行图片转换、数据库写入或图片引用同步。
- [ ] 创建和编辑从请求 `imageUrls` 保存、读取并同步图片资源引用；主题 8 张 URL 被拒绝，7 张及以下可保存。
- [ ] `.docker/image/mysql/nine_forum_schema.sql` 与 `.sql/20260821_thread_post_images_urls.sql` 均包含两个新列；增量 SQL 不依赖历史 TipTap 图片格式。
- [ ] 受影响 Maven 模块测试通过。

## 风险与回滚

- 发布前未执行增量 SQL 会导致新代码访问不存在的列；必须先执行迁移，再部署应用。
- 此变更有意不支持旧 TipTap 图片正文；若需回滚应用代码，可保留新增列，旧代码不会依赖它们。
