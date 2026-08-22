# Research: thread-post-images-upload-contract

- Query: 梳理用户端 thread/post 写入接口从 TipTap 内图片转为独立 `images` Base64 数组时的 DTO、端点、转换、资源引用、异常和测试合同。
- Scope: internal
- Date: 2026-08-22

## Findings

### 当前 API 与 DTO

- 主题创建、编辑分别是 `POST /api/threads` 与 `PUT /api/threads/{thread_id}`，请求体为 `ThreadDTO`，由 `Result.messageHandler` 将 Service 的 `null`/错误字符串映射为统一响应：`web/web-app/src/main/java/com/ayor/controller/ThreadController.java:143-157`。
- 评论创建为 `POST /api/threads/{thread_id}/posts`（Controller 以路径参数覆盖 `PostDTO.threadId`），评论编辑为 `PUT /api/posts/{post_id}`：`web/web-app/src/main/java/com/ayor/controller/ThreadController.java:113-120`、`web/web-app/src/main/java/com/ayor/controller/PostController.java:36-42`。
- 现有三个写 DTO 均只有 `imageUrls: List<String>`：`ThreadDTO.java:31`、`PostDTO.java:25`、`PostEditDTO.java:18`。可复用的图片提交元素是共享 model 的 `Base64Upload { base64, fileName }`：`model/src/main/java/com/ayor/entity/Base64Upload.java:11-15`。
- 已确认的编辑契约应落为：保留 `imageUrls` 承载要保留的既有最终 URL，新增 `images: List<Base64Upload>` 承载新图片；服务端把 `imageUrls`（空/null 规范化为 `[]`）与上传得到的 URL 合并后，写入既有 `images_urls`。读取 VO 仍只回传 `imageUrls`，不回传 Base64。

### 当前正文与存储链路

- 当前版本不存在 TipTap 图片提取、Base64 转换或 URL 回填代码；全仓搜索只发现 `TipTapUtils#assertNoImageNodes` 及独立 URL 逻辑。旧“递归识别 TipTap `data:image/...` 后上传”的事实只记录于本任务 `prd.md`，无法从当前工作树复原其具体实现或对象路径（本调研未执行 git 操作）。
- `TipTapUtils#assertNoImageNodes` 先解析合法的 `doc` 根节点，再递归任何深度寻找 `type=image`：`common/src/main/java/com/ayor/util/TipTapUtils.java:22-25,72-83,150-164`。两类 Service 都在任何数据库写入前调用并把 `IllegalArgumentException` 原样转换为错误字符串：`ThreaddServiceImpl.java:475-481,567-573`、`PostServiceImpl.java:191-194,250-256`。
- 当前提示为“`TipTap 内容不支持图片节点，请使用 imageUrls`”：`TipTapUtils.java:24`。R5 要求改为 `images` 后，Javadoc、实现、单测及 `thread-content-images.md` 的提示均须同改；不得保留提示与 HTTP 字段不一致的情况。
- `ImageStorageService#storeImageBase64Image(upload, path)` 是可直接复用的正文图片入口：它调用 `ImageProcessor#processImage`，生成 UUID 对象名，调用 `MinioService#uploadObject`，返回含最终 URL 和 objectName 的 `StoredImage`：`common/src/main/java/com/ayor/image/ImageStorageService.java:42-55`。
- `processImage` 允许 `jpg/jpeg/png/webp/gif`，保留原格式及字节，限制 Base64 解码后不超过 10 MiB，并验证可解码图片：`common/src/main/java/com/ayor/image/ImageProcessor.java:38-44,94-116,205-235`。典型异常为：空内容“图片内容不能为空”、不支持格式“仅支持 jpg、jpeg、png、webp 静态图片，禁止 GIF 或其他动图”、超限“图片体积过大”、非法 Base64“图片内容不是合法的 Base64 数据”；对象存储失败则为 `IllegalStateException("上传图片到对象存储失败", cause)`：`ImageProcessor.java:95-103,161-169,205-235`、`MinioService.java:100-113`。
- 现有主题、评论服务将输入 URL 直接复制到实体，再保存成功后调用资源引用同步：`ThreaddServiceImpl.java:479-500,524-557`、`PostServiceImpl.java:215-231,288-310`。两服务都标注 `@Transactional`：`ThreaddServiceImpl.java:54-57`、`PostServiceImpl.java:47-50`。

### 推荐的可执行写入顺序

1. 先完成所有非上传校验：主体 TipTap 校验、主题“保留 URL 数量 + 新 `images` 数量”不超过 7、主题标签校验；评论还要完成作者/帖子/拉黑/replyTo 校验。这样任何可预知业务失败都不会上传对象。
2. 逐项、按 `images` 的数组顺序调用 `storeImageBase64Image`，收集 `StoredImage.url`；与 `imageUrls` 合并为最终有序数组。建议把“null 归一化、总数量校验、逐项上传、返回最终 URL”抽成一个共用协作者/方法，供 thread 和 post 复用；不要将转换重新塞回 `TipTapUtils`。
3. 仅使用该最终数组设置 `Threadd.imagesUrls` / `Post.imagesUrls`，持久化成功后将同一数组传入 `syncContentRefs`；不可让读取层或 TipTap 再次推导图片。
4. 保持现有读取投影：主题详情/列表从实体复制 `imagesUrls`（`ThreaddServiceImpl.java:193-222,249-273`），评论及嵌套 `replyTo` 均通过 `toPostVO` 复制数组（`PostServiceImpl.java:121-173`）。

