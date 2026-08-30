# 静态代码安全审计证据

## 范围与方法

- 审查时间：2026-08-20（Asia/Taipei）
- 范围：`common`、`model`、`web/web-app`、`web/web-admin`、MyBatis/XML、应用配置与 `.docker`。
- 方法：入口枚举、SecurityFilterChain/过滤器/方法注解复核、Controller → Service → Mapper/对象存储数据流追踪、配置交叉比较。
- 安全限制：未执行动态请求、网络扫描、数据库写入或产品代码修改；未在本报告复制任何密码、令牌或密钥值。
- 严重/高危发现均进行了第二次调用链复核，复核证据写在对应条目中。

## 发现摘要

| 编号 | 严重级别 | 标题 | 置信度 |
| --- | --- | --- | --- |
| STATIC-01 | 严重 | 管理端 HTTP 安全链全局 `permitAll`，匿名请求可执行系统级管理操作 | 确定 |
| STATIC-02 | 高危 | 私有 Sticker 未校验所有权/可见性，可按递增资源 ID 水平越权读取和收藏 | 确定 |
| STATIC-03 | 高危 | 未认证 STOMP 客户端可直接向 broker 广播目的地发送伪造事件 | 确定 |
| STATIC-04 | 高危 | 管理端 STOMP 接受普通用户端 JWT，泄露实时举报数据 | 确定 |
| STATIC-05 | 高危 | 运行时配置提交了多类可复用秘密，JWT 签名密钥同时用于两端 | 高 |
| STATIC-06 | 高危 | Docker 基础设施默认向宿主机所有接口暴露无认证或静态凭据服务 | 高（外部可达性取决于宿主防火墙） |
| STATIC-07 | 中危 | 注册邮件入口无滥用控制，邮箱 JWT Redis TTL 被错误设置为约数十年 | 确定 |
| STATIC-08 | 中危 | 修改密码只撤销当前 JWT，其他已登录/被盗会话继续有效 | 高 |
| STATIC-09 | 中危 | 多个公开分页入口无最大页大小并使用任意参数构造缓存键 | 高 |
| STATIC-10 | 中危 | 图片仅限制压缩字节，解码前无像素上限，可触发解压炸弹型资源耗尽 | 高 |
| STATIC-11 | 低危 | 操作日志无字段脱敏，可能持久化完整图片 Base64 与业务入参 | 高 |
| STATIC-12 | 低危 | 认证与授权失败仍返回 HTTP 200，削弱网关防护与安全监控 | 确定 |

## 详细发现

### STATIC-01：管理端 HTTP 安全链全局放行

- 严重级别：严重
- CWE/类别：CWE-306（关键功能缺少认证）、CWE-862（缺少授权）、垂直权限提升
- 证据位置：
  - `web/web-admin/src/main/java/com/ayor/config/SecurityConfiguration.java:49-54`：唯一 `SecurityFilterChain` 对 `anyRequest()` 调用 `permitAll()`。
  - `web/web-admin/src/main/java/com/ayor/controller/RoleController.java:91-115`：账号分配角色、角色分配权限的写接口没有方法级授权。
  - `web/web-admin/src/main/java/com/ayor/controller/PermissionController.java:73-93`：权限批量更新、批量删除直接进入 Service。
  - `web/web-admin/src/main/java/com/ayor/controller/AccountController.java:98-100`：删除账号接口直接进入 Service。
  - `web/web-admin/src/main/java/com/ayor/controller/DataRepairController.java:25-36`：数据补齐和全量重建入口没有授权注解。
  - `web/web-admin/src/main/java/com/ayor/controller/ConversationMessageController.java:32-76`：私信读取、创建、修改、删除均没有授权注解。
