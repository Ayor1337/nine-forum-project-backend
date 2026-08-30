# 实施计划：主题与评论独立图片字段

## 实施顺序

1. 新增共享 JSON 列表 TypeHandler；在 `Threadd`、`Post` 增加 `imagesUrls` 映射。
2. 更新 `ThreadDTO`、`PostDTO`、`PostEditDTO`、`PostVO`，并核对各 `BeanUtils` 投影路径。
3. 将 `TipTapUtils` 的图片行为改为仅检测并拒绝输入图片节点；移除主题/评论写入路径的 Base64 转换和旧图片解析依赖。
4. 将 `ImageAssetService` 与实现的内容引用同步参数改为 URL 列表，并更新主题、评论的创建、编辑、删除调用。
5. 更新主题详情、主题各列表/收藏/点赞投影和评论列表投影，使其读取实体 `imagesUrls`。
6. 更新初始化 schema，新增 `.sql/20260821_thread_post_images_urls.sql` 增量迁移。
7. 更新/新增单元测试，执行 Maven 测试并检查完整 diff。

## 风险点与检查

- 所有 `syncContentRefs` 调用必须改为新签名；遗漏会导致图片资源引用计数失真。
- 所有从 Tiptap 提取图片的 ThreadVO 投影必须改读新字段；遗漏会让列表与详情结果不一致。
- JSON TypeHandler 必须将数据库 `NULL` 规范化为安全的空列表，防止返回 `null` 与 API 合同冲突。
- 增量 SQL 需可在当前 MySQL 上执行一次；部署顺序为 SQL 在前、应用在后。

## 验证

```powershell
.\mvnw.cmd -pl web/web-app -am test
git diff --check
git diff -- .docker/image/mysql/nine_forum_schema.sql .sql model web/web-app common
```

## 回滚点

- 代码问题：回滚应用提交，保留新增列。
- 数据库问题：仅在确认新字段未被写入生产数据时，手工执行反向 `DROP COLUMN images_urls`；不纳入自动迁移。
