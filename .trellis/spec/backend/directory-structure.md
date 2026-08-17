# 目录与分层

## 模块职责

| 目录 | 职责 |
| --- | --- |
| `common/src/main/java/com/ayor` | 可跨应用复用的结果封装、JWT/安全工具、MinIO 与图片能力，例如 `result/Result.java`、`minio/MinioService.java`。 |
| `model/src/main/java/com/ayor` | 公共 POJO、DTO、VO、消息对象、枚举和 TypeHandler；不放 Controller 或具体 Web 业务。 |
| `web/web-app` | 面向用户端 API、认证、论坛、私信、通知、搜索等实现。 |
| `web/web-admin` | 面向管理端 API、运营和内容治理实现。 |

`web` 依赖 `common`、`model`，两个 Web 应用不能让公共模块反向依赖自己；参见根 `pom.xml` 和 `docs/architecture.md`。

## Web 模块内的局部模式

在每个应用的 `src/main/java/com/ayor` 下，按职责放置：

- `controller/`：HTTP 边界，声明路由、参数与 OpenAPI 注解，调用 Service 并返回 `Result<T>`。示例：`web/web-app/src/main/java/com/ayor/controller/ThreadController.java`。
- `service/`：业务接口；`service/impl/`：业务规则、跨 Mapper/缓存/消息的编排。示例：`web/web-app/src/main/java/com/ayor/service/impl/PostServiceImpl.java`。
- `mapper/`：MyBatis-Plus Mapper 接口；复杂 SQL 可配套放在同模块 `src/main/resources/mapper/`，如 `web/web-app/src/main/resources/mapper/PermissionMapper.xml`。
- `entity/dto`、`entity/vo`、`entity/pojo`：仅当模型为该 Web 应用专属时放这里；两个应用共享的模型放 `model`。
- `config/`、`filter/`、`interceptor/`、`listener/`、`mq/`、`scheduled/`：分别承载配置、安全链路、事件消费/生产与定时工作，避免塞入 Controller。

测试置于同一 Maven 模块的 `src/test/java`，包路径与生产代码保持一致，例如 `web/web-app/src/test/java/com/ayor/controller/ThreadControllerTest.java`。

## 命名与边界

- 类型以职责命名：`XxxController`、`XxxService`、`XxxServiceImpl`、`XxxMapper`、`XxxDTO`、`XxxVO`、`Xxx`（POJO）。项目历史中业务“帖子”沿用 `Threadd` 命名；修改该领域时保持现有名称，不混入新的 `ThreadService` 平行体系。
- Controller 保持薄：不要将查询组装、权限判断、持久化或消息发送直接复制到 Controller；`ThreadController` 通过 `ThreaddService`、`PostService`、`ReportService` 协作是当前模式。
- 不把用户端专有模型放入 `common`，也不让 `model` 导入 `web-app` 或 `web-admin` 类。