- 攻击前提：攻击者能连接管理端 HTTP 端口；不需要账号或 JWT。
- 数据流/二次复核：
  1. URL 请求进入唯一安全链，被 `anyRequest().permitAll()` 放行。
  2. 全仓方法安全注解复核仅发现 `CreditController`、`DecorationController`、`FeedbackController`、`ReportController`、`ShopController` 使用 `@PreAuthorize("isAuthenticated()")`；角色、权限、账号、私信、数据修复等控制器没有补偿性保护。
  3. 以 `POST /api/roles/{roleId}/accounts/{accountId}` 为例，Controller 直接调用 `roleService.addAccountToRole`；以权限批量删除和账号删除为例同样直接进入写 Service。
  4. 因此这不是“仅文档端点被放行”，而是匿名请求可到达系统级写操作和敏感数据读取。
- 影响：匿名用户可读取账号/私信/权限等数据，修改角色和权限、删除账号和内容、触发数据修复或搜索重建，最终取得系统控制或造成大范围数据破坏。
- 现有补偿控制：少数控制器只要求 `isAuthenticated()`，但不要求管理员角色；无法补偿全局放行，且普通用户令牌仍可能通过。
- 修复建议：
  1. 管理端采用默认拒绝：仅登录/健康检查按精确方法与路径放行，其他请求至少 `authenticated()`。
  2. 对角色、权限、账号处罚/删除、积分调整、举报处理、数据修复等操作添加明确管理员角色/权限校验；不要仅使用 `isAuthenticated()`。
  3. 将用户端和管理端身份域分离，并在 Service 边界保留高影响操作的授权断言。
  4. 增加真实 FilterChain/MockMvc 契约测试：匿名为 401、普通用户为 403、具备指定权限的管理员才成功。
- 置信度：确定。

### STATIC-02：私有 Sticker 资源水平越权

- 严重级别：高危
- CWE/类别：CWE-639（用户可控键导致授权绕过）、CWE-862（缺少授权）
- 证据位置：
  - `web/web-app/src/main/java/com/ayor/service/impl/ImageAssetServiceImpl.java:61-68`：用户上传的 Sticker 被标记为 `PRIVATE` 并记录所有者 `accountId`。
  - `web/web-app/src/main/java/com/ayor/controller/StickerController.java:61-64`：客户端可直接按 `assetId` 收藏资源。
  - `web/web-app/src/main/java/com/ayor/service/impl/ImageAssetServiceImpl.java:87-106`：收藏仅检查资源存在、类型和状态，不检查 `visibility` 或所有者。
  - `web/web-app/src/main/java/com/ayor/controller/StickerController.java:91-94`：客户端可直接按 `assetId` 获取详情。
  - `web/web-app/src/main/java/com/ayor/service/impl/ImageAssetServiceImpl.java:145-155`：详情查询不检查可见性或所有权。
  - `web/web-app/src/main/java/com/ayor/entity/vo/StickerVO.java:13-39`：返回对象包含所有者、对象 URL、尺寸和使用统计等信息。
- 攻击前提：攻击者拥有任意普通账号；能猜测/枚举自增 `assetId`。
- 数据流/二次复核：上传链将资产设为 `PRIVATE`，但读取/收藏链仅以 `assetId` 调用 `getById`，随后通过 `BeanUtils.copyProperties` 返回含 URL 的 VO；全链路没有“当前用户是所有者”或“资源为 PUBLIC”的分支。`addStickerByUrl` 也会复用同一缺少可见性校验的收藏逻辑。
- 影响：普通用户可枚举并读取其他用户的私有图片对象地址、将其加入自己的 Sticker 库，构成私有媒体水平越权和隐私泄露。
- 现有补偿控制：要求登录，但任何普通账号均满足；对象名随机不能替代数据库资源授权，因为资源 ID 可枚举。
- 修复建议：建立统一资产访问策略：所有者可访问 `PRIVATE`，非所有者只允许访问明确 `PUBLIC` 且 `ACTIVE` 的资源；查询、收藏、按 URL 导入、内容引用四条链均复用同一授权函数。对外 VO 按需要最小化所有者与内部对象信息，并增加跨账号回归测试。
- 置信度：确定。

