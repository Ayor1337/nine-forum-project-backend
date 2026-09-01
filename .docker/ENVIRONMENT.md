# Docker 环境变量

本目录的 Compose 配置分两类环境变量：

- `.docker/.env`：只用于 Compose 插值，目前包含持久化数据根目录 `FORUM_HOME`。
- `.docker/environment/*.env`：注入对应容器，包含服务账号、密码和初始化参数。

实际 `.env` 文件都不会被 Git 跟踪。首次使用时从示例复制，所有 `CHANGE_ME` 必须替换为本地值：

```bash
cp .docker/.env.example .docker/.env
for service in mysql redis minio rabbitmq elasticsearch kibana; do
  cp ".docker/environment/${service}.env.example" ".docker/environment/${service}.env"
done
```

## Compose 路径

配置文件：`.docker/.env`

| 变量 | 必填 | 敏感 | 用途 | 示例 |
| --- | --- | --- | --- | --- |
| `FORUM_HOME` | 是 | 否 | 所有服务持久化数据的宿主机根目录，必须是绝对路径且不要包含末尾斜杠 | WSL：`/home/user/docker_volumes/nine_forum`；PowerShell：`E:/docker_volumes/nine_forum` |

Compose 会使用以下子目录：

- `${FORUM_HOME}/mysql`
- `${FORUM_HOME}/redis`
- `${FORUM_HOME}/minio/data`
- `${FORUM_HOME}/minio/config`
- `${FORUM_HOME}/rabbitmq`
- `${FORUM_HOME}/elastic/data`
- `${FORUM_HOME}/elastic/plugins`

直接执行 `docker compose` 时必须通过当前进程环境或 `--env-file .docker/.env` 提供 `FORUM_HOME`。两个启动脚本是例外：`start-local.sh` 会固定设置 `FORUM_HOME=/docker_volumes/nine_forum`，继续使用原 WSL 路径；`start-local.ps1` 会固定设置为执行脚本时工作目录下的 `volumes` 绝对路径，并在启动服务前自动创建该目录。例如从仓库根目录执行时使用 `<仓库>/volumes`。两个脚本都会启动 MySQL、Redis、MinIO、RabbitMQ、Elasticsearch 和 `elasticsearch-init`。

修改 `FORUM_HOME` 只会改变挂载位置，不会迁移已有数据。切换路径前必须停止服务并自行迁移数据。

## MySQL

配置文件：`.docker/environment/mysql.env`

| 变量 | 必填 | 敏感 | 用途 | 示例/约束 |
| --- | --- | --- | --- | --- |
| `MYSQL_ROOT_PASSWORD` | 是 | 是 | MySQL `root` 初始化密码和健康检查凭据 | 使用本地随机强密码 |
| `MYSQL_DATABASE` | 是 | 否 | 首次初始化时创建的数据库 | `nine_forum` |
| `MYSQL_USER` | 是 | 否 | 首次初始化时创建的应用账号 | `nine_forum_app` |
| `MYSQL_PASSWORD` | 是 | 是 | MySQL 应用账号密码 | 使用本地随机强密码 |
| `LANG` | 否 | 否 | 容器语言环境 | `C.UTF-8` |

`MYSQL_DATABASE`、账号和 `.docker/image/mysql/nine_forum_schema.sql` 只会在 `${FORUM_HOME}/mysql` 为空时初始化。已有数据目录不会重复执行。

## Redis

配置文件：`.docker/environment/redis.env`

| 变量 | 必填 | 敏感 | 用途 | 示例/约束 |
| --- | --- | --- | --- | --- |
| `REDIS_USERNAME` | 是 | 否 | Redis ACL 应用账号 | `nineforum-app` |
| `REDIS_PASSWORD` | 是 | 是 | Redis ACL 密码 | 使用本地随机强密码 |

默认 Redis 用户会被禁用；该账号只获得连接、读写、键空间、脚本、事务和发布订阅权限。

## MinIO

配置文件：`.docker/environment/minio.env`

