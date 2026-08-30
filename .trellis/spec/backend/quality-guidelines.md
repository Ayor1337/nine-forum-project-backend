# 质量要求

## 代码要求

- 保持 Controller 只做请求/响应组织；业务规则归 Service，持久化归 Mapper，跨应用通用能力归 `common` 或 `model`。
- 依赖通过构造器注入；现有 Controller 广泛使用 Lombok `@RequiredArgsConstructor`，例如 `ThreadController`。不要新增字段注入。
- 新接口维持 `Result<T>`、DTO 校验、当前用户读取与 OpenAPI 注解的已有形式。
- 修改常量、配置、路由、消息名或返回字段前，先全仓搜索其消费者；用户端与管理端常有平行实现。

## 测试

项目使用 JUnit 与 Spring Boot Test；测试与生产包路径一致。可靠样例包括：

- Service 规则和副作用：`web/web-app/src/test/java/com/ayor/service/impl/PostServiceImplTest.java`。
- Controller 合同、认证和校验：`web/web-app/src/test/java/com/ayor/controller/ThreadControllerTest.java`、`controller/exception/ValidateControllerTest.java`。
- RabbitMQ/STOMP：Mock `RabbitTemplate`、`SimpMessagingTemplate` 并验证 exchange、routing key、目的地与消息体。

业务代码至少运行受影响模块的 Maven 测试；共享模型、配置、安全或公共工具改动运行根目录测试。常用命令：

```powershell
.\mvnw.cmd -pl web/web-app -am test
.\mvnw.cmd -pl web/web-admin -am test
.\mvnw.cmd clean test
```

文档改动不必运行 Maven，但必须检查链接、路径、命令及占位文本。

## 审查清单

- 模块依赖方向是否仍为 `web -> common/model`？
- DTO/VO/POJO、Mapper、SQL 与文档是否在数据变更时同步？
- 新的副作用是否有消息、缓存、搜索或 WebSocket 的验证？
- 是否保留了输入校验、权限检查和统一响应？
- 是否避免日志中的秘密或隐私数据？
