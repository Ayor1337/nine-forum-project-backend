# 本地配置与秘密规范

## 1. 适用范围 / 触发条件

修改 Spring Boot 配置、数据库/JWT/MinIO/RabbitMQ/SMTP 凭据、示例配置或 Git 忽略规则时适用。目标是允许本地开发保存实际值，同时保证 Git 当前树、示例、日志和审查输出不包含这些值。

## 2. 文件与命令签名

- 本地用户端配置：`web/web-app/src/main/resources/application.yml`，必须存在于开发机但不受 Git 跟踪。
- 用户端示例配置：`web/web-app/src/main/resources/application.example.yml`，必须受 Git 跟踪且不得含真实凭据。
- 本地管理端配置：`web/web-admin/src/main/resources/application.yml`，必须存在于开发机但不受 Git 跟踪。
- 管理端示例配置：`web/web-admin/src/main/resources/application.example.yml`，必须受 Git 跟踪且不得含真实凭据。
- 本地基础设施凭据：`.docker/environment/*.env`，必须存在于开发机但不受 Git 跟踪；对应的 `*.env.example` 必须受 Git 跟踪且不得含真实凭据。
- 初始化命令（PowerShell）：

以下命令只适用于新克隆；已有本地文件必须先备份并手动合并新增键。

```powershell
Copy-Item web/web-app/src/main/resources/application.example.yml web/web-app/src/main/resources/application.yml
Copy-Item web/web-admin/src/main/resources/application.example.yml web/web-admin/src/main/resources/application.yml
Copy-Item .docker/environment/mysql.env.example .docker/environment/mysql.env
Copy-Item .docker/environment/redis.env.example .docker/environment/redis.env
Copy-Item .docker/environment/minio.env.example .docker/environment/minio.env
Copy-Item .docker/environment/rabbitmq.env.example .docker/environment/rabbitmq.env
Copy-Item .docker/environment/elasticsearch.env.example .docker/environment/elasticsearch.env
Copy-Item .docker/environment/kibana.env.example .docker/environment/kibana.env
```

- 初始化命令（Bash）：

```bash
cp web/web-app/src/main/resources/application.example.yml web/web-app/src/main/resources/application.yml
cp web/web-admin/src/main/resources/application.example.yml web/web-admin/src/main/resources/application.yml
cp .docker/environment/mysql.env.example .docker/environment/mysql.env
cp .docker/environment/redis.env.example .docker/environment/redis.env
cp .docker/environment/minio.env.example .docker/environment/minio.env
cp .docker/environment/rabbitmq.env.example .docker/environment/rabbitmq.env
cp .docker/environment/elasticsearch.env.example .docker/environment/elasticsearch.env
cp .docker/environment/kibana.env.example .docker/environment/kibana.env
```

## 3. 配置契约

- `.gitignore` 必须包含 `/web/web-app/src/main/resources/application.yml`、`/web/web-admin/src/main/resources/application.yml` 和 `/.docker/environment/*.env`，不得忽略 `application.example.yml` 或 `*.env.example`。
- 两个应用各自的本地文件与示例文件必须具有相同 YAML 键集合；示例的敏感键只能使用环境变量占位符或明确的非秘密示例值。
- Compose 的每个 `env_file` 都必须指向被忽略的本地文件；生产环境可改由部署平台注入，但不能把 `.env` 或凭据写入 Compose。
- 本地 Compose 允许回环 HTTP/AMQP，不生成或挂载 TLS 材料；SMTP 始终必须启用 STARTTLS（或显式隐式 TLS）并校验证书主机名。
- 生产覆盖配置必须为 RabbitMQ、Redis、MinIO、Elasticsearch 启用 TLS、证书链和主机名校验，禁止 trust-all；凭据使用外部秘密管理，并使用独立的应用、Kibana、service-account 和 RabbitMQ vhost 身份。
- 两个应用的自定义 Redis 连接工厂必须转发 username/password，并读取 `spring.data.redis.ssl.enabled`；启用后必须使用 Lettuce `FULL` 对端校验，生产信任材料由外部 JVM/SSL truststore 提供。
- 新增、删除或重命名配置键时必须同步两个文件；审查只比较键、类型和占位符规则，不打印本地值。
- 任何凭据扫描只能输出文件名、命中数量或摘要，不能输出匹配文本。

### 本地 Compose 安全基线