### STATIC-03：未认证 STOMP 客户端可伪造 broker 广播

- 严重级别：高危
- CWE/类别：CWE-306（缺少认证）、CWE-862（缺少授权）、消息注入
- 证据位置：
  - `web/web-app/src/main/java/com/ayor/config/WebsocketConfiguration.java:27-30`：四个握手端点允许任意 Origin。
  - `web/web-app/src/main/java/com/ayor/config/WebsocketConfiguration.java:40-47`：简单 broker 直接处理 `/broadcast`、`/transfer`、`/notif`、`/verify`。
  - `web/web-app/src/main/java/com/ayor/interceptor/StompAuthInterceptor.java:67-81`：CONNECT 没有 JWT 时仍被允许。
  - `web/web-app/src/main/java/com/ayor/interceptor/StompAuthInterceptor.java:150-170`：对所有不含 `/transfer` 的 SEND 默认返回 `true`；广播、通知和验证目的地不要求主体。
  - `web/web-app/src/main/java/com/ayor/service/impl/ChatboardHistoryServiceImpl.java:57`、`ForumRealtimeServiceImpl.java:24-25,66-81`、`PageBroadcastEventListener.java:26-37`：服务端实际使用的广播目的地可从代码直接获知。
- 攻击前提：攻击者可连接用户端 WebSocket；不需要 JWT。任意 Origin 允许使浏览器跨站发起更容易。
- 数据流/二次复核：匿名 CONNECT 被保留为无主体会话；`/chatboard` 和 `/forum` 端点白名单允许 `/broadcast`；SEND 进入 `canSend` 后因目的地不含 `/transfer` 直接放行。该目的地又由 simple broker 前缀直接消费，无需经过 `@MessageMapping`/业务 Service，因此可向正常订阅者发布攻击者构造的消息。
- 影响：伪造聊天室消息、帖子/回复实时事件、页面广播或通知，诱导客户端错误展示或执行状态变更；可用于冒充系统消息、垃圾消息和实时通道资源滥用。消息不一定落库，但客户端可观察影响明确。
- 现有补偿控制：会话 `/transfer` 路径有成员校验；它不能保护其他三个 broker 前缀。
- 修复建议：客户端 SEND 只允许进入经过身份和业务校验的 `/app/**` 目的地；显式拒绝客户端向所有 broker 输出前缀 SEND。广播由服务端 `SimpMessagingTemplate` 独占。按端点/命令要求认证、复用禁言规则、配置 Origin 白名单，并增加匿名 CONNECT/SEND 和跨 Origin 回归测试。
- 置信度：确定。

### STATIC-04：管理端 STOMP 接受普通用户端 JWT

- 严重级别：高危
- CWE/类别：CWE-287（身份验证不当）、CWE-863（授权不正确）、令牌域混用
- 证据位置：
  - `web/web-app/src/main/resources/application.yml:15` 与 `web/web-admin/src/main/resources/application.yml:14`：静态比较确认两端配置了相同的字面量 JWT 签名密钥（本报告不记录值）。
  - `common/src/main/java/com/ayor/util/JWTUtils.java:154-173,238-246`：验证仅要求共享 HMAC 签名和有效期/撤销状态，没有 issuer、audience 或 token 类型约束。
  - `web/web-admin/src/main/java/com/ayor/interceptor/StompAuthInterceptor.java:30-38`：CONNECT 接受任意能被共享 `JWTUtils` 验证的 JWT，没有管理员角色检查。
  - `web/web-admin/src/main/java/com/ayor/interceptor/StompAuthInterceptor.java:40-47`：任意已认证主体均可订阅 `/topic/reports`。
  - `web/web-admin/src/main/java/com/ayor/service/impl/ReportServiceImpl.java:45-60,192-204`：新举报会推送举报人/被举报人 ID、目标 ID、举报类型、用户名和内容摘要快照。
