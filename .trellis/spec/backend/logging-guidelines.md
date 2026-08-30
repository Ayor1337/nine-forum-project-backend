# 日志与可观测性规范

## 现有做法

项目使用 Lombok `@Slf4j` 提供日志，集中出现在基础设施和异步边界，而不是所有业务类强制打日志。可参考：

- `web/web-app/src/main/java/com/ayor/listener/EmailListener.java`：消息消费结果与失败记录。
- `web/web-app/src/main/java/com/ayor/listener/EsIndexSyncListener.java`：异步索引处理。
- `web/web-app/src/main/java/com/ayor/initializer/ESIndexInitializer.java`：启动初始化。
- `common/src/main/java/com/ayor/util/ImageUtils.java`：工具处理中的异常上下文。

## 写入原则

- 在异步消费、启动初始化、外部服务调用失败或无法由响应体表达的降级处记录日志；普通可预期业务失败优先走 `Result`/Service 返回约定，避免重复噪声日志。
- 记录可排障上下文：业务 ID、消息类别、交换机/routing key、操作阶段；使用参数化日志，避免字符串拼接。
- 捕获异常后若继续抛出或返回失败，应保留异常对象作为最后一个参数，确保堆栈可查。
- 不记录 JWT、密码、SMTP/MinIO/RabbitMQ 密钥、完整邮件内容、原始 Passkey 凭据或用户隐私内容；敏感配置必须通过运行环境注入，或只保存在 Git 忽略的本地配置中，具体遵循[本地配置与秘密规范](./configuration-secrets.md)。

## 修改异步链路时

RabbitMQ、STOMP、Redis、Elasticsearch 与 MinIO 的改动不能只加日志：同时检查配置、生产者/监听器、消息模型与测试；以对应 `config/`、`mq/`、`listener/` 与测试源码为准。
