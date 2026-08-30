# 实施计划：移除图片资源可见性字段

1. 全仓检索 `visibility` 与 `ImageAssetVisibility` 的图片资源引用，确认用户隐私设置中的同名字段不在修改范围。
2. 修改共享模型：删除 `ImageAsset.visibility` 与 `ImageAssetVisibility`。
3. 修改用户端图片资源服务：去除创建资源时的可见性参数和赋值；提取 `ACTIVE` 可用性判断，并让详情、按 URL Sticker 复用与内容图片引用遵守该判断；保留任何登录用户复用 `ACTIVE` 公共 Sticker 的行为。
4. 修改完整 schema，新增 `.sql/` 存量库迁移脚本以删除 `image_asset.visibility`。
5. 扩展 `ImageAssetServiceImplTest`：覆盖跨用户收藏公共 Sticker、`DISABLED` Sticker 详情/收藏/按 URL 导入拒绝，以及 `DISABLED` 内容图片不建立引用。
6. 全仓复查，确保图片资源范围内不存在被删除字段的引用，并确认管理端状态管理未被改动。
7. 运行 `./mvnw.cmd -pl web/web-app -am test` 与 `./mvnw.cmd -pl web/web-admin -am test`；如共享模型变更影响范围不确定，再运行根目录 `./mvnw.cmd test`。

## 风险检查点

- schema 与增量 SQL 必须一起提交；不可只删除 Java 属性。
- 业务 API 应拒绝 `DISABLED`，但不将其误解为对象存储直链的访问撤销。
- `accountId`、收藏关系和管理端 `status` 接口不属于本次删除对象。
