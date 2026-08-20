# 限制帖子图片数量并完整返回

## Goal

限制单个帖子正文最多包含 7 张图片，避免一次创建或编辑帖子时上传过多内容；帖子列表不再只返回前 3 张预览图，而是返回该帖子正文中的全部图片 URL。

## Background

- `ThreadDTO.content` 接收 TipTap JSON，目前只校验非空，没有图片数量限制：`web/web-app/src/main/java/com/ayor/entity/dto/ThreadDTO.java:18`。
- 创建和编辑帖子都会在保存前调用 `TipTapUtils.convertBase64ImagesToUrl(...)`，现有流程会逐张上传 Base64 图片：`web/web-app/src/main/java/com/ayor/service/impl/ThreaddServiceImpl.java:479`、`:520`。
- threads 列表通过 `TipTapUtils.extractImageUrls(...)` 填充 `ThreadVO.imageUrls`：`web/web-app/src/main/java/com/ayor/service/impl/ThreaddServiceImpl.java:263`。
- `extractImageUrls(...)` 当前硬编码最多提取 3 个 URL，而 `extractAllImageUrls(...)` 已具备返回全部图片 URL 的能力：`common/src/main/java/com/ayor/util/TipTapUtils.java:77`、`:89`。
- 收藏帖子列表复用了同一套最多 3 张的提取逻辑；点赞帖子列表当前未填充 `imageUrls`。

## Requirements

### R1：帖子图片数量限制

- 创建帖子和编辑帖子时，最终提交的 TipTap 正文最多允许 7 个 `type=image` 节点。
- 数量校验必须覆盖 Base64 图片和普通 URL 图片，不能通过预先上传后提交 URL 绕过。
- 第 7 张图片允许通过；第 8 张及以上必须拒绝。
- 校验必须发生在上传 Base64 图片及写入数据库之前，拒绝时不得上传部分图片、创建/更新帖子、写编辑历史或触发后续同步副作用。
- 已确认：`type=sticker` 节点不计入帖子图片数量。
- 创建和编辑使用同一数量规则及同一错误消息，避免行为漂移。

### R2：返回全部帖子图片

- 主题帖子列表、主题排行榜、全站排行榜和用户发帖列表中的每个 `ThreadVO.imageUrls` 返回正文内全部有效图片 URL，并保持 TipTap 文档顺序。
- 收藏帖子列表同样返回正文内全部有效图片 URL。
- 不去重：同一 URL 在正文中出现多次时，返回结果保持正文节点数量和顺序。
- 没有图片时返回空列表。

### R3：兼容性

- 不迁移、不裁剪已存在的帖子正文。
- 历史上超过 7 张图片的帖子仍可读取，列表应返回其全部图片 URL；但再次编辑时，提交后的最终正文必须降至 7 张以内。
- 帖子详情接口继续以完整 `content` 返回正文；本任务不改变详情接口现有 `imageUrls` 填充行为。

## Acceptance Criteria

- [x] AC1：创建包含 0、1、7 张图片的帖子可成功。
- [x] AC2：创建包含 8 张图片的帖子返回明确错误，且没有调用图片上传、帖子保存和保存后的同步副作用。
- [x] AC3：编辑后的正文包含 7 张图片可成功；包含 8 张图片时被拒绝，且没有上传图片、写编辑历史或更新帖子。
- [x] AC4：图片数量统计同时包含 Base64 `src` 和普通 URL `src` 的 `image` 节点，`sticker` 节点不计数。
- [x] AC5：主要 threads 列表和收藏列表对包含 7 张图片的帖子返回 7 个 `imageUrls`，顺序与正文一致。
- [x] AC6：读取历史上超过 7 张图片的帖子列表时，返回全部图片 URL，不静默截断。
- [x] AC7：无图片帖子返回空 `imageUrls`，重复 URL 不被去重。
- [x] AC8：受影响模块测试通过：`.\mvnw.cmd -pl web/web-app -am test`。

## Out of Scope

- 不限制评论/回复（post）正文的图片数量。
- 不把 sticker 计入 7 张图片限制。
- 不迁移或删除历史帖子中的图片。
- 不修改独立图片资产上传、头像、横幅或表情包上传接口。
- 不补齐点赞帖子列表当前缺失的 `imageUrls`；该行为可另开一致性任务处理。
