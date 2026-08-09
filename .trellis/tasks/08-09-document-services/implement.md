# Service 文档实施计划

## 执行清单

1. 再次固定源码清单，生成 78 个“模块 + 类名”期望 ID。
2. 按模块逐个阅读接口、实现及必要调用方/测试，记录作用、职责、关键依赖和代表性入口。
3. 创建 `review/README.md`，写入范围、统计、口径、模块关系和导航。
4. 创建 `review/web-app-services.md`，完成 46 个用户端条目。
5. 创建 `review/web-admin-services.md`，完成 30 个管理端条目。
6. 创建 `review/common-services.md`，完成 2 个公共服务条目。
7. 自动提取文档 ID 与源码期望 ID，检查缺失、重复和额外条目。
8. 检查所有相对源码链接、Markdown 结构、中文表述和事实准确性。
9. 使用 `trellis-check` 完成最终质量检查，确认只有任务资料和 `review/` 文档发生变化。

## 验证命令与检查

- `git diff --check`：检查 Markdown 空白及补丁格式问题。
- `git status --short`：确认未改动业务源码。
- PowerShell 清单比对：从 service 源目录和指定包外服务生成期望 ID，与文档中 `### \`module:Class\`` 标题集合比较。
- PowerShell 链接检查：解析 `review/*.md` 中的本地相对链接并验证目标存在。
- 人工抽查高协作复杂度服务：`AccountService`、`AuthorizationService`、`ThreaddService`、`ConversationService`、`ImageAssetService`、`DataRepairService`、`DecorationService`。

本任务不运行 Maven 测试：交付只涉及 Markdown，覆盖集合、链接检查和 `git diff --check` 比业务测试更直接；如检查发现业务文件意外变化，则停止并回滚本任务自身改动。

## 风险与回滚点

- 风险：只按类名概括导致职责失真。措施：接口方法、实现依赖、调用方和测试交叉验证。
- 风险：用户端/管理端同名服务被误合并。措施：所有标题带模块前缀。
- 风险：新增或无接口服务漏记。措施：结合目录清单、命名扫描和 `@Service` 注解扫描。
- 回滚点：四个 `review/` 文件均为新增文件，可按文件独立撤销；不触碰业务代码。
