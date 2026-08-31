# 调查并修复基础设施弱保护

## Goal

消除 SEC-06 在仓库可控范围内的默认不安全行为：本地依赖服务不得绑定所有宿主接口，不得提交可用凭据，不得依赖无认证 Redis 或匿名 Elasticsearch 作为生产基线；应用对 SMTP、RabbitMQ 等跨主机传输应能启用并严格校验 TLS。开发环境仍应有明确、可复现的启动方式。

## Background and confirmed facts

- `.docker/docker-compose.yaml:9-10,25-26,34-36,85-87,98-99` 使用未指定宿主 IP 的短端口语法。Docker Compose 官方定义该写法绑定所有接口；已停止容器的 `HostConfig.PortBindings` 也记录为空 `HostIp`。
- `.docker/docker-compose.yaml:22-30` 的 Redis 没有 ACL 或密码；`.docker/docker-compose.yaml:81-87` 显式设置 `xpack.security.enabled=false`，并发布 Elasticsearch HTTP 与无需宿主访问的 transport 端口 `9300`。
- `.docker/docker-compose.yaml:46-52` 没有发布 RabbitMQ 端口，但 `README.md:82` 和两个应用配置都按宿主 `localhost:5672` 连接，当前本地启动合同互相矛盾。
- `.docker/docker-compose.yaml:40-42`、受 Git 跟踪的 `.docker/environment/mysql.env`、`web/web-admin/src/main/resources/application.yml` 含字面量凭据；用户端真实 `application.yml` 已被忽略，示例配置使用占位符。
- `web/web-app/src/main/resources/application.example.yml:30-44` 使用 RabbitMQ `5672`、SMTP 默认协议和 Elasticsearch HTTP，未声明 TLS、证书链或主机名校验。
- Compose 中 MySQL、Redis、RabbitMQ 使用可漂移标签 `latest` / `management`。本机 `mysql:latest` 已解析到 MySQL 9.5.0，说明该标签不能表达项目兼容基线；已停止 Elasticsearch/Kibana 容器为 9.2.1，而当前 Compose 声明 8.18.8，也显示运行态可能与文件漂移。
- 仓库没有生产 Compose、Kubernetes/Helm、反向代理、证书生命周期或密钥管理配置，无法仅凭当前仓库实现并验证完整生产部署。
- 当前所有 `nine-*` 容器均已停止；本轮没有启动容器、探测业务数据或输出秘密值。

## Requirements

### R1 — 收敛本地宿主暴露面

- 所有确需宿主访问的 Compose 端口显式绑定 `127.0.0.1`。
- 删除 Elasticsearch transport `9300` 的宿主发布。
- RabbitMQ 若继续供宿主运行的 Spring Boot 应用使用，只发布 `127.0.0.1:5672:5672`；管理 UI 不默认发布。
- README 端口表与实际 Compose 保持一致，并明确该 Compose 仅用于本地开发。

### R2 — 移除仓库中的可用秘密

- 移除 Compose、MySQL env 和管理端配置中的字面量凭据，改为未跟踪的本地配置或外部环境注入，并提供安全占位模板。
- `.gitignore` 覆盖真实 env/应用配置，但继续跟踪无秘密的示例文件。
- 文档明确：历史中出现过或可能经明文链路传输的 MySQL、Redis、MinIO、RabbitMQ、SMTP、Elasticsearch 凭据必须轮换；Git 历史清理与否单独决策，不能替代轮换。

### R3 — 认证与最小权限

- Redis 默认要求认证；优先使用命名 ACL 用户，并按应用实际命令/键空间收敛权限。若首批无法安全枚举完整命令集，可先使用命名用户和强随机密码，禁止匿名 `default` 用户，再以测试收敛权限。
- Elasticsearch 本地开发也启用 Security 和独立应用/Kibana 身份，但允许在回环接口上使用 HTTP；生产合同必须同时启用 Security、HTTPS 和最小权限。
- MinIO 不使用 root 身份作为应用长期凭据；生产使用独立 service account/policy。开发 root 凭据只能从本地未跟踪配置注入。