| 变量 | 必填 | 敏感 | 用途 | 示例/约束 |
| --- | --- | --- | --- | --- |
| `MINIO_ROOT_USER` | 是 | 是 | 本地 MinIO 初始化管理员账号 | 仅限本地开发使用 |
| `MINIO_ROOT_PASSWORD` | 是 | 是 | 本地 MinIO 初始化管理员密码 | 使用本地随机强密码 |

## RabbitMQ

配置文件：`.docker/environment/rabbitmq.env`

| 变量 | 必填 | 敏感 | 用途 | 示例/约束 |
| --- | --- | --- | --- | --- |
| `RABBITMQ_DEFAULT_USER` | 是 | 否 | 首次初始化时创建的应用账号 | `nineforum-app` |
| `RABBITMQ_DEFAULT_PASS` | 是 | 是 | RabbitMQ 应用账号密码 | 使用本地随机强密码 |
| `RABBITMQ_DEFAULT_VHOST` | 是 | 否 | 应用账号使用的虚拟主机 | `/nine_forum` |

## Elasticsearch

配置文件：`.docker/environment/elasticsearch.env`

| 变量 | 必填 | 敏感 | 用途 | 示例/约束 |
| --- | --- | --- | --- | --- |
| `ELASTIC_PASSWORD` | 是 | 是 | Elasticsearch 内置 `elastic` bootstrap 密码，仅用于健康检查和初始化 | 使用本地随机强密码 |
| `ELASTICSEARCH_APP_USERNAME` | 是 | 否 | `elasticsearch-init` 创建的应用账号 | `nineforum_app` |
| `ELASTICSEARCH_APP_PASSWORD` | 是 | 是 | Elasticsearch 应用账号密码 | 使用本地随机强密码 |
| `ELASTICSEARCH_APP_ROLE` | 是 | 否 | 应用角色名称，仅允许访问 `thread` 和 `search_log` 索引 | `nineforum_app` |

应用配置中的 `ELASTICSEARCH_USERNAME`、`ELASTICSEARCH_PASSWORD` 必须分别对应这里的 `ELASTICSEARCH_APP_USERNAME`、`ELASTICSEARCH_APP_PASSWORD`。

## Kibana

配置文件：`.docker/environment/kibana.env`

| 变量 | 必填 | 敏感 | 用途 | 示例/约束 |
| --- | --- | --- | --- | --- |
| `ELASTICSEARCH_SERVICEACCOUNTTOKEN` | 是 | 是 | Kibana 连接 Elasticsearch 的 service-account token | Elasticsearch 启动后按示例文件中的命令生成 |
| `XPACK_ENCRYPTEDSAVEDOBJECTS_ENCRYPTIONKEY` | 是 | 是 | 加密 Kibana saved objects | 至少 32 个字符，使用独立随机值 |
| `XPACK_SECURITY_ENCRYPTIONKEY` | 是 | 是 | 加密 Kibana 会话和安全数据 | 至少 32 个字符，使用独立随机值 |
| `XPACK_REPORTING_ENCRYPTIONKEY` | 是 | 是 | 加密 Kibana reporting 数据 | 至少 32 个字符，使用独立随机值 |

Kibana 位于 `kibana` profile 中，只有显式指定 `--profile kibana` 才会启动。

## Compose 内固定环境变量

以下值直接写在 `.docker/docker-compose.yaml`，通常不需要本地覆盖：

| 服务 | 变量 | 固定值 | 用途 |
| --- | --- | --- | --- |
| Elasticsearch | `discovery.type` | `single-node` | 本地单节点模式 |
| Elasticsearch | `xpack.security.enabled` | `true` | 开启本地认证 |
| Elasticsearch | `ES_JAVA_OPTS` | `-Xms1g -Xmx1g` | JVM 初始和最大堆内存 |
| Kibana | `ELASTICSEARCH_HOSTS` | `http://nine-elasticsearch:9200` | 容器网络内的 Elasticsearch 地址 |

## 安全规则

- 不要提交 `.docker/.env` 或 `.docker/environment/*.env`。
- 不要在日志、Issue、提交信息或截图中暴露密码、token 和 encryption key。
- 所有 `CHANGE_ME`、空值或未配置的必填凭据都会被 Compose 凭据预检阻止。
- 本配置仅供本地开发；生产环境应使用外部 secret 管理和最小权限账号。
