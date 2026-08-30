# Research: thread-post-upload-source-map

- Query: 定位 thread/post 的 DTO、写入端点、TipTap/Base64 图片处理、ImageStorageService、图片引用同步及测试落点。
- Scope: internal
- Date: 2026-08-22

## Findings

### HTTP 与 DTO 契约

- `web/web-app/src/main/java/com/ayor/controller/ThreadController.java:115-120`：`POST /api/threads/{thread_id}/posts` 接收并校验 `PostDTO`；`ThreadController.java:143-147`：`POST /api/threads` 接收 `ThreadDTO`；`ThreadController.java:153-157`：`PUT /api/threads/{thread_id}` 也使用 `ThreadDTO`。
- `web/web-app/src/main/java/com/ayor/controller/PostController.java:37-41`：评论编辑端点为 `PUT /api/posts/{post_id}`，接收 `PostEditDTO`。
- `web/web-app/src/main/java/com/ayor/entity/dto/ThreadDTO.java:14-33`、`PostDTO.java:14-27`、`PostEditDTO.java:13-21`：三种写 DTO 当前仅有 `imageUrls: List<String>`，应在三者增加 `images: List<Base64Upload>`。
- `model/src/main/java/com/ayor/entity/Base64Upload.java:11-16`：现成上传模型为 `base64` 与 `fileName` 两字段，可直接复用；模型模块适合继续承载该共享类型。

### TipTap 与图片存储

- `common/src/main/java/com/ayor/util/TipTapUtils.java:17-25`：`assertNoImageNodes` 递归拒绝任意 TipTap 图片节点，现有提示仍指向 `imageUrls`；本任务应同步改为指向 `images`。
- `web/web-app/src/main/java/com/ayor/service/impl/ThreaddServiceImpl.java:475-482,520-527`：主题创建、编辑先校验 TipTap，再按 `imageUrls` 数量检查 7 张上限；新实现必须以 `imageUrls.size() + images.size()` 在上传前校验。
- `web/web-app/src/main/java/com/ayor/service/impl/PostServiceImpl.java:191-194,288-291`：评论创建、编辑已经在写库和快照之前拒绝 TipTap 图片节点；新图片上传必须位于所有可失败的输入/权限/关联校验之后。
- `common/src/main/java/com/ayor/image/ImageStorageService.java:42-46`：`storeImageBase64Image(upload, path)` 调用 `ImageProcessor#processImage` 并上传 MinIO，返回 URL。`ImageProcessor.java:94-116` 验证 Base64、格式、10 MiB 上限和可解码性。
- `common/src/main/java/com/ayor/image/ImageProcessor.java:38-40,94-116`：正文图片允许 jpg/jpeg/png/webp/gif，静态格式原样透传；该路径不是 WebP 转码。仅贴纸路径 `processSticker` 会统一转 WebP（`ImageProcessor.java:60-84`）。

### 服务写入与引用同步

- `web/web-app/src/main/java/com/ayor/service/impl/ThreaddServiceImpl.java:55-84` 与 `PostServiceImpl.java:47-75`：两类服务均为 `@Transactional` + Lombok 构造器注入；新增 `ImageStorageService` 依赖会要求同步调整单测的显式构造器。
- `ThreaddServiceImpl.java:487-503` 与 `:536-561`：主题分别在保存/更新后调用 `ImageAssetService#syncContentRefs`，编辑会先写历史快照；最终 URL 数组应在此之前组装并赋给实体。
- `PostServiceImpl.java:195-233` 与 `:294-311`：评论的对应写入、快照、引用同步和消息/索引副作用位置。
- `web/web-app/src/main/java/com/ayor/service/impl/ImageAssetServiceImpl.java:156-174`：同步会删除旧引用、按 URL 去重处理、忽略不可归一化的平台外 URL，然后刷新使用计数。最终数组可保留显示顺序和重复值，但引用层不会重复建立记录。
- `ImageAssetServiceImpl.java:183-199`：只有 `MinioService#normalizeUrl` 成功的平台 URL 才会被登记为 `ImageAsset` / 引用。

### 读取模型与测试

- `ThreaddServiceImpl.java:214,266` 与 `PostServiceImpl.java:164-168`：VO 已从实体 `imagesUrls` 复制数组，无需改读路径。
- `web/web-app/src/test/java/com/ayor/service/impl/ThreaddServiceImplTest.java:442-503,616-657`：覆盖主题空数组、0/1/7 张、8 张拒绝、TipTap 图片节点拒绝及编辑副作用；应扩展为保留 URL + 新上传 URL 的顺序、总数上限和上传未发生断言。
- `web/web-app/src/test/java/com/ayor/service/impl/PostServiceImplTest.java:230-249,451-460,495-522`：覆盖评论 URL 持久化/引用、创建和编辑的 TipTap 图片节点拒绝；应增加创建/编辑上传、保留顺序和失败时不调用存储服务。
- `common/src/test/java/com/ayor/image/ImageStorageServiceTest.java:48-58`：已有正文图片 Base64 存储用例；不需要新建底层转换能力。
- `common/src/test/java/com/ayor/util/TipTapUtilsTest.java:105-109`：断言当前提示文本，修改指引后需同步预期。

## Related Specs

- `.trellis/spec/backend/thread-content-images.md`：现有独立 `imageUrls` 合同；应更新为“`imageUrls` 保留清单 + `images` 新上传列表”，并保持 TipTap 禁图和读模型合同。
- `.trellis/spec/backend/directory-structure.md`：共享 Base64 类型继续放 `model`，HTTP/服务编排留在 `web-app`，通用存储留在 `common`。
- `.trellis/spec/backend/error-handling.md`：控制器仍返回 `Result<T>`，不应在控制器自行吞掉存储异常。

## Caveats / Not Found

- 全仓当前未找到将 TipTap `image` 节点解析为 `Base64Upload`、上传后回写正文的实现；PRD 所称“旧实现”不在当前工作树。
- `ImageStorageService#storeImageBase64Image` 会在数据库写入前执行外部 MinIO 写入，Spring 事务不能自动回滚该对象；现有 `MinioService#deleteFile` 位于 `common/src/main/java/com/ayor/minio/MinioService.java:257-263`。需求明确的“校验失败不上传”可通过先完成全部确定性校验达成；若还要保证数据库失败绝不遗留对象，需额外定义补偿删除策略。
- 需求所称“转换”与现有正文路径不完全一致：正文图片当前验证并原样存储，只有贴纸会转 WebP。实现前应明确是复用正文路径，还是有意更改正文文件格式策略。
