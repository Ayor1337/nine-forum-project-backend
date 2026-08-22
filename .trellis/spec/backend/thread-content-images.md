# 主题与评论独立图片契约

## 1. 范围

本规范适用于用户端主题（`thread`）与评论（`post`）的创建、编辑、读取投影和图片资源引用。图片不属于 TipTap 正文：正文只保存富文本节点，图片 URL 只保存于数据库 JSON 列 `images_urls`。

适用接口包括主题详情与列表、用户收藏/点赞列表，以及 `GET /api/threads/{threadId}/posts` 的评论及其嵌套 `replyTo`。头像、横幅、贴纸和编辑历史快照不在本合同内。

## 2. 签名

- 主题写入：`ThreaddService#insertThread(ThreadDTO, Integer)`、`ThreaddService#editThread(Integer, ThreadDTO, Integer)`
- 评论写入：`PostService#insertPost(PostDTO, Integer)`、`PostService#editPost(Integer, PostEditDTO, Integer)`
- TipTap 校验：`TipTapUtils#assertNoImageNodes(String)`；列表文本摘要仅可使用 `TipTapUtils#filterStickerNodes(String)` 处理贴纸。
- Base64 上传：`ImageStorageService#storeImageBase64Images(List, String)`。
- 图片引用同步：`ImageAssetService#syncContentRefs(String, Integer, List<String>, Integer)`
- 主题读取：`ThreaddServiceImpl#getThreadById`、`ThreaddServiceImpl#toVOs`、`CollectServiceImpl#toVO`、`LikeThreadServiceImpl#getLikesByAccountId`
- 评论读取：`PostServiceImpl#toPostVO`

## 3. 合同

- 写入 DTO 使用 `imageUrls: List<String>` 与 `images: List<Base64Upload>`：前者是编辑时需要保留的既有 URL，后者是本次需上传的新图；读取 DTO/VO/POJO 只使用最终的 `imageUrls: List<String>`。数据库列使用 `images_urls JSON NOT NULL DEFAULT (JSON_ARRAY())`。
- `Threadd.imagesUrls` 与 `Post.imagesUrls` 必须使用 `StringListTypeHandler` 映射；读取到 SQL `NULL` 或空值时规范化为 `[]`。
- 创建或编辑时，`content` 必须是合法 TipTap `doc` JSON，且任意深度均不得有 `type=image` 节点。
- TipTap 工具不得保留图片节点的提取、计数、丢弃或占位转换 API；贴纸文本摘要不得处理图片节点。
- `imageUrls` 与 `images` 为 null 时均视为空数组。服务端在全部确定性校验通过后，按 `imageUrls` 原有顺序保留旧图，再按 `images` 请求顺序上传新图，持久化合并后的最终 URL 数组。主题以两数组总数执行最多 7 项的限制；评论不设置数量上限。
- 主题图片上传路径为 `threads/{topicId}/`，评论图片上传路径为 `posts/{threadId}/`；上传沿用正文图片格式、体积与异常策略，不改变 GIF/WebP 规则。
- 主题详情和所有已携带 `ThreadVO` 的列表，以及评论列表项与 `replyTo`，必须从实体 `imagesUrls` 返回完整、有序的 `imageUrls`，不得从 TipTap 正文提取、转换或回填。
- 保存成功后以独立 URL 数组调用 `syncContentRefs`；仅可归一化的平台内 URL 建立图片资源引用。删除主题/评论仍须清除对应引用。
- 不迁移、不解析、不回填历史 TipTap `image` 节点；旧正文不是 `images_urls` 的数据来源。

## 4. 校验矩阵

| 条件 | 结果 |
| --- | --- |
| 合法 TipTap doc、主题 `imageUrls + images` 共 0–7 项 | 允许写入，上传新图并同步最终 URL 数组引用 |
| 合法 TipTap doc、主题 `imageUrls + images` 共 8 个及以上 | 返回 `帖子最多只能包含7张图片`，不得上传、写入、创建快照或产生后续副作用 |
| 合法 TipTap doc、评论任意数量 URL/新图 | 上传新图、写入并同步最终 URL 数组引用 |
| 正文任意层级存在 `type=image` | 返回 `TipTap 内容不支持图片节点，请使用 images`，不得写入、创建快照、上传或同步引用 |
| TipTap JSON 非法 | 返回既有 `TipTapUtils` 格式错误消息 |
| `imageUrls`、`images` 均省略、为 `null` 或为空 | 保存和返回空数组 |

## 5. 案例

- Good：主题正文仅含段落与 sticker，`imageUrls` 含 5 个既有 URL、`images` 含 2 个新图；主题详情按“5 个旧 URL、2 个新 URL”返回。
- Good：评论引用另一条评论；当前评论和 `replyTo` 分别返回各自的独立图片数组。
- Base：主题或评论不传 `imageUrls`、`images`；保存后 API 返回 `[]`。
- Bad：正文任意嵌套层含 image（无论 `src` 是 Base64、普通 URL 或缺失）；在数据库和图片引用副作用前被拒绝。
- Migration：存量正文含 image 节点时，不回填 `images_urls`；执行增量 SQL 后该列默认为 `[]`。

## 6. 测试

- `TipTapUtilsTest`：无图片的 doc 通过；任意深度 image 节点被拒绝；非法 doc 保持既有报错。
- `ImageStorageServiceTest`：批量上传保持输入顺序并复用正文图片处理。
- `ThreaddServiceImplTest`：主题创建/编辑合并旧 URL 与新上传 URL、7/8 项总数边界、图片节点拒绝且无上传副作用、详情与列表投影数组。
- `PostServiceImplTest`：评论创建/编辑合并旧 URL 与新上传 URL、图片节点拒绝且无上传副作用、列表与嵌套 `replyTo` 投影数组。
- `ImageAssetServiceImplTest`：引用同步接受 URL 数组，不从正文提取 URL。
- 结构变更必须同步 `.docker/image/mysql/nine_forum_schema.sql` 与 `.sql/YYYYMMDD_<feature>.sql`；运行 `./mvnw.cmd -pl web/web-app -am test`。

## 7. 错误与正确示例

### 错误

```java
// 错误：重新从 TipTap 正文推导图片，绕过独立字段合同。
List<String> urls = parseImageUrlsFromTipTap(thread.getContent());
imageAssetService.syncContentRefs("THREAD", threadId, urls, accountId);
```

### 正确

```java
// 正确：输入拒绝正文图片；保留 URL 与新上传图片合并后是唯一持久化、响应和引用来源。
tipTapUtils.assertNoImageNodes(threadDTO.getContent());
List<String> imageUrls = new ArrayList<>(threadDTO.getImageUrls() == null ? List.of() : threadDTO.getImageUrls());
imageUrls.addAll(imageStorageService.storeImageBase64Images(threadDTO.getImages(), "threads/" + threadDTO.getTopicId() + "/"));
thread.setImagesUrls(imageUrls);
imageAssetService.syncContentRefs("THREAD", thread.getThreadId(), thread.getImagesUrls(), accountId);
threadVO.setImageUrls(new ArrayList<>(thread.getImagesUrls()));
```
