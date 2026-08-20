# 实施计划：帖子最多 7 张图片并完整返回

## Task List

- [x] 在 `TipTapUtils` 中建立单一图片节点计数/上限校验能力，并为 0、7、8 张、URL/Base64 混合、sticker 和重复 URL 补充单元测试。
- [x] 在 `ThreaddServiceImpl.insertThread(...)` 和 `editThread(...)` 的任何图片上传或持久化操作前应用最多 7 张规则。
- [x] 为创建和编辑超限场景补充 Service 测试，验证错误消息及“无上传、无写入、无后续副作用”。
- [x] 将 `ThreaddServiceImpl.toVOs(...)` 与 `CollectServiceImpl.toVO(...)` 改为返回全部图片 URL，并补充列表转换测试。
- [x] 全仓搜索旧的 3 张限制及 `extractImageUrls(...)` 消费者，确认没有遗漏或无意扩大到 post。
- [x] 运行 `.\mvnw.cmd -pl web/web-app -am test`，再执行 Trellis 全量质量检查。

## 风险文件与回滚点

- `common/src/main/java/com/ayor/util/TipTapUtils.java`：公共工具被多个内容流程复用，新增能力应避免改变 post 的上传行为。
- `web/web-app/src/main/java/com/ayor/service/impl/ThreaddServiceImpl.java`：校验顺序必须位于 Base64 转换前。
- `web/web-app/src/main/java/com/ayor/service/impl/CollectServiceImpl.java`：只改变图片 URL 投影，不改变分页与权限逻辑。
- 如测试发现公共方法语义影响其他消费者，保留现有方法并新增命名明确的方法，避免扩大行为变化。

## 验证门槛

- 创建、编辑与列表返回的边界测试全部通过。
- 超限失败路径没有外部存储或数据库副作用。
- 搜索确认主要 threads 列表和收藏列表不再使用最多 3 张的提取逻辑。
- 工作区仅包含本任务文件及预期代码/测试变更。
