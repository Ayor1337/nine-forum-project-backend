# SEC-06 实施计划

> 状态：实现与静态验证完成；隔离 Redis ACL 动态验证通过，完整应用/Elasticsearch/Kibana 动态验证待新卷环境完成。

## Ordered checklist

1. 盘点并备份
   - 记录现有容器镜像 ID、数据卷来源与实际服务版本。
   - 对 MySQL、Redis、MinIO、Elasticsearch 的持久数据做可恢复备份；不读取业务内容。
2. 收敛 Compose 网络面
   - 所有必要端口绑定 `127.0.0.1`。
   - 删除 Elasticsearch `9300` 发布；补齐仅回环 RabbitMQ `5672` 以匹配宿主应用开发模式。
   - 更新健康检查，使认证启用后仍能正确判断服务状态。
3. 外置秘密
   - 新增无秘密的 env/config example，忽略真实文件。
   - 把 MinIO、MySQL、Redis、RabbitMQ、Elasticsearch 凭据改为必填注入。
   - 将管理端真实配置改为忽略文件并提交等价 example；检查用户端/管理端结构一致性。
4. 加服务认证与最小权限
   - Redis 命名 ACL 用户 + 禁用匿名 default；用集成测试迭代权限。
   - MinIO/RabbitMQ/Elasticsearch 分离 root/admin、应用和 Kibana 身份；本地 Elasticsearch 开启 Security 但保留回环 HTTP，生产合同进一步要求 HTTPS 和最小权限。
5. 配置传输安全
   - SMTP 启用 required STARTTLS 或明确的 implicit TLS，并开启主机名校验和超时。
   - RabbitMQ 生产配置启用 AMQPS、CA 信任和主机名校验。
   - 本地 Compose 保持回环明文，不生成或挂载 CA 与服务证书。
6. 固定镜像
   - 基于实际数据版本选定兼容 patch tag 和 digest；禁止 `latest` / 裸 `management`。
   - 记录升级约束，不在未验证的数据卷上做主版本跨越。
7. 文档和审计状态
   - 更新 README 的端口、启动、秘密、初始化、TLS、轮换与生产边界。
   - 更新 `.trellis/spec/backend/configuration-secrets.md`，把管理端真实配置纳入与用户端一致的秘密隔离合同。
   - 更新 `security-audit-report.md`，按实际证据标记已修复、部分修复或待生产验证。
8. 回归和安全验收
   - 运行静态、动态和 Maven 验证；复核 Git diff 不含秘密。

## Validation commands

```powershell
docker compose -f .docker/docker-compose.yaml config --quiet
docker compose -f .docker/docker-compose.yaml config
docker ps --filter "name=nine-" --format "table {{.Names}}\t{{.Ports}}"
docker port nine-redis
docker port nine-elasticsearch
redis-cli -h 127.0.0.1 -p 16379 PING
curl.exe -fsS http://127.0.0.1:9200/
.\mvnw.cmd test
git diff --check
git status --short
```

动态命令需按新身份/TLS 参数调整。验收必须同时覆盖成功路径和未认证、错误证书、主机名不匹配等失败路径；不得在输出中打印密码、token 或私钥。

## Risky files and rollback points

- `.docker/docker-compose.yaml`：端口、镜像、认证和数据卷启动参数集中，错误可能导致服务启动失败。
- `.docker/environment/mysql.env`、`web/web-admin/src/main/resources/application.yml`：当前被跟踪且含字面量秘密，迁移时必须确保示例与忽略规则同时落地。
- Elasticsearch/MySQL 数据卷：镜像版本选择错误可能不可逆写入；启动新版本前设置独立备份/克隆回滚点。
- Redis ACL：权限过窄会造成运行时失败，过宽则达不到最小权限目标；需要集成测试而非只做静态配置。
- TLS：证书 SAN、CA chain 或 truststore 配置错误会阻断应用启动；保留上一版配置和证书引用作为回滚点，不回退到 trust-all。

## Pre-start gates

- [x] 用户确认本地 Compose 不启用 TLS。
- [x] 最终 planning summary 已展示并于 2026-08-31 获得后续明确批准。
- [x] `implement.jsonl` 与 `check.jsonl` 已加入真实 spec 条目并通过校验。
- [x] 未启动容器或修改数据卷；已确认现有 `nine-*` 容器属于另一份 WSL 工作区，当前仓库没有可用的本地 Docker 运行态兼容性基线。
- [ ] 在目标工作区启用 Compose 前，确认实际持久化服务版本、备份位置与恢复方式；禁止直接用新主版本试探旧数据卷。

## Verification evidence

- `docker compose -f .docker/docker-compose.yaml config --quiet`：通过。
- `docker compose -f .docker/docker-compose.yaml --profile kibana config --quiet`：通过。
- `.docker/verify-compose-security.ps1`：通过。
- 隔离 `redis:8.4.6`：匿名访问拒绝；具名 ACL 用户读写、Lua、事务和发布订阅能力可用；`CONFIG`、`ACL` 拒绝。
- `.\\mvnw.cmd -pl web/web-app,web/web-admin -am test`：通过；`web-app` 423、`web-admin` 150，失败与错误均为 0。
- 未启动或迁移现有数据卷。本机已停止的 Elasticsearch 容器是 9.2.1，而本项目基线为 8.18.8，必须使用新卷或完成逻辑备份/迁移后再做动态验证。
