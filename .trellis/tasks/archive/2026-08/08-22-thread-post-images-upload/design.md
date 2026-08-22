# 技术设计：主题与评论独立图片上传

## 边界与数据流

本次只调整用户端主题和评论的写入请求；读取模型、`images_urls` 数据库列、POJO 与图片引用模型不变。

```text
HTTP DTO
  imageUrls: 已上传、要保留的 URL（可空）
  images: 新的 Base64Upload 数组（可空）
        │
        ▼
Service：完成权限、关联、TipTap、数量等确定性校验
        │
        ▼
ImageStorageService：依次上传 images，得到最终 URL
        │
        ▼
最终 imagesUrls = imageUrls + uploadedUrls
        │
        ├─► Threadd/Post.images_urls
        └─► ImageAssetService.syncContentRefs(..., finalUrls, ...)
        │
        ▼
ThreadVO/PostVO：继续仅返回 imageUrls
```

`imageUrls` 保留原数组内顺序，`images` 上传结果保留提交顺序，并固定追加在其后。两数组不支持新旧图片任意交错排序；该能力不在本次范围。

## 请求合同

`ThreadDTO`、`PostDTO` 与 `PostEditDTO` 均增加 `List<Base64Upload> images`，保留现有 `List<String> imageUrls`：

- `imageUrls`：编辑时需要保留的、此前已上传的 URL；`null` 等同于空数组，故省略即不保留旧图。
- `images`：本次要上传的图片，元素为 `{ base64, fileName }`；`null` 等同于空数组。
- 最终数组只持久化 URL，响应不会暴露 `images` 或 Base64。
- TipTap `content` 仍必须是合法 `doc` JSON，且任何深度都不能含 `type=image`；提示改为使用 `images` 字段。

主题创建、编辑以 `imageUrls.size() + images.size()` 执行最多 7 张的校验；评论不设图片数量上限。

## 服务编排

在 `ThreaddServiceImpl` 与 `PostServiceImpl` 注入 `ImageStorageService`，通过一个复用的批量存储能力按顺序调用既有 `storeImageBase64Image(upload, path)`。主题使用 `threads/{topicId}/`，评论使用 `posts/{threadId}/`，与历史正文图片路径保持一致。

每条写入链路必须先完成全部确定性校验，再执行上传：

- 主题：用户、TipTap、图片总数、标签归属。
- 评论：请求字段、TipTap、用户、主题存在性、拉黑关系、回复对象。

上传成功后才设置最终 URL 数组；保存/更新成功后以同一数组同步资源引用，随后保持原有的快照、消息、缓存和索引动作顺序。图片处理复用当前正文图片存储策略（格式校验、大小校验和存储 URL 生成），不引入 GIF/WebP 策略变更。

## 失败、事务与兼容性

- 总数超限、TipTap 图片节点、权限或关联校验失败发生在上传前，因此没有上传、持久化、快照或后续副作用。
- 图片解析或对象存储失败沿用现有 `ImageStorageService` 异常语义，停止写入和后续副作用；此前已上传的对象不由 Spring 数据库事务回滚。本次不新增 MinIO 补偿删除，以维持现有存储行为。
- `imageUrls` 中的外部 URL 仍可持久化，但资源引用同步会按现有规则忽略不能归一化的平台外 URL。
- 无 schema 迁移；旧客户端若只发送 `imageUrls` 仍保持写入兼容，升级客户端可同时发送两字段。

## 测试与文档

- 扩展主题与评论服务测试，mock `ImageStorageService`，断言上传路径、最终 URL 合并顺序、同步参数和更新快照顺序。
- 回归 7/8 张总数边界、正文图片拒绝以及权限/关联失败前不调用存储服务。
- 更新 `TipTapUtilsTest` 的字段提示，及 `thread-content-images.md` 的写入合同、校验矩阵和示例。

研究依据：`research/upload-contract.md`、`research/thread-post-upload-source-map.md`。
