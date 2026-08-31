# SEC-06 修复技术设计

## Architecture and boundaries

采用两层配置边界：

1. **本地开发层**：仓库提供的 `.docker/docker-compose.yaml` 只服务宿主本机开发，所有发布端口绑定回环；应用使用认证身份；秘密来自未跟踪的本地 env/config。
2. **生产合同层**：仓库用示例配置、校验和文档声明私网、TLS、最小权限与外部秘密要求。由于项目没有生产编排资产，不伪造一个无法验证的生产部署。

本地层不建立 CA、服务证书或 truststore。回环明文只适用于同机开发，不得作为跨主机或生产配置复用。

## Proposed configuration shape

### Docker Compose

- 使用长端口语法或 `127.0.0.1:HOST:CONTAINER`，覆盖 MySQL、Redis、MinIO API/Console、RabbitMQ、Elasticsearch HTTP、Kibana。
- 移除 Elasticsearch `9300` 发布；容器网络内服务依靠 Compose DNS，不需要 `ports` 或多余 `expose`。
- 把静态凭据替换成必填环境变量 `${VAR:?message}`，由 `.docker/environment/*.env`（忽略）或 shell/部署平台提供；提交 `.env.example`。
- Redis 挂载 ACL/config 模板，禁用匿名 default 用户并创建应用用户。用户名/密码通过启动时生成的未跟踪文件或受控注入提供，避免把秘密写回模板。
- 开发 Elasticsearch 启用 `xpack.security.enabled=true` 并配置应用/Kibana 身份，只在回环边界允许 HTTP；生产示例/合同进一步启用 HTTPS。

### Spring Boot configuration

- 为用户端、管理端提供结构一致的 example 配置；真实配置一律忽略或从环境注入。
- SMTP 采用 fail-fast TLS：STARTTLS 场景配置 `mail.smtp.starttls.enable=true`、`mail.smtp.starttls.required=true`、`mail.smtp.ssl.checkserveridentity=true`；隐式 TLS服务商使用 `spring.mail.ssl.enabled=true` 和对应端口，不能同时靠猜测启用两种模式。
- RabbitMQ 生产使用 `spring.rabbitmq.ssl.enabled=true` 或 `amqps://`，由 Spring SSL bundle/truststore 提供信任材料，并保持服务器证书与连接主机名一致。禁止 `useTlsWithNoVerification` 或 trust-all。
- Redis/Elasticsearch/MinIO 客户端生产 URI 改为 TLS scheme，并显式提供 CA 信任；不关闭证书校验。

## Identity and least privilege

- Redis 先建立 `nineforum-app` 命名用户，关闭匿名 default。项目使用 String、Set、Sorted Set、Lua/EVAL、过期、删除、发布订阅/缓存相关命令；ACL 必须以集成测试验证，避免凭经验漏授权。管理操作、`CONFIG`、`ACL`、危险命令不授予应用。
- 两个 Spring 应用的自定义 Lettuce 连接工厂必须同时转发 ACL username/password 和 `spring.data.redis.ssl.enabled`；启用 Redis TLS 时使用 Lettuce `FULL` 对端校验，信任材料由生产 JVM/SSL truststore 外部提供，不允许关闭 peer verification。
- Elasticsearch 为应用索引读写与 Kibana 分配不同身份；应用身份只允许项目索引和必要 cluster monitor 权限。
- MinIO 应用使用独立 service account/policy，仅访问目标 bucket；root 只用于初始化。
- RabbitMQ 使用独立 vhost 和应用用户，权限仅覆盖项目 exchange/queue routing patterns。

## Compatibility and migration

- MySQL `latest` 当前本机对应 9.5.0，且只读运行日志确认现有 bind volume 曾由 9.5.0 成功打开。Compose 固定到已验证的 9.5.0，避免把该数据目录降级挂载到 8.4；若需使用 8.4 LTS，必须先做逻辑导出，再使用新数据卷导入。
- Elasticsearch 当前 Compose 声明 8.18.8，而旧容器元数据为 9.2.1。实施前确认数据卷创建版本；ES 不能跨不支持的主版本直接复用数据目录。
- Redis/RabbitMQ 同样固定 patch tag + digest，并通过项目集成测试确认 Lua、ACK、管理插件等行为。

本次 Compose 选择具体标签 `mysql:9.5.0`、`redis:8.4.6`、`rabbitmq:4.2.1-management`、现有 MinIO release 以及 Elasticsearch/Kibana `8.18.8`；Docker registry digest 尚未锁定。只读运行日志确认现有 MySQL bind volume 曾由 9.5.0 成功打开、Redis bind volume 曾由 8.4.0 成功打开，因此本地 Compose 固定同一主次版本的补丁版本以避免降级挂载。迁移到其它主版本必须先做逻辑导出，再使用新数据卷导入；由于当前未建立可验证的数据卷备份，本次不启动或迁移旧卷。目标工作区启用前仍必须按上述约束确认版本、备份和回滚点。

## Rollout and rollback

1. 先备份并记录数据库/对象/索引/队列状态与当前镜像 ID。
2. 先合入端口回环与秘密外置，不改变数据格式。
3. 分服务启用认证：Redis → MinIO/RabbitMQ identities → Elasticsearch Security；每步同时更新客户端并验证健康检查。
4. 最后启用跨主机 TLS 并验证证书链、SAN/主机名、到期时间和失败路径。
5. 回滚只回退配置和镜像到已记录版本；不得把新版本写过的数据卷直接挂回旧主版本。

## Sources

- Docker Compose services/ports/image: https://docs.docker.com/reference/compose-file/services/
- Redis security, ACL and TLS: https://redis.io/docs/latest/operate/oss_and_stack/management/security/
- Elasticsearch minimal security and TLS: https://www.elastic.co/guide/en/elasticsearch/reference/current/security-minimal-setup.html
- RabbitMQ TLS: https://www.rabbitmq.com/docs/4.2/ssl
- Spring Boot mail/application properties and SSL bundles: https://docs.spring.io/spring-boot/appendix/application-properties/
- MinIO container TLS: https://min.io/docs/minio/container/operations/network-encryption.html

## Accepted trade-off

不为本地 Compose 启用 TLS，避免引入 CA/证书生成、SAN、truststore、续期和跨平台脚本维护。回环 + 认证切断 SEC-06 的局域网暴露路径；代价是生产 TLS 只能作为配置合同和部署验收项，不能在当前仓库内端到端证明。