### R4 — 传输加密与严格校验

- SMTP 默认配置应要求 STARTTLS 或使用隐式 TLS，并启用证书链与服务器主机名校验；不允许静默降级到明文。
- RabbitMQ 生产连接使用 AMQPS/TLS，校验证书链与主机名；本地同机连接允许在回环接口上使用明文 AMQP。
- Redis、MinIO、Elasticsearch 在跨主机或生产网络中使用 TLS；证书、私钥和 truststore 不进入 Git。

### R5 — 固定供应链输入

- 所有基础设施镜像固定到经过兼容性验证的具体版本；生产或可复现环境进一步固定 digest。
- 不把本机当前 `latest` 解析结果直接当作兼容版本；MySQL 数据卷升级前必须先确认当前数据版本与升级路径。

### R6 — Fail-fast 与文档同步

- 缺少必需凭据或生产 TLS 配置时启动失败，不用弱默认值兜底。
- README/配置说明同步覆盖本地启动、秘密生成与放置、首次初始化、凭据轮换、生产网络/TLS要求和回滚注意事项。

## Acceptance Criteria

- [x] AC1（R1）：`docker compose config` 展示所有宿主发布端口只绑定 `127.0.0.1`，且不存在 `9300` 宿主发布。
- [ ] AC2（R1）：宿主运行的两个 Spring Boot 应用仍可按文档连接所需依赖；RabbitMQ 端口说明与 Compose 一致。
- [x] AC3（R2）：Git 跟踪文件中不存在基础设施、JWT 或 SMTP 的可用字面量凭据；秘密扫描/定向 `rg` 检查通过。
- [ ] AC4（R3）：未认证 Redis 命令和匿名 Elasticsearch 请求均失败，应用与 Kibana 使用各自身份成功连接。
- [x] AC5（R4）：SMTP 配置要求加密并校验主机名；生产 RabbitMQ 配置启用 TLS、证书链与主机名校验，且没有 trust-all 配置。
- [x] AC6（R5）：Compose 不再使用 `latest`、裸 `management` 等可漂移标签；所选版本与现有持久化数据兼容性已记录。
- [x] AC7（R6）：README 与示例配置完整描述开发/生产边界、秘密注入、轮换和验证命令。
- [x] AC8：Compose 静态校验、配置合同测试及受影响 Maven 测试通过；若执行动态验证，只使用测试凭据和隔离数据。
- [x] AC9：`security-audit-report.md` 的 SEC-06 状态按实际完成度更新；未落地的生产控制不得标记为已验证修复。

AC2 尚未执行两个 Spring Boot 应用连接全部依赖的端到端启动验证。AC4 已用隔离 Redis 验证匿名拒绝、应用命令允许、管理命令拒绝；Elasticsearch 应用身份与 Kibana token 的动态验证仍待使用新卷执行，不能复用本机现有 9.2.1 数据卷验证 8.18.8 配置。

## Out of Scope

- 为本地 Compose 建立 CA、签发服务证书、生成 truststore 或启用全链路 TLS。
- 在未知生产平台上代建防火墙、VPC、安全组、Kubernetes、反向代理或托管密钥服务。
- 签发、托管或自动轮换真实生产证书和凭据。
- 未经备份与兼容性验证直接升级现有 MySQL/Elasticsearch 数据卷。
- 为清理秘密而强制重写共享 Git 历史；如需执行，另行制定协作与备份方案。

## Key Decisions

- 本地 Compose 采用“回环绑定 + 服务认证 + 外置秘密”的安全基线，不启用 TLS。
- SMTP 仍必须使用 TLS，因为邮件连接跨越本机信任边界。
- RabbitMQ、Redis、MinIO、Elasticsearch 的 TLS 作为生产部署合同和部署验收项；当前仓库没有生产编排资产，因此不声称完成生产端到端验证。
- 管理端真实 `application.yml` 与用户端采用相同隔离模式。现有 `.trellis/spec/backend/configuration-secrets.md` 只允许忽略用户端配置，实施后必须同步更新该规范，不能让代码和规范长期冲突。
