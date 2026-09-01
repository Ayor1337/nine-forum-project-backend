# NineForum 后端

NineForum 是一个基于 Spring Boot 的论坛后端项目，提供面向用户端和管理端的 API 服务。项目采用多模块 Maven 结构，将通用能力、领域模型和 Web 应用拆分管理，便于独立维护和复用。

## 技术栈

- Java 17
- Spring Boot 3.5.5
- Maven Wrapper
- Spring Web / Validation / AOP / Security
- MyBatis-Plus
- MySQL
- Redis
- RabbitMQ
- MinIO
- Elasticsearch / Kibana
- WebSocket / STOMP
- WebAuthn4J
- Lombok

## 模块结构

```text
.
├── common            # 通用工具、配置、结果封装、JWT、MinIO、图片处理等
├── model             # 公共实体、DTO、VO、枚举和文档模型
├── web
│   ├── web-app       # 用户端论坛 API
│   └── web-admin     # 管理端 API
├── .docker           # 本地依赖服务的 Docker Compose 与初始化资源
├── pom.xml           # Maven 聚合工程
└── mvnw              # Maven Wrapper
```

## 主要能力

### 用户端 `web-app`

- 账号认证、JWT 授权、Passkey / WebAuthn
- 用户资料、用户搜索、关注关系与隐私设置
- 主题、话题、帖子、标签、收藏、点赞
- 站内通知、系统消息、提及消息、未读消息
- 私信会话、聊天室、WebSocket 推送
- 搜索、热词、内容图片、贴纸与公告

默认端口：`9966`

### 管理端 `web-admin`

- 仪表盘与统计数据
- 用户、角色、权限与权限操作日志
- 主题、话题、帖子、标签、收藏、点赞管理
- 举报处理、广播、页面公告、图片资源管理
- 会话消息、聊天室历史、数据修复任务

默认端口：`9977`

## 环境要求

- JDK 17
- Docker 与 Docker Compose
- Bash 或兼容 Shell

建议使用仓库内置的 Maven Wrapper，避免本机 Maven 版本差异。

## 本地依赖服务

本 Compose 仅用于本机开发：所有宿主端口都绑定 `127.0.0.1`，本地 Redis/Elasticsearch 启用认证但不启用 TLS。不要把它作为跨主机或生产部署文件使用。

完整环境变量清单见 [`.docker/ENVIRONMENT.md`](.docker/ENVIRONMENT.md)。

首次使用先创建未跟踪的 Compose 路径配置和本地凭据文件（并把 `CHANGE_ME` 值替换为实际值）：

```bash
cp .docker/.env.example .docker/.env
for service in mysql redis minio rabbitmq elasticsearch kibana; do
  cp ".docker/environment/${service}.env.example" ".docker/environment/${service}.env"
done
```

PowerShell：

```powershell
Copy-Item .docker/.env.example .docker/.env
foreach ($service in "mysql", "redis", "minio", "rabbitmq", "elasticsearch", "kibana") {
  Copy-Item ".docker/environment/$service.env.example" ".docker/environment/$service.env"
}
```

编辑 `.docker/.env`，把 `FORUM_HOME` 设置为持久化数据根目录的绝对路径，不要添加末尾斜杠。WSL 可使用 `/home/<用户名>/docker_volumes/nine_forum`，PowerShell 可使用 `E:/docker_volumes/nine_forum`；后续命令必须在路径所属的同一环境中运行。变量化不会迁移已有数据：要继续使用原目录，应设置 `FORUM_HOME=/docker_volumes/nine_forum`；切换到新路径前必须先停止服务并完成数据迁移。

先启动不依赖 Kibana service token 的服务：

```bash
docker compose --env-file .docker/.env -f .docker/docker-compose.yaml up -d mysql redis minio rabbitmq elasticsearch elasticsearch-init
```

启动脚本只读取并检查 MySQL、Redis、MinIO、RabbitMQ、Elasticsearch 的 env 文件，不会创建、迁移或修改任何凭据。缺少文件、字段为空或字段仍以 `CHANGE_ME` 开头时，脚本会列出具体文件和字段后退出：

