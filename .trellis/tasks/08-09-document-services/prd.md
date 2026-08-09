# 调查并编写 Service 作用文档

## Goal

系统调查 NineForum 后端的 service，形成可供开发者快速理解职责、模块边界和代码入口的中文文档，并将最终文档放入仓库根目录的 `review/` 目录。

## Background

- 项目是 Java 17、Spring Boot 3.5.5 的多模块 Maven 后端。
- `web/web-app` 面向用户端，`web/web-admin` 面向管理端，`common` 提供通用能力。
- 仓库当前不存在 `review/` 目录，也没有现成的 service 文档模板。
- 主代码中发现：
  - `web-app` 的 `com.ayor.service` 下有 44 个抽象/接口文件及 44 个实现文件；
  - `web-admin` 的 `com.ayor.service` 下有 29 个抽象/接口文件及 29 个实现文件；
  - `common` 中有 `ImageStorageService`、`MinioService` 两个服务类；
  - service 包外还有 3 个标注 `@Service` 的类：两个模块各自的 `EsIndexSyncProducer`，以及 `web-app` 的 `ESIndexManager`。
- 接口及其实现应视为同一个逻辑 service，不重复形成两个文档条目；用户端与管理端的同名 service 属于不同模块，需要分别说明。

## Requirements

- 覆盖口径为 78 个逻辑条目：`web-app` 44 个 service 抽象、`web-admin` 29 个 service 抽象、`common` 2 个明确命名服务，以及 service 包外 3 个标注 `@Service` 的基础设施类。
- 以源代码为主要事实依据，测试、Controller、Mapper、消息队列和配置用于交叉验证职责。
- 覆盖全部已确认逻辑 service，不遗漏无接口实现、适配器、公共服务或包外 Spring Service Bean。
- 每个逻辑 service 至少说明：所在模块、核心作用、主要职责/能力、关键协作者或数据边界、源代码入口。
- 清楚区分用户端、管理端和公共模块中的同名 service。
- 文档采用中文，目录和内容结构应便于浏览及后续维护。
- 只新增或修改 `review/` 下的交付文档及本 Trellis 任务资料，不修改业务代码。

## Acceptance Criteria

- [ ] `review/` 下存在一个总览入口，可查看覆盖范围、统计口径和各模块文档链接。
- [ ] 最终范围内的每个逻辑 service 都恰好有一个可检索条目，并可追溯到源文件。
- [ ] 每个条目准确描述作用与主要职责，不仅是根据类名直译。
- [ ] 文档明确记录接口/实现合并口径、跨模块同名类口径，以及排除项。
- [ ] 通过自动化清单比对确认没有漏写或重复记录。
- [ ] Markdown 链接和目录结构可用，业务源码保持不变。

## Out of Scope

- 修改、重构或修复现有 service 代码。
- 生成逐方法 API 参考手册或 Controller 接口文档。
- 为每个 service 补充测试、JavaDoc 或架构图。
- 评价实现质量或提出大规模重构方案；明显风险可在文档中客观备注，但不是本任务主目标。

## Technical Notes

- 用户已确认采用完整口径；`EsIndexSyncProducer` 和 `ESIndexManager` 作为“包外 Spring 服务”单列。
- `PermissionService`、`RoleService`、`TopicChatService` 等部分用户端接口只继承 MyBatis-Plus `IService`，文档应如实写明没有自定义业务方法，不推断不存在的职责。
- 覆盖校验以“模块 + 类名”的稳定标识为准，避免用户端和管理端同名 service 相互抵消。
