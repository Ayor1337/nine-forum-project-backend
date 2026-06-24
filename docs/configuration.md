# 配置说明

本项目配置集中在各模块 `application.yml`。当前文件包含本地开发参数；生产、测试和共享环境不应提交真实凭据，应通过环境变量、配置中心或部署平台注入。

## 配置文件

| 文件 | 用途 |
| --- | --- |
| `web/web-app/src/main/resources/application.yml` | 用户端应用配置，端口 `9966`。 |
| `web/web-admin/src/main/resources/application.yml` | 管理端应用配置，端口 `9977`。 |
| `model/src/main/resources/application.yml` | 模型模块基础应用名配置。 |

## 应用与服务端口

| key | 说明 |
| --- | --- |
| `spring.application.name` | 应用名，当前为 NineForum 后端统一应用名。 |
| `server.port` | Web 应用监听端口，用户端为 `9966`，管理端为 `9977`。 |

## MySQL

| key | 说明 |
| --- | --- |
| `spring.datasource.driver-class-name` | MySQL JDBC 驱动。 |
| `spring.datasource.url` | MySQL 连接地址，本地库名为 `nine_forum`。 |
| `spring.datasource.username` | 数据库用户名。 |
| `spring.datasource.password` | 数据库密码，生产环境必须外部注入。 |

本地 Docker MySQL 暴露端口为 `16033`，容器内端口为 `3306`。

## Redis

| key | 说明 |
| --- | --- |
| `spring.data.redis.port` | Redis 端口，本地为 `16379`。 |
| `spring.data.redis.database` | Redis database，用户端当前配置为 `0`。 |

Redis 用于缓存、JWT 黑名单/失效状态、Passkey challenge、未读消息等临时数据。新增 Redis key 时应在对应 Service 中保持命名清晰，并补充测试。

## JWT 与认证

| key | 说明 |
| --- | --- |
| `spring.security.jwt.secret-key` | JWT 签名密钥，必须外部注入。 |
| `spring.security.jwt.expire` | JWT 过期时间配置，当前工具类按项目实现解释。 |

JWT 配置被用户端和管理端共同使用。调整签发逻辑时，需要同步检查登录、登出、JWT 过滤器、会话撤销和测试。

## WebAuthn / Passkey

用户端配置：

| key | 说明 |
| --- | --- |
| `spring.security.webauthn.rp-id` | Relying Party ID，本地为 `localhost`。 |
| `spring.security.webauthn.rp-name` | Relying Party 展示名。 |
| `spring.security.webauthn.allowed-origins` | 允许发起 WebAuthn 的前端 origin。 |
| `spring.security.webauthn.challenge-expire-seconds` | challenge 过期秒数。 |

生产环境必须按真实域名配置 `rp-id` 和 `allowed-origins`，否则浏览器 WebAuthn 校验会失败。

## MinIO

| key | 说明 |
| --- | --- |
| `spring.minio.endpoint` | MinIO API 地址，本地为 `http://localhost:9000`。 |
| `spring.minio.access-key` | MinIO access key，生产环境必须外部注入。 |
| `spring.minio.secret-key` | MinIO secret key，生产环境必须外部注入。 |
| `spring.minio.bucket` | 默认 bucket，当前为 `nineforum`。 |

本地 MinIO Console 默认暴露在 `9001`。首次使用前需要确认 bucket 已创建。

## RabbitMQ

| key | 说明 |
| --- | --- |
| `spring.rabbitmq.host` | RabbitMQ 主机。 |
| `spring.rabbitmq.port` | RabbitMQ AMQP 端口，当前配置为 `5672`。 |
| `spring.rabbitmq.username` | RabbitMQ 用户名。 |
| `spring.rabbitmq.password` | RabbitMQ 密码，生产环境必须外部注入。 |
| `spring.rabbitmq.virtual-host` | 虚拟主机，当前为 `/nine_forum`。 |
| `spring.rabbitmq.listener.simple.acknowledge-mode` | 监听器确认模式，当前为 `manual`。 |

Compose 文件使用 `rabbitmq:management`，如果本地连接失败，优先检查虚拟主机是否存在。

## 邮件

用户端邮件配置：

| key | 说明 |
| --- | --- |
| `spring.mail.host` | SMTP 主机。 |
| `spring.mail.username` | SMTP 用户名，生产环境必须外部注入。 |
| `spring.mail.password` | SMTP 授权码或密码，生产环境必须外部注入。 |

邮件主要用于注册验证。不要在文档、提交记录或测试数据中复制真实授权码。

## Elasticsearch 与 OpenAPI

| key | 说明 |
| --- | --- |
| `spring.elasticsearch.uris` | Elasticsearch 地址，本地为 `http://localhost:9200`。 |
| `springdoc.api-docs.path` | OpenAPI JSON 路径，当前为 `/v3/api-docs`。 |
| `springdoc.swagger-ui.path` | Swagger UI 路径，当前为 `/swagger-ui.html`。 |
| `knife4j.enable` | 是否启用 Knife4j。 |

本地 Elasticsearch 使用 IK Analyzer 插件，Compose 启动时会尝试安装并初始化配置。

## 敏感配置治理

- 不要提交真实数据库、Redis、RabbitMQ、MinIO、SMTP、JWT 密钥或生产端点。
- 本地默认值只能用于开发环境，不应复用到共享环境。
- 推荐使用环境变量覆盖敏感 key，并在部署文档中记录变量名、用途和是否必填。