### 图片引用与最终 URL 约束

- `syncContentRefs(contentType, contentId, imageUrls, accountId)` 会先删旧引用，再以 `LinkedHashSet` 对 URL 去重、逐项摄取，最后刷新旧/新资源的使用计数：`web/web-app/src/main/java/com/ayor/service/impl/ImageAssetServiceImpl.java:156-174`。因此 `images_urls` 可保留展示顺序和重复值，但 `content_image_ref` 天然无重复且不保存顺序。
- 只有 `MinioService#normalizeUrl` 能识别的平台内 URL 才会建立资源引用；接受 `endpoint/bucket/object`、`bucket/object` 或 `/bucket/object`，并规范化为 `bucket/object`。空白与外部 URL 返回 `null`，`syncContentRefs` 会静默跳过：`common/src/main/java/com/ayor/minio/MinioService.java:144-193`、`ImageAssetServiceImpl.java:183-199`。
- `storeImageBase64Image` 经 `uploadObject` 产出的最终 URL 是 `bucketName/objectName`，故天然满足引用同步约束：`MinioService.java:100-113,138-145`。保留的 `imageUrls` 应只传此前服务端返回的平台 URL；当前实现虽不拒绝外部 URL，但它们会被持久化而不会有 `content_image_ref`。

### 可扩展测试点

- 主题现有 Mockito 单测已覆盖创建/编辑的 7 张、8 张拒绝、TipTap 图片节点拒绝、保存后同步与无副作用，直接扩展 `ThreaddServiceImplTest`：`web/web-app/src/test/java/com/ayor/service/impl/ThreaddServiceImplTest.java:440-501,615-657,843-866`。
- 评论测试已覆盖创建/编辑的独立 URL、同步、TipTap 图片节点拒绝、快照顺序及嵌套 `replyTo` 投影，直接扩展 `PostServiceImplTest`：`web/web-app/src/test/java/com/ayor/service/impl/PostServiceImplTest.java:227-252,449-522,635-652`。
- `ImageStorageServiceTest` 可验证批量协作者逐项使用正文模式、对象前缀与 URL 顺序：`common/src/test/java/com/ayor/image/ImageStorageServiceTest.java:49-59`。`ImageAssetServiceImplTest` 已验证重复 URL 引用只插入一次：`web/web-app/src/test/java/com/ayor/service/impl/ImageAssetServiceImplTest.java:148-165`。
- 需要新增/改写断言：主题 7 项应按“保留 URL + 新图”总数计；8 项时 `ImageStorageService`、Mapper、快照、引用和消息/索引均不得调用；TipTap image 节点时同样不得调用上传；成功时捕获实体与 `syncContentRefs` 参数，断言为上传后的最终 URL、有序且无 Base64；编辑同时验证已有 URL 被保留、新 URL 被追加。

## Related Specs

- `.trellis/spec/backend/thread-content-images.md`：当前独立图片存储、读取投影、无正文 image 节点与同步引用的主合同；实现后应将请求字段/提示从 `imageUrls` 更新为“`imageUrls` 保留项 + `images` 新上传项”。
- `.trellis/spec/backend/error-handling.md`：Controller 继续以 `Result<T>` 与 Service 错误字符串表达业务校验失败。
- `.trellis/spec/backend/quality-guidelines.md`：要求 Service 副作用测试及受影响模块 `./mvnw.cmd -pl web/web-app -am test` 验证。

## Caveats / Not Found

- 两个独立数组无法表示“旧图与新图任意交错”的全局顺序；当前确认的合并语义只能明确为 `imageUrls` 在前、`images` 上传结果在后（各自保留数组内顺序）。若产品需要任意排序，必须改成带类型的单一数组或额外排序字段，不能凭两数组推断。
- 现有 `@Transactional` 只能回滚数据库。若第 N 张对象上传成功后第 N+1 张上传失败，或上传成功后数据库/引用同步失败，已写入 MinIO 的对象不会自动删除；`MinioService#deleteFile` 可删对象（`MinioService.java:251-263`），但当前写链路没有补偿。R4 的“校验失败无上传”可通过先校验实现；若验收将任何后续失败也视为“上传回滚”，实现必须显式记录 `StoredImage.objectName` 并补偿删除，且定义删除失败的日志/告警策略。
- 当前代码没有一个 thread/post 既定的对象路径前缀；公共单测使用 `content/`（`ImageStorageServiceTest.java:55`），其他业务按领域使用 `avatar/`、`topic/` 等。实施前需固定 `content/` 或分别的 `threads/`/`posts/` 前缀；该选择不影响 URL 引用合同，但不能从当前代码推断旧路径。
- 当前当前工作树中未找到旧 TipTap 内递归 Base64 转换源码；任务 PRD 是其唯一可见依据。也未找到 Controller 层对 DTO 图片字段的专门契约测试，主要覆盖应落在上述 Service 单测。
