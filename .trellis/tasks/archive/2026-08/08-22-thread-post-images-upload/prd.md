# 调整主题与帖子图片上传接口

## Goal

主题（thread）与评论（post）的创建、编辑接口不再要求客户端先上传图片 URL；改为通过独立 `images` 数组提交图片，在服务端沿用既有 Base64 图片存储与格式转换流程，将最终 URL 写入现有 `images_urls`，从而保持 TipTap 正文不含图片节点。

## Confirmed Facts

- 用户端写入端点为 `POST /api/threads`、`PUT /api/threads/{thread_id}`、`POST /api/threads/{thread_id}/posts` 与现有评论编辑端点；统一响应仍为 `Result<T>`。
- 当前 `ThreadDTO`、`PostDTO`、`PostEditDTO` 只接收 `imageUrls: List<String>`，服务直接写入 JSON 列并同步图片资源引用。
- `TipTapUtils#assertNoImageNodes` 已在主题和评论创建、编辑前执行；正文出现任意深度 `image` 节点会被拒绝。
- 旧实现曾在 TipTap 内递归识别 Base64 `data:image/...`，调用 `ImageStorageService#storeImageBase64Image(...)` 上传并转换为 URL；非 Base64 URL 保持原样。
- 数据库、POJO 与读模型均已使用 `images_urls` / `imageUrls`；主题至多 7 张，评论没有数量上限。

## Requirements

- R1：主题和评论的创建、编辑请求保留 `imageUrls: string[]` 作为已有图片的保留清单，并新增 `images: Base64Upload[]` 接收待上传图片；TipTap `content` 继续禁止图片节点。
- R2：服务端对 `images` 中的新 Base64 图片复用现有图片存储和格式转换能力；按 `imageUrls` 原有顺序后追加 `images` 的上传结果，得到最终 URL 数组并写入现有 `images_urls`。
- R3：保存成功后继续以最终 URL 数组同步 `content_image_ref`；读取响应继续返回 `imageUrls: string[]`，不将 Base64 回显给客户端。
- R4：主题保留最多 7 张图片限制；创建、编辑的确定性校验失败不得产生上传、写库、快照、引用同步或消息/索引副作用。
- R5：更新主题、评论、公共 TipTap 提示与测试，使接口字段名称和错误指引一致。

## Out of Scope

- 不恢复 TipTap 图片节点支持，也不从正文回填历史图片。
- 不调整数据库表、图片资源引用模型、独立图片上传接口、头像/横幅/贴纸流程。
- 不修改管理端接口，除非全仓使用同一用户端 DTO 的证据表明必须同步。

## Acceptance Criteria

- [ ] AC1：四个用户端主题/评论写入流程可接受 `imageUrls` 保留清单与 `images` 上传数组，并将“保留 URL 在前、新上传 URL 在后”的最终数组有序持久化到 `images_urls`。
- [ ] AC2：任意写入请求中 TipTap 正文含 `image` 节点均被拒绝，且不产生上传或其他写入副作用。
- [ ] AC3：主题 7/8 张边界与评论多图场景按既有规则可验证；总数超限、正文图片节点等确定性失败不会产生副作用。
- [ ] AC4：创建、编辑后的详情与评论读取返回最终 `imageUrls`，并正确同步图片引用。
- [ ] AC5：受影响模块测试覆盖上传成功、已有图片保留、确定性失败无副作用与输入契约。