- 攻击前提：攻击者拥有一个普通用户端账号及其有效 JWT，并能访问管理端 WebSocket 端口。
- 数据流/二次复核：普通用户端签发的 JWT 使用同一密钥，管理端公共 `JWTUtils` 不区分发行方/受众；管理端 CONNECT 只检查签名有效性并把 token 内主体设为 Principal；SUBSCRIBE 只检查 Principal 非空。不存在数据库管理员角色的二次校验。
- 影响：普通论坛用户可持续监听管理端实时举报流，获取举报双方标识、被举报目标和内容摘要等敏感审核数据。
- 现有补偿控制：目的地被固定为 `/topic/reports`，但没有限制订阅者的管理员身份；任意 Origin 也被允许（`WebsocketConfiguration.java:20-22`）。
- 修复建议：管理端使用独立签名密钥/issuer/audience，拒绝用户端 token；CONNECT 和 SUBSCRIBE 均验证数据库当前管理员角色或细粒度权限，不信任 token 中旧权限声明；限制 Origin，并增加“普通用户 token 必须失败”的集成测试。
- 置信度：确定。

### STATIC-05：运行时配置包含可复用秘密

- 严重级别：高危
- CWE/类别：CWE-798（硬编码凭据）、CWE-321（硬编码密码学密钥）、秘密管理
- 证据位置（仅记录位置与类型）：
  - `web/web-app/src/main/resources/application.yml:6-7,15,27-28,33-34,41-42`：数据库、JWT、MinIO、RabbitMQ、SMTP 的用户名/密码/密钥均为字面量。
  - `web/web-admin/src/main/resources/application.yml:6-7,14,18-19,24-25`：数据库、JWT、MinIO、RabbitMQ 凭据均为字面量。
  - `.docker/environment/mysql.env:1`：MySQL root 密码为字面量。
  - `.docker/docker-compose.yaml:41-42`：MinIO root 账号/密码为字面量。
- 攻击前提：攻击者获得仓库、构建产物、配置备份或日志中的配置文件；相关环境仍使用这些值。
- 数据流/复核：这些文件是默认 `application.yml`，`JWTUtils`、数据源、MinIO、RabbitMQ 和邮件组件直接读取对应属性；未发现单独的生产 profile 对其作强制替换。两端 JWT 字面量经静态比较相同。
- 影响：若任一已部署环境复用这些值，仓库泄露可导致 JWT 伪造、数据库/对象存储/消息队列访问或 SMTP 账号滥用；共享 JWT 密钥还扩大单点泄露范围。
- 现有补偿控制：环境变量可在 Spring 外部配置优先级中覆盖文件，但当前配置没有“缺失即拒绝启动”的保证，无法证明部署时一定覆盖。
- 修复建议：立即盘点并轮换所有出现过的值；从 Git 当前树和必要历史中清理秘密；仅提交无权默认值或 `${ENV_VAR}` 占位，并让非开发环境缺失秘密时启动失败。用户端/管理端使用独立 JWT 密钥及 issuer/audience。秘密轮换后验证旧 JWT、旧 SMTP/MinIO/DB/RabbitMQ 凭据全部失效。
- 置信度：高（秘密存在与调用链确定；是否仍在生产复用需部署侧确认）。

### STATIC-06：Docker 基础设施对宿主网络暴露弱保护服务

- 严重级别：高危
- CWE/类别：CWE-306（缺少认证）、CWE-284（访问控制不当）、不安全默认配置
- 证据位置：
  - `.docker/docker-compose.yaml:8-14`：MySQL 使用浮动 `latest` 镜像并发布宿主端口。
  - `.docker/docker-compose.yaml:22-29`：Redis 发布宿主端口，未配置密码/ACL 或仅监听回环地址。
  - `.docker/docker-compose.yaml:31-43`：MinIO API/控制台发布宿主端口，使用仓库中的静态 root 凭据。
  - `.docker/docker-compose.yaml:53-87`：Elasticsearch 明确 `xpack.security.enabled=false`，同时发布 HTTP 和 transport 端口。
  - `.docker/docker-compose.yaml:93-101`：Kibana 发布宿主端口并连接无认证 Elasticsearch。
