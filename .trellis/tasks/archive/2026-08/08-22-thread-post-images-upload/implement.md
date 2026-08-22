# 执行计划：主题与评论独立图片上传

## 实施步骤

1. 更新用户端 `ThreadDTO`、`PostDTO`、`PostEditDTO`，添加 `images: List<Base64Upload>`，保留 `imageUrls`。
2. 在现有图片存储边界提供可复用的批量 Base64 上传能力，逐项复用 `storeImageBase64Image` 并返回有序 URL；不把图片上传重新耦合到 `TipTapUtils`。
3. 调整主题创建、编辑：在所有上传前以保留 URL 与新图片总数校验 7 张上限；上传后合并 URL、写入实体并以最终数组同步图片引用。
4. 调整评论创建、编辑：在所有权限、关联和正文校验通过后上传、合并 URL、写入实体并同步引用。
5. 将 `TipTapUtils` 的图片节点错误提示改为指向 `images`，同步 Javadoc 与相关单测断言。
6. 更新主题与评论 Service 单测：上传成功、数组顺序、7/8 张总数、正文图片拒绝、编辑快照与无上传副作用。
7. 更新 `.trellis/spec/backend/thread-content-images.md`，记录双字段输入和最终 URL 的唯一持久化/读取来源。

## 验证

1. `./mvnw.cmd -pl web/web-app -am test`
2. 若批量存储能力变更位于 `common`，补充执行 `./mvnw.cmd clean test`。
3. 执行 Trellis 全量质量检查，核对 DTO、Service、资源引用、TipTap 提示、测试和规范的跨层一致性。

## 风险与回滚点

- `ImageStorageService`：复用单图处理策略，不改变既有头像、横幅、贴纸等调用方的语义。
- `ThreaddServiceImpl` / `PostServiceImpl`：图片总数、正文校验和业务校验必须位于任何上传前；编辑时快照必须在上传与校验成功后才写入。
- MinIO 写入不受数据库事务回滚保护；本任务不增加补偿删除，出现存储或数据库异常时沿用既有对象生命周期。
- 可通过回退本次 Java 与规范改动恢复“仅 `imageUrls` 直写”的行为；数据库不变，因此不需要回滚迁移。
