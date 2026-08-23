# 移除图片资源可见性字段

## Goal

将图片与 Sticker 明确建模为论坛内可公开复用的资源，移除没有产品需求支撑的资源可见性概念，避免系统错误地将用户上传的 Sticker 视为私有资产。

## Confirmed Facts

- `ImageAsset` 是 `model` 模块的共享实体，当前包含 `visibility` 字段；`ImageAssetVisibility` 枚举仅在用户端图片资源服务中使用。
- 用户端上传和由站内图片生成的 Sticker 当前固定写入 `PRIVATE`；帖子内容图片固定写入 `PUBLIC`。
- 产品决策：图片和 Sticker 均为论坛内可复用的公开资源，不提供私有图片或私有 Sticker 能力。
- `status` 不属于本次删除范围，保留 `ACTIVE` 与 `DISABLED`，供管理端下架和强制删除资源使用。
- 项目没有 Flyway/Liquibase；模式变更必须同步完整初始化脚本 `.docker/image/mysql/nine_forum_schema.sql` 与 `.sql/` 中的存量环境增量脚本。

## Requirements

1. 从共享实体、枚举和所有生产代码中删除图片资源 `visibility` 概念，不保留仅为兼容旧模型而存在的无效字段或参数。
2. 所有新建图片资源和 Sticker 的业务语义均为公开、可复用；按 `assetId` 查询或收藏公开且可用的 Sticker 不需要所有者授权。
3. 删除数据库 `image_asset.visibility` 列：更新新环境初始化 schema，并提供存量环境可执行的增量 SQL。
4. 保留 `status` 及管理端上下架/强制删除能力；本次变更不得改变管理员对资源状态的管理接口。
5. `DISABLED` 资源在所有用户端读取或复用路径均不可访问：详情查询不得返回资源信息；收藏、按 URL 导入和帖子内容引用不得复用它。
6. 更新受影响的单元测试，确保代码不再依赖 `visibility`，并维持公共 Sticker 的查询与收藏行为。

## Acceptance Criteria

- [ ] `ImageAsset` 不再含 `visibility` 属性，`ImageAssetVisibility` 枚举已删除，全仓生产代码不存在图片资源可见性引用。
- [ ] 新环境 schema 不含 `image_asset.visibility`，且 `.sql/` 中存在针对已有环境删除该列的增量脚本。
- [ ] 上传与按 URL 转换的 Sticker 均可被其他登录用户按现有公共资源流程查询、收藏；不再因资源上传者不同而拒绝。
- [ ] `ImageAssetStatus`、管理端状态更新接口与强制删除接口仍可用。
- [ ] 状态为 `DISABLED` 的资源不会通过用户端详情、收藏、按 URL 导入或内容引用路径暴露或复用。
- [ ] `web-app` 与 `web-admin` 受影响模块的 Maven 测试通过。

## Out of Scope

- 新增私有图片、私有 Sticker、关注者可见等访问控制能力。
- 更改对象存储/CDN 的公开访问策略。
- 改变资源所有者 `accountId` 的溯源用途，或重新设计 Sticker 收藏数据模型。

## Open Question

- 无。

## Notes

- 本任务跨共享模型、用户端、管理端与数据库 schema，按复杂任务编写设计和实施计划。
