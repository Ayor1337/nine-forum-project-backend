# 帖子正文图片契约

## 1. Scope / Trigger

本规范适用于用户端创建、编辑和列表读取 thread 正文图片的流程。评论/回复（post）、sticker、头像、横幅及独立图片资产上传不属于此合同。

该规则必须同时约束 Base64 和普通 URL 图片，避免客户端先上传图片再提交 URL 绕过数量限制。

## 2. Signatures

- 创建：`ThreaddService#insertThread(ThreadDTO threadDTO, Integer accountId)`
- 编辑：`ThreaddService#editThread(Integer threadId, ThreadDTO threadDTO, Integer accountId)`
- 图片计数：`TipTapUtils#countImageNodes(String content)`
- 完整投影：`TipTapUtils#extractAllImageUrls(String content)`
- 主要列表投影：`ThreaddServiceImpl#toVOs(List<Threadd> threads)`
- 收藏列表投影：`CollectServiceImpl#toVO(Threadd thread)`

## 3. Contracts

- 输入 `ThreadDTO.content` 必须是合法的 TipTap `doc` JSON。
- 单个 thread 最多包含 7 个 `type=image` 节点；节点 `src` 是 Base64、URL 或重复 URL 都计数。
- `type=sticker` 不计入图片数量。
- 创建和编辑必须在 `convertBase64ImagesToUrl(...)`、数据库写入、编辑历史及消息/缓存/索引副作用之前完成数量校验。
- 主题帖子列表、主题/全站排行榜、用户帖子列表和收藏列表的 `ThreadVO.imageUrls` 必须返回正文内全部非空图片 URL，保持 TipTap 文档顺序且不去重。
- 返回层不得按 7 张裁剪：历史超限内容仍需完整读取；写入层负责限制新提交正文。

## 4. Validation & Error Matrix

| 条件 | 结果 |
| --- | --- |
| 0–7 个 `image` 节点 | 允许继续创建或编辑 |
| 8 个及以上 `image` 节点 | 返回 `帖子最多只能包含7张图片` |
| TipTap JSON 非法 | 返回 `TipTapUtils` 的现有格式错误消息 |
| 超限正文包含 Base64 图片 | 拒绝且不得调用图片存储 |
| 编辑历史帖子但最终正文仍超过 7 张 | 拒绝编辑；读取不受影响 |

业务失败继续沿用 Service 返回错误字符串、Controller 使用 `Result.messageHandler(...)` 的现有响应合同。

## 5. Good / Base / Bad Cases

- Good：正文包含 7 个 `image` 和任意数量 `sticker`，允许提交并返回 7 个图片 URL。
- Base：正文没有图片，列表返回空 `imageUrls`。
- Bad：正文包含 8 个 URL/Base64 混合的 `image`，在第一张 Base64 上传前拒绝。
- Compatibility：历史正文有 8 张图片时列表返回 8 个 URL；再次编辑需将最终正文降至 7 张以内。

## 6. Tests Required

- `TipTapUtilsTest`：断言 0、7、8 张边界；URL/Base64 均计数；重复节点保留；sticker 不计数；全部 URL 顺序不变。
- `ThreaddServiceImplTest`：断言创建和编辑接受 7 张、拒绝 8 张；拒绝时图片存储、Mapper、编辑历史、图片引用、消息、缓存、实时事件和索引均无副作用。
- `ThreaddServiceImplTest` 与 `CollectServiceImplTest`：使用历史 8 张正文断言列表返回 8 个 URL，证明返回层未截断。
- 受影响模块验证：`.\mvnw.cmd -pl web/web-app -am clean test`。

## 7. Wrong vs Correct

### Wrong

```java
// 只统计本次 Base64 上传，URL 图片可绕过；返回层也会截断历史内容。
threadVO.setImageUrls(tipTapUtils.extractImageUrls(thread.getContent()));
String converted = tipTapUtils.convertBase64ImagesToUrl(content, path);
```

### Correct

```java
// 先统计最终正文的全部 image 节点，再允许上传；读取始终完整投影。
if (tipTapUtils.countImageNodes(content) > MAX_THREAD_IMAGE_COUNT) {
    return THREAD_IMAGE_LIMIT_ERROR;
}
threadVO.setImageUrls(tipTapUtils.extractAllImageUrls(thread.getContent()));
```