- 攻击前提：攻击者能从本机、局域网或被错误放通的网络访问 Docker 宿主机；实际可达性受宿主防火墙影响。
- 数据流/复核：Compose 的短端口映射未指定 `127.0.0.1`，默认绑定宿主所有接口；Redis/Elasticsearch 没有应用层认证，MinIO/MySQL 凭据又在仓库中。论坛数据、缓存/会话、对象和搜索索引均处于这些服务边界。
- 影响：可直接读取/篡改数据库与搜索数据，读取/删除媒体对象，操纵 Redis 会话/黑名单/Passkey challenge，最终造成账号接管、数据泄露或服务破坏。
- 现有补偿控制：Docker 自定义网络只限制容器互联，不能抵消已发布的宿主端口；宿主防火墙状态不在仓库证据范围内。
- 修复建议：开发环境端口至少绑定 `127.0.0.1`；不需要宿主访问的服务不发布端口。生产使用私网、网络策略、TLS、Redis ACL、Elasticsearch Security、强随机且外部注入的凭据；固定镜像摘要/受控版本。增加 Compose 静态策略检查。
- 置信度：高（配置确定；外部可达性依赖宿主网络）。

### STATIC-07：注册邮件滥用与邮箱 JWT 超长 Redis 存活

- 严重级别：中危
- CWE/类别：CWE-400（不受控资源消耗）、CWE-799（交互频率控制不当）
- 证据位置：
  - `web/web-app/src/main/java/com/ayor/config/SecurityConfiguration.java:42-47,125-130`：注册验证邮件入口公开。
  - `web/web-app/src/main/java/com/ayor/controller/AuthorizeController.java:31-33`：每次请求直接创建 token 并发送邮件。
  - `web/web-app/src/main/java/com/ayor/service/impl/AuthorizeServiceImpl.java:31-35`：每次调用创建 JWT、写 Redis 并投递 RabbitMQ 邮件消息。
  - `common/src/main/java/com/ayor/util/JWTUtils.java:132-143`：邮箱 JWT 的 Redis TTL 把绝对 epoch 毫秒 `expire.getTime()` 当作持续时间传入，导致 key 约数十年后才过期；JWT 本身虽在三小时后失效，但 Redis key 不会同步清理。
  - `common/src/main/java/com/ayor/util/CONST.java:8-9`：存在邮件限制常量，但生产调用链未使用。
- 攻击前提：无需登录；攻击者可重复提交格式合法的邮箱地址。
- 数据流/复核：公开请求每次生成随机 JTI，形成新的 Redis key 和邮件队列消息。全仓未找到注册邮件入口的按 IP/邮箱限流、幂等复用或验证码 key 清理；TTL 参数单位与 API 语义不匹配。
- 影响：低成本制造长期 Redis key 堆积、SMTP 发送费用/信誉损害、RabbitMQ 和邮件服务压力；持续滥用可降低注册与会话基础设施可用性。
- 现有补偿控制：DTO 只校验邮箱格式，不能限制频率或目标数量；JWT 三小时过期不能清理被错误设置 TTL 的 Redis key。
- 修复建议：使用“过期时间减当前时间”的剩余 Duration；按 IP、邮箱和全局速率限制，短窗口内复用未过期请求；设置队列/邮件供应商配额与告警。注册成功后原子消费 token/key，并添加 TTL 单元测试。
- 置信度：确定。

### STATIC-08：修改密码不撤销其他登录会话

