# 技术设计：移除图片资源可见性字段

## 边界与目标模型

`ImageAsset` 表示论坛中的公开可复用图片资源；`accountId` 只保留上传溯源作用，不承担访问授权。资源是否可被用户端读取或复用只由 `status` 决定：`ACTIVE` 允许，`DISABLED` 拒绝。

本次不改变对象存储/CDN 的公开直链策略。`DISABLED` 的阻断边界是业务 API 与业务引用；管理员对需要彻底撤销的对象继续使用现有强制删除能力。

## 数据模型与迁移

1. 从 `model` 中的 `ImageAsset` 删除 `visibility` 属性，并删除 `ImageAssetVisibility` 枚举。
2. 从完整 schema 的 `image_asset` 建表语句移除 `visibility` 列。
3. 在 `.sql/` 新增一次性迁移，使用 `ALTER TABLE image_asset DROP COLUMN visibility` 处理已有环境。该列没有需保留的业务数据，因此无需回填或数据转换。

## 用户端服务规则

| 路径 | `ACTIVE` | `DISABLED` |
| --- | --- | --- |
| Sticker 详情 | 返回 `StickerVO` | 返回不存在/不可访问结果，不暴露 URL 或元数据 |
| 收藏 Sticker | 允许（与上传者无关） | 返回“资源不可用”，不创建收藏关系 |
| 按 URL 添加 Sticker | 可复用已有 Sticker 或生成新 Sticker | 不得复用已禁用的既有资源 |
| 内容图片引用 | 可创建或保留引用 | 不得创建对已禁用资源的引用 |

服务内部使用一个私有的“资源是否可用”判断，避免在详情、按 URL 查找与内容引用处散落 `status` 比较。该判断不属于访问控制策略，也不包含所有者或 `visibility` 条件。

## 兼容性与风险

- Java 编译会暴露遗留的 `visibility` getter/setter、枚举和建造参数引用；全仓搜索后统一删除。
- 旧数据库若未执行增量 SQL，会因实体列与 schema 不一致产生漂移，因此发布时必须先应用 `.sql/` 脚本。
- 公开对象存储 URL 一旦被外部保存，`DISABLED` 无法撤回该直链；严重内容需强制删除对象。
- 管理端按状态筛选和更新、强制删除语义保持不变。

## 回滚

代码发布失败时回滚应用版本；数据库列删除不可从原始值恢复，但该值已被产品决策废弃且无需恢复。若必须回滚旧版本，可临时重新添加 `visibility varchar(16)` 并以 `PUBLIC` 填充，之后再恢复旧应用。
