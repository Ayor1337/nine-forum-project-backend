# 测试说明

项目使用 Maven、Spring Boot Test 和 JUnit。测试按模块放在对应 `src/test/java` 下，包名与生产代码保持一致。

## 常用命令

在仓库根目录执行：

```bash
./mvnw clean test
```

Windows PowerShell：

```powershell
.\mvnw.cmd clean test
```

模块级测试：

```bash
./mvnw -pl web/web-app -am test
./mvnw -pl web/web-admin -am test
./mvnw -pl common test
./mvnw -pl model test
```

## 当前覆盖重点

- Controller：用户端和管理端主要 HTTP 控制器测试。
- Service：账号、主题、帖子、搜索、私信、反馈、图片资源、权限、统计、Passkey 等服务测试。
- Security：JWT 过滤器、安全配置未认证场景、公开 GET、登出行为。
- Mapper/XML：权限 Mapper XML、权限操作日志 Mapper 等。
- DTO/实体：部分 DTO 校验、Passkey Credential、枚举和 TypeHandler。
- AOP/通知：聊天通知、未读消息、操作日志等切面测试。
- common 工具：JWT、安全工具、TipTap、图片处理等。

## 新增测试建议

| 变更类型 | 建议测试 |
| --- | --- |
| Service 业务规则 | 单元测试覆盖成功、失败、边界条件和副作用。 |
| Controller 入参/出参 | MockMvc 测试认证、参数校验、分页参数、错误响应。 |
| 权限或安全配置 | 未认证、无权限、公开接口、登出和 JWT 失效测试。 |
| Mapper 或 SQL | Mapper 测试或针对 XML 的结构性测试，避免字段名和结果映射漂移。 |
| RabbitMQ / STOMP 副作用 | Mock `RabbitTemplate` 或 `SimpMessagingTemplate`，验证 exchange、routing key、目的地和消息体。 |
| Redis 缓存 | Mock Redis 操作或使用隔离配置，验证 key、TTL、失效逻辑。 |
| WebAuthn / Passkey | 测试 challenge 生成、过期、重复使用、credential 校验失败。 |

## 外部依赖原则

- 单元测试优先 Mock MySQL、Redis、RabbitMQ、MinIO、Elasticsearch 和邮件服务。
- 需要真实依赖的集成测试应明确标注运行前提，避免默认 `clean test` 依赖本机服务状态。
- 涉及缓存、消息、搜索索引的测试要验证副作用，而不只验证返回值。
- 新增测试不得依赖真实凭据、真实邮箱或生产端点。

## 验收标准

- 文档或纯注释变更不要求运行 Maven 测试，但应检查链接、路径和命令是否准确。
- 业务代码变更至少运行对应模块测试。
- 共享工具、模型、配置或安全链路变更应运行根目录 `clean test`，或说明无法运行的原因。