- 严重级别：中危
- CWE/类别：CWE-613（会话过期不足）、会话撤销不完整
- 证据位置：
  - `web/web-app/src/main/java/com/ayor/controller/UserController.java:251-255`：密码修改入口调用 Service，但请求 DTO 未加 `@Valid`。
  - `web/web-app/src/main/java/com/ayor/service/impl/AccountServiceImpl.java:389-413`：旧密码校验并更新成功后只调用 `invalidateJWT(token)` 撤销当前 token。
  - `common/src/main/java/com/ayor/util/JWTUtils.java:165-170`：其他 JWT 只要其 `sid` 对应 Redis 活跃 key 仍存在就继续有效。
  - `web/web-app/src/main/java/com/ayor/service/impl/UserLoginSessionServiceImpl.java:54-55,96-100`：项目已有逐会话活跃 key 与撤销机制，但密码修改链未调用按账号撤销全部会话。
- 攻击前提：攻击者已获得同一账号的另一个有效会话；账号本人随后修改密码。
- 数据流/复核：密码修改只把当前请求 JTI 加入黑名单，不删除同账号其他 `LOGIN_SESSION_ACTIVE + sid` key，也没有账号级 token version。其他会话后续仍通过 `resolveJwt`。默认 JWT 有效期配置为七天。
- 影响：被盗会话在受害者修改密码后仍可继续访问账号，延长账号接管窗口；缺失 `@Valid` 还使 DTO 上的新密码长度/字符规则没有在该入口执行。
- 现有补偿控制：用户可在会话列表中手工逐个撤销，但这不是密码修改后的自动安全边界。
- 修复建议：密码修改成功后按账号原子撤销所有会话（可选择重新签发当前会话），或采用账号级 token version/password-changed-at 校验；Controller 添加 `@Valid`，并测试旧的多个 JWT 全部失效。
- 置信度：高。

### STATIC-09：公开分页与缓存键缺少上限

- 严重级别：中危
- CWE/类别：CWE-400（不受控资源消耗）
- 证据位置：
  - `web/web-app/src/main/java/com/ayor/config/SecurityConfiguration.java:54-81,128`：帖子、评论、用户帖子、关注/收藏等分页 GET 公开。
  - `web/web-app/src/main/java/com/ayor/controller/ThreadController.java:44-84,102-107`：用户提供的 `page_num/page_size` 原样传入 Service。
  - `web/web-app/src/main/java/com/ayor/service/impl/ThreaddServiceImpl.java:111-127,135-158`：直接 `Page.of(pageNum, pageSize)`；排行榜还把任意页码/页大小写入 Redis 缓存键。
  - `web/web-app/src/main/java/com/ayor/service/impl/PostServiceImpl.java:86-101`、`UserRelationServiceImpl.java:197-218`：评论和关注列表同样没有最大页大小。
  - `web/web-app/src/main/java/com/ayor/config/MybatisPlusConfig.java:17-21`：分页拦截器未设置全局 `maxLimit`。
- 攻击前提：公开列表无需账号；攻击者可反复请求极大、负数边界或大量不同的分页组合。
- 数据流/复核：Controller 无 Bean Validation 上限，Service 无 clamp，MyBatis 分页拦截器无 maxLimit。排行榜每个任意组合生成不同缓存 key，既可放大数据库/序列化内存，也可制造缓存基数增长。
- 影响：大结果集查询、VO 转换和 JSON 序列化消耗 CPU/内存/数据库连接；任意缓存键导致 Redis 内存压力，可能造成应用或缓存服务拒绝服务。
- 现有补偿控制：部分其他 Service 使用默认页大小，但没有统一最大值，不能保护上述公开路径。
- 修复建议：入口统一约束 `page_num >= 1`、`1 <= page_size <= 100`（按业务调整），Service 再做防御性 clamp；MyBatis 配置全局 `maxLimit`；缓存只接受规范化后的参数并配置容量/TTL。增加极大值和负值测试。
- 置信度：高。

### STATIC-10：图片解码缺少像素/帧资源上限