- 启动签名：`docker compose -f .docker/docker-compose.yaml up -d`；Kibana 仅通过 `--profile kibana` 显式启动。
- 所有发布到宿主的端口必须写成 `127.0.0.1:<host>:<container>`；MySQL `16033`、Redis `16379`、MinIO `9000/9001`、RabbitMQ `5672`、Elasticsearch `9200`、Kibana `5601`。RabbitMQ 管理端口 `15672` 和 Elasticsearch transport 端口 `9300` 不得发布。
- `credentials-preflight` 必须在业务容器前检查 `MYSQL_ROOT_PASSWORD`、`MYSQL_USER`、`MYSQL_PASSWORD`、`REDIS_USERNAME`、`REDIS_PASSWORD`、`MINIO_ROOT_USER`、`MINIO_ROOT_PASSWORD`、`RABBITMQ_DEFAULT_USER`、`RABBITMQ_DEFAULT_PASS`、`RABBITMQ_DEFAULT_VHOST`、`ELASTIC_PASSWORD`；空值或 `CHANGE_ME*` 必须让启动失败。
- Kibana profile 还必须检查 `ELASTICSEARCH_SERVICEACCOUNTTOKEN` 与三个独立的 32 字符以上加密键：`XPACK_SECURITY_ENCRYPTIONKEY`、`XPACK_ENCRYPTEDSAVEDOBJECTS_ENCRYPTIONKEY`、`XPACK_REPORTING_ENCRYPTIONKEY`。
- Redis 必须关闭 `default` 用户，只启用具名 ACL 用户；应用用户仅允许连接、读写、键空间、脚本、事务和发布订阅类别，不得授予 `@admin`、`CONFIG` 或 `ACL`。
- Elasticsearch 必须启用 Security；初始化容器为应用创建只覆盖 `thread` 与 `search_log` 索引的独立角色/用户，Kibana 使用 service-account token，应用不得使用 `elastic` 超级用户。
- 本地不启用 TLS 是明确边界，只允许回环访问；任何跨宿主环境必须使用独立生产覆盖配置，不能放宽本文件的回环绑定来代替生产部署设计。

## 4. 验证与错误矩阵

| 条件 | 结果 |
| --- | --- |
| 任一本地 `application.yml`/`.env` 未被忽略或仍在索引中 | 拒绝提交 |
| 示例 YAML 无法解析，或同一应用的键集合与本地文件不同 | 拒绝提交 |
| 示例敏感键复用了本地值 | 视为秘密泄露，立即移除并检查历史 |
| 新克隆缺少本地配置 | 按 README 复制示例后填写本机值 |
| 检查命令会打印配置值 | 改为计数、摘要或仅键名检查 |
| 任一 Compose 凭据为空或仍为 `CHANGE_ME*` | `credentials-preflight` 非零退出，依赖服务不得启动 |
| Redis 匿名连接 | 拒绝认证；具名用户 `PING`、读写和脚本命令可用 |
| Redis 应用用户执行 `CONFIG` 或 `ACL` | 权限拒绝 |
| Elasticsearch 匿名访问根接口 | 返回认证失败；应用身份只能访问约定索引 |
| 宿主端口绑定到 `0.0.0.0`、`[::]` 或省略 host IP | 静态检查失败，拒绝提交 |
| 本地配置请求 TLS | Redis 使用 `FULL` 校验；RabbitMQ/SMTP 保持证书链及主机名校验 |

## 5. Good / Base / Bad Cases

- Good：本地文件存在且被忽略，示例受跟踪、键完整、敏感值为空占位符。
- Base：新克隆只有示例文件，开发者复制后填写本机配置再启动。
- Bad：把本地文件强制加入 Git，或直接复制真实文件作为示例。

## 6. 必需测试

- `git check-ignore`：断言本地配置被精确规则命中。
- `git ls-files`：断言本地配置未跟踪、示例配置已跟踪或位于待提交集合。
- YAML 解析：断言两个文件都可解析，键集合相等。
- 敏感键检查：断言示例未复用本地敏感值；输出仅含计数。
- `./mvnw.cmd -pl web/web-app -am test`：断言模块及依赖测试通过。
- `.\\mvnw.cmd -pl web/web-app,web/web-admin -am test`：断言两个应用的 Redis username/password、TLS `FULL` 校验和 RabbitMQ/SMTP 安全属性契约通过。
- `docker compose -f .docker/docker-compose.yaml config --quiet` 与带 `--profile kibana` 的同类命令：断言两个 Compose 视图都能解析。
- `.docker/verify-compose-security.ps1`：断言端口只绑定回环、镜像不使用 `latest`、Redis/Elasticsearch 安全开关及凭据门禁存在。
- 隔离 Redis 动态测试：断言匿名 `PING` 被拒绝；具名用户允许 `PING`、读写、`EVAL`/`EVALSHA`/`SCRIPT LOAD`，拒绝 `CONFIG` 与 `ACL`。

## 7. Wrong vs Correct

### Wrong

```text
web/web-app/src/main/resources/application.yml  # 含本机值且受 Git 跟踪
```

### Correct

```text
web/web-app/src/main/resources/application.yml          # 本机保留、Git 忽略
web/web-app/src/main/resources/application.example.yml  # 安全示例、Git 跟踪
web/web-admin/src/main/resources/application.yml         # 本机保留、Git 忽略
web/web-admin/src/main/resources/application.example.yml # 安全示例、Git 跟踪
.docker/environment/mysql.env                            # 本机保留、Git 忽略
.docker/environment/mysql.env.example                    # 安全示例、Git 跟踪
```
