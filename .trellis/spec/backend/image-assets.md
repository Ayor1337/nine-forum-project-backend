# 公开图片资产与表情包契约

## 场景：论坛图片资源的复用与下架

### 1. 范围 / 触发条件

本规范适用于共享实体 `ImageAsset`、用户端 `ImageAssetService` 及管理端图片资源状态管理。论坛内图片与 Sticker 都是公开、可复用的资源；上传者 `accountId` 只用于溯源，不参与读取或收藏授权。

当修改图片资源 schema、按 URL 复用逻辑、Sticker 收藏/详情或内容图片引用时，必须遵守本规范。

### 2. 签名

- 数据表：`image_asset` 不包含 `visibility` 列；实体 `ImageAsset` 不包含 `visibility` 属性或可见性枚举。
- 用户端：`ImageAssetService#addSticker(Integer, Integer)`、`getDetail(Integer, Integer)`、`addStickerByUrl(Integer, String)`、`syncContentRefs(String, Integer, List<String>, Integer)`。
- 管理端：`PUT /api/image-assets/{assetId}/status` 接受 `ACTIVE` 或 `DISABLED`；`DELETE /api/image-assets/{assetId}` 执行强制删除。
- 结构变更同时更新 `.docker/image/mysql/nine_forum_schema.sql` 和 `.sql/YYYYMMDD_<feature>.sql`。

### 3. 合同

- 新建图片与 Sticker 均为公开资源，不实现私有、关注者可见或按上传者隔离的访问策略。
- `status=ACTIVE` 是用户端读取、收藏、按 URL 导入和内容引用的唯一可用条件。
- `status=DISABLED` 的资源不得通过上述用户端路径返回 URL/元数据或建立新的引用、收藏关系。
- 若对象存储 URL 已被外部保存，业务层的 `DISABLED` 不能撤回该直链；需删除对象时使用管理端强制删除。

### 4. 校验与错误矩阵

| 条件 | 结果 |
| --- | --- |
| `ACTIVE` Sticker，调用者不是上传者 | 允许详情与收藏 |
| `DISABLED` Sticker，查询详情 | 返回不存在/不可访问结果，不返回 `StickerVO` |
| `DISABLED` Sticker，直接收藏或按 URL 添加 | 返回 `资源不可用`，不得插入 `image_asset_favorite` |
| `DISABLED` 图片，作为主题/评论 `imageUrls` 同步 | 不建立 `content_image_ref`，不得读取对象内容或重新派生资源 |
| 管理员下架资源 | 资源状态变为 `DISABLED`；管理端仍可筛选、恢复或强制删除 |

### 5. Good / Base / Bad 案例

- Good：账号 B 按 `assetId` 收藏账号 A 上传的 `ACTIVE` Sticker，创建 B 与该资产的收藏关系。
- Base：按 URL 同步的资源不存在时，服务校验平台内 URL 后创建新的 `ACTIVE` 图片资产。
- Bad：详情接口仅校验资源类型而忽略 `DISABLED`，返回已下架对象的 URL。
- Bad：发现同 URL 的 `DISABLED` 资源后仍读取对象存储并作为帖子图片或新 Sticker 复用。

### 6. 必需测试

- `ImageAssetServiceImplTest`：不同账号可收藏 `ACTIVE` Sticker；`DISABLED` Sticker 的详情、直接收藏和按 URL 添加均被拒绝。
- `ImageAssetServiceImplTest`：`DISABLED` 图片同步内容引用时不调用 `insertIgnore`，也不读取对象内容。
- schema 变更：核查完整 schema 不含废弃列，增量 SQL 能对已有库删除该列。
- 运行 `./mvnw.cmd -pl web/web-app -am test`；共享模型变更还应运行 `./mvnw.cmd -pl web/web-admin -am test`。

### 7. 错误与正确示例

#### 错误

```java
// 错误：把上传者当作公共 Sticker 的访问边界，或忽略资源已下架。
if (!Objects.equals(asset.getAccountId(), accountId)) {
    return "无权访问";
}
return toVO(asset, false);
```

#### 正确

```java
// 正确：公开性不需要授权分支；用户端只复用 ACTIVE 资源。
if (!ImageAssetStatus.ACTIVE.name().equals(asset.getStatus())) {
    return null;
}
return toVO(asset, false);
```