- 严重级别：中危
- CWE/类别：CWE-409（压缩数据处理不当）、CWE-400（资源消耗）
- 证据位置：
  - `model/src/main/java/com/ayor/entity/Base64Upload.java:8-15`：上传对象没有字符串长度或请求体大小约束。
  - `web/web-app/src/main/java/com/ayor/controller/StickerController.java:36-40`：任意登录用户可提交 Base64 图片。
  - `common/src/main/java/com/ayor/image/ImageProcessor.java:42-44,67-73,101-106`：仅在完整 Base64 解码并分配 byte[] 后检查 10 MiB；正文图片随后直接解码。
  - `common/src/main/java/com/ayor/image/ImageProcessor.java:247-268`：`ImageReader.read(0)` 在检查宽高/总像素前创建 `BufferedImage`；未设置最大像素、宽高、帧数或解码内存预算。
- 攻击前提：攻击者拥有普通账号；提交压缩体积小但像素尺寸巨大的合法图片，或并发提交接近上限的 Base64 请求。
- 数据流/复核：压缩字节限制不能约束解码后的 `width × height × channels`；代码直到 `reader.read(0)` 完成后才从 `BufferedImage` 读取尺寸。Sticker 虽之后缩放到 512，但大图已经先完整解码；正文图片不会缩放。
- 影响：单次或少量请求即可造成大内存分配、长时间图片解码、GC 压力甚至 JVM OOM，影响所有用户可用性。
- 现有补偿控制：压缩字节 10 MiB 和扩展名白名单降低普通大文件风险，但不能阻止图片解压炸弹；文件名扩展名也不能作为像素资源控制。
- 修复建议：在 `reader.read` 前用 reader 元数据读取宽高并校验最大宽/高、总像素和帧数；设置 HTTP 请求体/Base64 文本上限，优先流式解码并限制并发图片处理。对解压炸弹样本添加受控单元测试，不在生产环境尝试利用。
- 置信度：高。

### STATIC-11：操作日志未脱敏完整业务入参

- 严重级别：低危
- CWE/类别：CWE-532（日志中插入敏感信息）、日志资源放大
- 证据位置：
  - `web/web-app/src/main/java/com/ayor/aspect/oplog/OperationLog.java:18-26`：默认记录参数。
  - `web/web-app/src/main/java/com/ayor/aspect/oplog/OperationLogAspect.java:55-72`：参数 Map 被完整写入应用日志，失败分支同样记录。
  - `web/web-app/src/main/java/com/ayor/aspect/oplog/OperationLogAspect.java:122-155`：对象被展开为所有字段，没有字段名脱敏、长度限制或类型排除。
  - `web/web-app/src/main/java/com/ayor/aspect/oplog/OperationLogAspect.java:107-116`：同一参数 Map 还会持久化到数据库操作日志。
  - `web/web-app/src/main/java/com/ayor/controller/permission/PermTopicController.java:33-48` 与 `web/web-app/src/main/java/com/ayor/entity/dto/TopicDTO.java:17-28`：创建/更新话题会记录含 Base64 封面的完整 DTO。
- 攻击前提：具有话题管理权限的用户执行创建/更新，或异常触发日志；攻击者随后取得日志/操作日志读取权限。
- 数据流/复核：切面将 `TopicDTO` 展开，`cover` 作为嵌套对象保留并由日志框架/JSON type handler 序列化；没有发现 `base64/password/token` 等通用敏感字段过滤。
- 影响：日志和数据库可能保存大体积 Base64、内容字段或未来 DTO 中新增的秘密/隐私字段，扩大数据副本和日志存储压力。
- 现有补偿控制：当前注解主要用于权限管理入口，未覆盖登录/密码接口；因此当前直接凭据泄露证据不足，定为低危。
- 修复建议：日志切面采用显式允许字段；统一遮蔽 `password/token/secret/base64/content` 等字段并截断长度。二进制/请求/响应对象默认不记录，持久化日志只保存审计所需 ID、动作和结果。
- 置信度：高。