```powershell
# 只检查 .docker/environment/*.env
.\.docker\start_pwsh.ps1 -CheckOnly

# 检查通过后启动基础设施
.\.docker\start_pwsh.ps1
```

Linux / WSL：

```bash
# 只检查 .docker/environment/*.env
bash ./.docker/start_bash.sh --check-only

# 检查通过后启动基础设施
bash ./.docker/start_bash.sh
```

`start_bash.sh` 会固定设置 `FORUM_HOME=/docker_volumes/nine_forum`，沿用原有 WSL 数据路径；启动时会通过 `sudo` 将 Elasticsearch 数据与插件目录准备为容器用户可写。`start_pwsh.ps1` 会把 `FORUM_HOME` 固定为执行脚本时工作目录下的 `volumes` 绝对路径并自动创建该目录；从仓库根目录执行时数据保存在 `<仓库>/volumes`。两个脚本都会启动 MySQL、Redis、MinIO、RabbitMQ、Elasticsearch 8.18.8 和 `elasticsearch-init`。原有 9.2.1 named volume 不会被自动挂载或删除。Elasticsearch 启动后，启动 `web-app` 即会全量重建索引。

Compose 会先运行凭据预检；任何空值或仍为 `CHANGE_ME` 的基础设施凭据都会阻止依赖服务启动。可在启动前运行不输出配置值的静态检查：

```powershell
.docker/verify-compose-security.ps1
```

Elasticsearch 健康后生成 Kibana service token，将输出的 token 写入未跟踪的 `.docker/environment/kibana.env`，再启动 Kibana：

```bash
docker compose --env-file .docker/.env -f .docker/docker-compose.yaml exec elasticsearch \
  bin/elasticsearch-service-tokens create elastic/kibana nineforum-kibana
docker compose --env-file .docker/.env -f .docker/docker-compose.yaml --profile kibana up -d kibana
```

Kibana 使用 `kibana` profile，避免在 token 尚未生成时以无效凭据启动。

| 服务 | 回环端口 | 说明 |
| --- | --- | --- |
| MySQL | `127.0.0.1:16033` | 数据库 `nine_forum` |
| Redis | `127.0.0.1:16379` | ACL 用户 `REDIS_USERNAME`，默认用户已禁用；仅授予连接、读写、键空间、脚本、事务和发布订阅类别 |
| MinIO API | `127.0.0.1:9000` | 本地 root 凭据仅用于开发/初始化 |
| MinIO Console | `127.0.0.1:9001` | 本地控制台 |
| RabbitMQ | `127.0.0.1:5672` | AMQP；管理 UI 不发布到宿主 |
| Elasticsearch | `127.0.0.1:9200` | Security 开启，仅回环 HTTP；不发布 transport `9300` |
| Kibana | `127.0.0.1:5601` | 使用独立 service-account token |

Elasticsearch 首次启动后，执行 `kibana.env.example` 中的 service-token 命令，生成 token 后写入未跟踪的 `kibana.env`。`elasticsearch-init` 会用 bootstrap `elastic` 身份创建本地应用管理员角色和用户；该角色拥有全部集群权限和全部普通索引权限，但不能访问受限系统索引，仅限本地开发使用。应用配置中的 `ELASTICSEARCH_USERNAME`/`ELASTICSEARCH_PASSWORD` 必须与该用户一致。

MySQL 初始化 SQL 位于 `.docker/image/mysql/nine_forum_schema.sql`，Compose 会将它只读挂载到 `/docker-entrypoint-initdb.d/`。官方 MySQL 入口脚本仅在 `${FORUM_HOME}/mysql` 为空时创建 `MYSQL_DATABASE` 并执行初始化 SQL；已有数据目录不会重复执行。Compose 镜像已固定为具体版本（MySQL 9.5.0、Redis 8.4.6、RabbitMQ 4.2.1-management、Elasticsearch/Kibana 8.18.8、现有 MinIO release）；当前仓库未锁定 registry digest。现有 MySQL bind volume 已由运行日志确认曾使用 9.5.0，Redis bind volume 已由运行日志确认曾使用 8.4.0，因此本地基线固定为同一主次版本的补丁版本。迁移到其它主版本必须先做逻辑导出，再使用新数据目录导入，禁止将旧数据目录直接挂到不兼容版本。

