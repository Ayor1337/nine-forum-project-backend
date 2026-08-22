# 技术设计：主题与评论独立图片字段

## 边界

最小行为缺口是：图片当前与富文本正文耦合，导致详情接口漏填主题图片、评论接口无图片字段，并使 Tiptap 继续接收图片节点。

变更在共享持久化模型、用户端 DTO/VO、主题/评论服务、图片资源引用服务和数据库 schema 之间完成；Controller 路由与统一 `Result<T>` 响应不变。

不改动管理端接口、图片上传能力、搜索文本投影或编辑历史快照结构。

## 数据模型与序列化

```text
HTTP imageUrls: string[]
        ↓ DTO / VO（List<String>）
POJO imagesUrls（List<String>，JSON TypeHandler）
        ↓ MyBatis-Plus
thread.images_urls / post.images_urls（MySQL JSON）
```

- 在 `model` 新增专用 `List<String>` JSON TypeHandler，并在 `Threadd`、`Post` 的 `imagesUrls` 字段上明确指定，避免依赖全局泛型推断。
- `ThreadDTO`、`PostDTO` 与 `PostEditDTO` 新增 `imageUrls`。编辑请求提供的数组完整替换当前数组；省略/空数组表示清空。
- `ThreadVO` 保持既有 `imageUrls` 字段，`PostVO` 新增同名字段。`BeanUtils.copyProperties` 会投影同名 `List<String>` 字段。

## 写入流程

```text
请求 DTO
  ├─ 校验 TipTap doc JSON 且不包含 image 节点
  ├─ 校验主题 imageUrls ≤ 7
  ├─ 保存 content + imagesUrls
  └─ syncContentRefs(内容类型、ID、imagesUrls、作者)
```

- 删除 Base64 图片转换调用，不再使用 Tiptap 正文作为图片载体。
- `TipTapUtils` 收敛为文档校验和图片节点检测；检测到 `type=image` 返回确定的业务错误。不会丢弃、替换或提取旧节点。
- `ImageAssetService#syncContentRefs` 的输入由正文 `String` 改为 `List<String>`，仍仅对可归一化的平台内 URL 建立引用并刷新计数。
- 当前主题列表、收藏列表、点赞列表等既有图片投影统一改读 `Threadd.imagesUrls`，不再解析正文，防止同一字段在不同接口产生不同来源。

## 读取流程

```text
数据库 images_urls → POJO imagesUrls → ThreadVO/PostVO imageUrls → API JSON
```

- 主题详情在现有 `BeanUtils.copyProperties` 后得到 `imageUrls`；必要时确保 null 规范化为空列表。
- `PostServiceImpl#toPostVO` 投影图片字段，因此分页评论及其 `replyTo` 都具有相同响应合同。

## 迁移与兼容性

1. 初始化 schema 在 `thread`、`post` 各加入 `images_urls JSON NOT NULL`，默认空 JSON 数组。
2. `.sql/20260821_thread_post_images_urls.sql` 为已部署数据库执行同样的 `ALTER TABLE`。
3. 不从 `content` 解析或回填历史图片节点；旧数据不能作为新字段来源，符合明确的不兼容要求。
4. 先执行 SQL 再部署应用。回滚应用时可保留列；若必须回滚 schema，须在确认无新数据依赖后人工删除列。

## 测试策略

- `TipTapUtilsTest`：普通文档通过，任意层级 `image` 节点被检测并拒绝。
- `ThreaddServiceImplTest`：创建/编辑保存独立 URL 列表、7/8 张边界、图片节点拒绝时无副作用、详情返回数组。
- `PostServiceImplTest`：创建/编辑保存列表、图片节点拒绝、评论列表及嵌套 `replyTo` 返回数组。
- 运行 `./mvnw.cmd -pl web/web-app -am test`。