### STATIC-12：认证失败使用 HTTP 200

- 严重级别：低危
- CWE/类别：CWE-390（未检测错误条件）、安全可观测性与协议语义
- 证据位置：
  - `web/web-app/src/main/java/com/ayor/config/SecurityConfiguration.java:183-188,224-249`：登录失败、拒绝访问与未认证处理器只写 JSON 业务码；未认证处理器显式设置 HTTP 200。
  - `web/web-admin/src/main/java/com/ayor/config/SecurityConfiguration.java:98-103,125-144`：管理端采用相同行为。
  - `web/web-admin/src/test/java/com/ayor/config/SecurityConfigurationUnauthorizedTest.java:20-35`：测试明确断言未认证响应调用 `setStatus(200)`，说明该行为已被测试固化。
- 攻击前提：攻击者反复触发登录失败、未认证或无权限访问。
- 数据流/复核：Spring Security 的异常处理器被调用后，Servlet 响应状态保持或被设为 200，仅响应体内 `code` 表示 401/403。按 HTTP 状态工作的反向代理、WAF、CDN、告警规则与指标采集会把这些请求视作成功响应。
- 影响：登录爆破、令牌失效和权限探测更难被基础设施层发现、限速和统计；缓存或客户端中间件也可能误处理失败响应。该问题本身不绕过认证，因此定为低危。
- 修复建议：认证失败返回 HTTP 401、权限不足返回 HTTP 403，并保留一致的 JSON 业务码；更新契约测试，验证 HTTP 状态和响应体同时正确。
- 置信度：确定。

## 已检查且未形成有效漏洞的方面

- MyBatis/XML：生产 Mapper/XML 未发现外部输入使用 `${...}`；动态条件均使用 `#{...}` 或 MyBatis-Plus 类型化条件，未发现可达 SQL 注入证据。
- SSRF/命令/反序列化：未发现用户输入进入 `RestTemplate/WebClient/openConnection`、`ProcessBuilder/Runtime.exec` 或 Java 原生反序列化；个人网站字段只存储 URI，不发起服务端请求。
- 用户端关键所有权：帖子/回复编辑删除、私信会话访问/发送/撤回、会话未读清理、Passkey 删除、背包装备等链路均找到当前用户绑定及 Service 所有权断言，未发现有效 IDOR。
- Passkey：challenge 使用安全随机数、Redis 原子消费、RP ID/Origin 服务端快照和 WebAuthn4J 签名验证；未发现认证绕过。嵌套 DTO 缺少级联 `@Valid` 会降低错误输入质量，但异常被失败路径处理，未单独上报为漏洞。
- JWT：HMAC 签名、过期、黑名单和登录 `sid` 活跃状态均被验证；主要风险是秘密硬编码、身份域混用和密码修改撤销不完整，已分别上报。
- 富文本：后端只解析 TipTap JSON，不执行 HTML；是否存在持久化 XSS 取决于前端渲染器是否严格按 schema 渲染/转义，当前后端仓库不足以确认，因此记录为残余风险而非有效漏洞。

## 限制与后续验证

- 本报告不代表“绝对安全”；未覆盖前端 TipTap 渲染、宿主防火墙、生产配置覆盖、云端秘密轮换状态和第三方服务端权限。
- 未运行网络漏洞扫描、依赖漏洞库、Git 历史秘密扫描、Maven 测试或本地动态请求；这些由主审计流程分别记录。
- 建议动态阶段优先验证（均只对 localhost、非破坏性）：管理端匿名 GET/写入口、匿名 STOMP `/broadcast` SEND、普通用户 JWT 订阅管理端 `/topic/reports`、私有 Sticker 跨账号详情读取。管理端写操作只验证到鉴权拒绝边界，不发送会产生业务写入的有效载荷。