## 配置说明

用户端和管理端真实配置只保存在本机，不受 Git 跟踪。复制示例后填写环境变量引用的本地值：

```bash
cp web/web-app/src/main/resources/application.example.yml web/web-app/src/main/resources/application.yml
cp web/web-admin/src/main/resources/application.example.yml web/web-admin/src/main/resources/application.yml
```

PowerShell：

```powershell
Copy-Item web/web-app/src/main/resources/application.example.yml web/web-app/src/main/resources/application.yml
Copy-Item web/web-admin/src/main/resources/application.example.yml web/web-admin/src/main/resources/application.yml
```

上述复制命令只适用于新克隆；已有本地配置请先备份并手动合并新增键，避免覆盖本机凭据。`.gitignore` 已覆盖两个真实配置文件和所有本地 `.env` 文件，示例文件保持跟踪。

两个应用均要求 DB、Redis ACL、JWT、MinIO、RabbitMQ 凭据；用户端另外要求 Elasticsearch 和 SMTP 凭据。缺少必需变量时应用应启动失败。示例中的 SMTP 已强制 STARTTLS、证书链和服务器主机名校验；本地 RabbitMQ/Redis/MinIO/Elasticsearch 仅因绑定回环而允许明文。

生产环境必须使用私网和外部秘密管理：Redis ACL、MinIO scoped service account、独立 RabbitMQ vhost/用户、独立 Elasticsearch 应用/Kibana 身份，并为 Redis、MinIO、Elasticsearch、RabbitMQ 和 SMTP 启用 TLS、证书链及主机名校验。RabbitMQ 的 Spring 配置可通过 `SPRING_RABBITMQ_SSL_ENABLED=true` 和 `SPRING_RABBITMQ_SSL_BUNDLE=<bundle-name>` 注入 SSL bundle，也可注入 `SPRING_RABBITMQ_SSL_TRUST_STORE`、`SPRING_RABBITMQ_SSL_TRUST_STORE_PASSWORD` 等 truststore 属性；Redis 的自定义连接工厂会读取 `SPRING_DATA_REDIS_SSL_ENABLED=true`，并强制 Lettuce `FULL` 对端校验，生产运行时必须提供包含服务 CA 的 JVM/SSL truststore。不要在配置中加入 trust-all 或把证书、私钥、truststore 提交到 Git。可能曾经通过明文链路传输或出现在 Git 历史的 MySQL、Redis、MinIO、RabbitMQ、Elasticsearch、SMTP、JWT 凭据必须轮换；历史清理需另行评估，不能代替轮换。

## 构建与测试

在仓库根目录执行：

```bash
./mvnw clean test
```

打包全部模块：

```bash
./mvnw clean package
```

仅构建用户端模块及其依赖：

```bash
./mvnw -pl web/web-app -am package
```

仅构建管理端模块及其依赖：

```bash
./mvnw -pl web/web-admin -am package
```

## 启动应用

启动用户端服务：

```bash
./mvnw -pl web/web-app -am spring-boot:run
```

启动管理端服务：

```bash
./mvnw -pl web/web-admin -am spring-boot:run
```

启动后访问：

- 用户端 API：`http://localhost:9966`
- 管理端 API：`http://localhost:9977`

## 常用开发命令

```bash
# 查看所有模块是否能通过测试
./mvnw clean test

# 启动本地依赖
docker compose --env-file .docker/.env -f .docker/docker-compose.yaml up -d

# 停止本地依赖
docker compose --env-file .docker/.env -f .docker/docker-compose.yaml down

# 只运行 web-app 测试
./mvnw -pl web/web-app -am test

# 只运行 web-admin 测试
./mvnw -pl web/web-admin -am test
```
