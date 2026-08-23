# NineForum 后端全面安全审计报告

审计日期：2026-08-20（Asia/Taipei）  
审计基线：`develope` / `ce6f2fa40facf6b7be3a08f520d56d4dfd9e13f5`  
审计范围：`common`、`model`、`web/web-app`、`web/web-admin`、构建配置、Docker 配置、必要 Git 历史及 localhost 运行环境

## 执行摘要

当前版本不建议直接暴露到不受信任网络。审计确认 13 项有效发现：1 项严重、6 项高危、4 项中危、2 项低危。首要风险不是单一依赖漏洞，而是管理端 HTTP 接口全局匿名放行；该问题已通过源码和 localhost 动态请求双重确认。其次是私有图片水平越权、两个 WebSocket/STOMP 信任边界缺陷、仓库秘密泄露、基础设施服务暴露，以及存在可适用公开漏洞且传输安全不足的依赖基线。

现有自动化测试全部通过，但未覆盖上述关键安全契约；管理端现有测试甚至固化了“认证失败仍返回 HTTP 200”的行为。根 Maven 打包还因 Spring Boot 插件配置错误而失败。测试通过不能抵消这些安全结论。

本次审计未修改产品代码、未扫描公网目标、未执行爆破或破坏性利用，也未发送会产生业务数据的 HTTP 请求。管理端启动时存在一个无条件统计刷新任务；审计发现该副作用后停止应用并核对数据，未发现净业务数据变化。详细过程见 `research/audit-matrix.md`。

## 风险总览

| 编号 | 级别 | 发现 | 状态 |
| --- | --- | --- | --- |
| SEC-01 | 严重 | 管理端 HTTP 全局 `permitAll`，匿名请求可到达系统级管理操作 | 源码与动态确认 |
| SEC-02 | 高危 | 私有 Sticker 可按资源 ID 跨账号读取和收藏 | 源码确认 |
| SEC-03 | 高危 | 匿名 STOMP 客户端可直接向用户端 broker 广播前缀发送伪造事件 | 源码确认 |
| SEC-04 | 高危 | 管理端 STOMP 接受普通用户 JWT，可订阅实时举报数据 | 源码确认 |
| SEC-05 | 高危 | 仓库及 Git 历史包含多类可复用秘密，两端共用 JWT 密钥 | 当前树与历史确认 |
| SEC-06 | 高危 | Redis/Elasticsearch 等基础设施绑定宿主所有接口且缺少认证或使用静态凭据 | 配置与运行态确认 |
| SEC-11 | 高危 | 依赖基线存在可适用公开漏洞，关键组件补丁明显滞后 | OSV、官方公告与依赖树确认 |
| SEC-07 | 中危 | 注册邮件入口无滥用控制，邮箱 JWT Redis TTL 约为数十年 | 源码确认 |
| SEC-08 | 中危 | 修改密码只撤销当前 JWT，其他会话继续有效 | 源码确认 |
| SEC-09 | 中危 | 公开分页无最大页大小，任意参数扩大查询与缓存键基数 | 源码确认 |
| SEC-10 | 中危 | 图片解码前无宽高、像素与帧数上限，可触发解压炸弹型 DoS | 源码确认 |
| SEC-12 | 低危 | 操作日志无字段脱敏与长度限制 | 源码确认 |
| SEC-13 | 低危 | 认证/授权失败仍返回 HTTP 200 | 源码与测试确认 |

字段约定：下述每项均给出类别/置信度、证据、攻击前提或适用条件、数据流/复现说明、影响及修复验证建议。除 SEC-05 明确要求轮换并清理历史、SEC-06 建议轮换可能经明文传输的凭据外，其余发现不涉及秘密轮换或 Git 历史清理。

## 最高优先级发现

### SEC-01：管理端 HTTP 全局匿名放行

- 类别：CWE-306、CWE-862；置信度：确定。
- 证据：`web/web-admin/src/main/java/com/ayor/config/SecurityConfiguration.java:49-54` 对唯一安全链执行 `anyRequest().permitAll()`；角色、权限、账号、私信和数据修复等控制器没有补偿性管理员授权。
- 动态结果：匿名 GET `/api/accounts` 与 `/api/roles` 没有得到 401/403，而是进入 Controller/Service，随后因审计刻意指向不存在的数据库端口而返回 500。匿名 `/v3/api-docs` 返回 200，公开了完整接口清单。
- 攻击前提：能访问管理端端口，不需要账号或令牌。
- 影响：读取敏感数据、分配角色与权限、删除账号/内容、修改私信、触发数据修复或搜索重建，可能导致系统完全失陷或大范围数据破坏。
- 修复：安全链默认 `authenticated()` 或拒绝；仅精确放行登录和必要健康检查。高影响操作使用明确的管理员权限表达式，并在 Service 边界保留授权断言。增加真实 FilterChain/MockMvc 契约：匿名 401、普通用户 403、具备权限的管理员成功。

### SEC-02：私有 Sticker 水平越权

- 类别：CWE-639、CWE-862；置信度：确定。
- 证据：`web/web-app/src/main/java/com/ayor/service/impl/ImageAssetServiceImpl.java:61-68` 把用户上传资源设为 `PRIVATE`；`87-106` 的收藏链与 `145-155` 的详情链只校验资源存在、类型和状态，不校验 `visibility` 或所有者；入口分别位于 `web/web-app/src/main/java/com/ayor/controller/StickerController.java:61-64,91-94`。
- 攻击前提：任意普通账号，能够枚举自增 `assetId`。
- 影响：读取他人的私有图片对象 URL、所有者与尺寸信息，并收藏到自己的资源库。
- 修复：建立统一资产访问策略，所有读取、收藏、按 URL 导入与内容引用路径都复用“所有者可访问 PRIVATE，非所有者仅访问 PUBLIC+ACTIVE”的授权函数；增加双账号 IDOR 回归测试。

### SEC-03：匿名 STOMP 消息注入

- 类别：CWE-306、CWE-862；置信度：确定。
- 证据：用户端 `web/web-app/src/main/java/com/ayor/interceptor/StompAuthInterceptor.java:67-81` 允许无 JWT CONNECT；`150-170` 对所有不含 `/transfer` 的 SEND 默认放行。`web/web-app/src/main/java/com/ayor/config/WebsocketConfiguration.java:27-30,40-47` 又把 `/broadcast`、`/notif`、`/verify` 交给 simple broker 并允许任意 Origin。
- 攻击前提：能连接用户端 WebSocket，不需要 JWT。
- 影响：向正常订阅者伪造聊天室、帖子/回复、通知或验证事件，实施冒充系统消息、垃圾广播和实时通道滥用。
- 供应链叠加：Spring WebSocket 6.2.10 受 CVE-2026-41838 影响，可预测会话标识在授权不足时具有安全影响；本缺陷使该利用前提具有现实意义。[Spring WebSocket 公告](https://spring.io/security/cve-2026-41838/)
- 修复：客户端 SEND 仅允许进入经过身份与业务校验的 `/app/**`；显式拒绝客户端向 broker 输出前缀 SEND；广播只允许服务端 `SimpMessagingTemplate` 产生，并限制 Origin；同时升级到 Spring Framework 6.2.19 或更高修复版本。

### SEC-04：管理端 STOMP 身份域混用

- 类别：CWE-287、CWE-863；置信度：确定。
- 证据：`web/web-app/src/main/resources/application.yml:15` 与 `web/web-admin/src/main/resources/application.yml:14` 使用同一 JWT 签名密钥；`common/src/main/java/com/ayor/util/JWTUtils.java:154-173,238-246` 不校验 issuer、audience 或 token 类型；管理端 `web/web-admin/src/main/java/com/ayor/interceptor/StompAuthInterceptor.java:30-47` 只验证签名并允许任何已认证主体订阅 `/topic/reports`。
- 攻击前提：持有任意普通用户端有效 JWT，能访问管理端 WebSocket。
- 影响：普通论坛用户可监听包含举报双方标识、目标和内容摘要的实时审核流。
- 供应链叠加：CVE-2026-41838 的可预测 WebSocket 会话标识会放大当前授权不足；因此不能仅依赖升级消除本项业务授权缺陷。
- 修复：分离两端签名密钥与 issuer/audience；管理端 CONNECT 和 SUBSCRIBE 查询并校验当前管理员权限；升级 Spring Framework；增加“普通用户 token 必须失败”的集成测试。

### SEC-05：仓库秘密与共享签名密钥

- 类别：CWE-798、CWE-321；置信度：高。
- 证据：`web/web-app/src/main/resources/application.yml:6-7,15,27-28,33-34,41-42` 与 `web/web-admin/src/main/resources/application.yml:6-7,14,18-19,24-25` 含多类字面量凭据；`.docker/environment/mysql.env:1` 含数据库 root 密码；当前 SMTP 凭据指纹可在 37 个历史提交中找到。本报告不复制任何秘密值。
- 攻击前提：取得仓库、构建产物、配置备份或日志；部署环境仍复用相关值。
- 影响：JWT 伪造、数据库/对象存储/消息队列访问、SMTP 滥用；共享密钥会把用户端泄露扩大为管理端影响。
- 修复：立即盘点并轮换所有已出现值，再清理当前树与必要历史；使用环境变量/秘密管理器并让非开发环境在缺失秘密时拒绝启动；两端密钥独立。先轮换再重写 Git 历史。

### SEC-06：基础设施弱保护且绑定所有宿主接口

- 类别：CWE-306、CWE-284；置信度：高。
- 证据：`.docker/docker-compose.yaml:8-14,22-43,53-101` 发布 MySQL、Redis、MinIO、Elasticsearch/Kibana 端口；Redis 未配置认证，Elasticsearch 明确关闭 Security。运行态 `docker port` 确认 MySQL、Redis、MinIO、Elasticsearch 映射到 `0.0.0.0` 和 `[::]`；无密码 Redis PING 返回 PONG，Elasticsearch 根接口匿名返回 200。
- 传输边界：应用按 5672 端口连接 RabbitMQ，运行日志显示 AMQP 明文连接；SMTP 配置未启用 TLS。Angus Mail 的 SMTP STARTTLS 与 SSL 默认均为关闭，因此邮件凭据和邮件内容可能以明文传输。[Angus Mail SMTP 属性文档](https://eclipse-ee4j.github.io/angus-mail/docs/api/org.eclipse.angus.mail/org/eclipse/angus/mail/smtp/package-summary.html)
- 攻击前提：攻击者可达宿主端口；实际局域网/公网可达性取决于宿主防火墙与上游网络。
- 影响：读取或篡改数据库、缓存/会话、对象和搜索索引，可能导致账号接管、数据泄露或服务破坏。
- 修复：开发环境至少绑定 `127.0.0.1`，不需要宿主访问的服务不发布端口；生产使用私网、TLS、Redis ACL、Elasticsearch Security 和外部注入的强随机凭据；为 SMTP 与 RabbitMQ 开启 TLS、证书链及主机名校验并轮换可能经明文传输的凭据；镜像固定版本或摘要，禁止 `latest`。

### SEC-11：依赖基线存在可适用公开漏洞

- 类别：CWE-1104；置信度：高。
- 扫描证据：OSV 官方 API 已完整批量查询四个生产模块中 146 个去重后的 `compile` / `runtime` Maven 坐标，返回 80 条原始公告记录：5 条 Critical、28 条 High、35 条 Moderate、12 条 Low。原始级别不等于项目级严重度；本报告已逐项结合源码与配置复核。明细见 `research/osv-scan-summary.md`。
- Spring Security：当前 6.5.3 受 CVE-2026-22732 影响，项目使用相关响应头写入器，组件与功能条件成立；修复版本为 6.5.9。尚未确认具体敏感缓存路径，因此未直接照搬厂商 Critical 评级，但这是可适用且需要优先修复的高危供应链风险。[Spring Security 公告](https://spring.io/security/cve-2026-22732/)
- WebSocket：Spring Framework 6.2.10 受 CVE-2026-41838 影响；其“授权不足”利用条件与 SEC-03、SEC-04 已确认缺陷重叠，修复版本为 6.2.19。[Spring WebSocket 公告](https://spring.io/security/cve-2026-41838/)
- 对象存储：直接依赖 MinIO Java 8.5.17 受 CVE-2025-59952 影响，恶意 XML 响应可替换标签并暴露环境或系统值，修复版本为 8.6.0。攻击者控制服务端响应的前提尚未确认，但当前 MinIO 使用 HTTP、静态凭据且端口发布到宿主，因此该条件风险不能忽略。[MinIO Java 公告](https://github.com/minio/minio-java/security/advisories/GHSA-h7rh-xfpj-hpcm)
- 邮件：Spring Boot 3.5.5 受 CVE-2026-40992 影响，3.5.x 修复版本为 3.5.15；当前 SMTP 更直接的问题是未启用 TLS，开启 TLS 后仍需显式启用严格主机名校验。[Spring Boot 公告](https://spring.io/security/cve-2026-40992/)
- 排除结果：Tomcat Digest/HTTP2/Servlet constraint/Rewrite/WebDAV、Bouncy Castle GOST、Netty 解压、Jackson async/default typing、Spring Data 用户属性路径和用户可控 SpEL 等高分线索，均未发现当前配置或调用链可达证据；不能把这些原始命中计为已确认项目漏洞。
- 攻击前提/数据流复核：Spring Security 响应头功能与 Spring WebSocket 均在实际请求链上；MinIO 公告还要求攻击者可影响服务端 XML 响应，当前未确认该控制能力；邮件公告在启用 TLS 后才涉及主机名校验，而当前配置更早地缺少传输加密。项目级高危评级来自多个可适用缺陷与已确认信任边界的叠加，不是把 80 条原始命中全部视为可利用。
- 影响：在相应前提满足时，可能造成敏感响应缓存保护缺失、WebSocket 会话标识被利用、环境/系统值泄露或邮件传输遭中间人攻击；继续停留在已结束 OSS 支持的补丁线还会扩大后续修复窗口。
- 修复与验证：优先升级到 Spring Boot 3.5.16，由 BOM 统一带入 Spring Framework 6.2.19、Spring Security 6.5.11、Spring AMQP 3.2.12、Spring Data 2025.0.13、Spring Retry 2.0.13 等版本；MinIO Java 单独升级到至少 8.6.0。3.5.16 是 3.5.x 最后一个开源支持版本，完成补丁升级后应规划迁移到受支持主版本，并重新执行测试、打包、依赖扫描与关键安全回归。[Spring Boot 3.5.16 发布说明](https://spring.io/blog/2026/06/25/spring-boot-3-5-16-available-now/)、[3.5.16 BOM](https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/3.5.16/spring-boot-dependencies-3.5.16.pom)

## 中低危发现

### SEC-07：注册邮件滥用与错误 TTL

- 类别：CWE-400、CWE-799；置信度：确定；级别：中危。
- 证据：`web/web-app/src/main/java/com/ayor/controller/AuthorizeController.java:31-33` 与 `web/web-app/src/main/java/com/ayor/service/impl/AuthorizeServiceImpl.java:31-35` 对每次公开请求创建 JWT、Redis key 和邮件消息；`common/src/main/java/com/ayor/util/JWTUtils.java:132-143` 把绝对 epoch 毫秒 `expire.getTime()` 当作 Redis 持续时间。
- 攻击前提与数据流：无需登录，只需重复提交格式合法的邮箱；每次请求产生新的随机 JTI、长期 Redis key 与 RabbitMQ 邮件消息，且未发现按 IP/邮箱限流、幂等复用或成功后的 key 清理。
- 影响：攻击者可制造长期 key 堆积、队列压力与 SMTP 费用/信誉损害，持续滥用可降低注册及会话基础设施可用性。
- 修复与验证：使用剩余 `Duration`；增加 IP、邮箱和全局限流、短窗口幂等、配额告警及注册成功后的原子消费；用单元测试断言 Redis TTL 接近 JWT 剩余寿命，并测试限流边界。

### SEC-08：密码修改未撤销全部会话

- 类别：CWE-613；置信度：高；级别：中危。
- 证据：`web/web-app/src/main/java/com/ayor/service/impl/AccountServiceImpl.java:389-413` 更新密码后只撤销当前 JWT；`common/src/main/java/com/ayor/util/JWTUtils.java:165-170` 仍接受其他具有活跃 `sid` 的 JWT；`web/web-app/src/main/java/com/ayor/controller/UserController.java:251-255` 还缺少 `@Valid`。
- 攻击前提与数据流：攻击者已持有同账号的另一有效会话，受害者随后修改密码；改密链既不删除其他 `LOGIN_SESSION_ACTIVE + sid`，也没有账号级 token version，因此其他会话继续通过 `resolveJwt`。
- 影响：被盗会话可在受害者改密后继续使用，默认窗口可达七天；缺少 `@Valid` 还使新密码约束未在该入口执行。
- 修复与验证：撤销账号全部会话或引入账号级 token version/password-changed-at，Controller 添加 `@Valid`；用至少两个旧 JWT 验证改密后全部失败，并验证新密码约束。

### SEC-09：公开分页和缓存基数无上限

- 类别：CWE-400；置信度：高；级别：中危。
- 证据：`web/web-app/src/main/java/com/ayor/controller/ThreadController.java:44-84,102-107` 原样传入分页参数；`web/web-app/src/main/java/com/ayor/service/impl/ThreaddServiceImpl.java:111-127,135-158` 直接调用 `Page.of` 并用任意参数构造缓存键；`web/web-app/src/main/java/com/ayor/config/MybatisPlusConfig.java:17-21` 未设置 `maxLimit`。
- 攻击前提与数据流：公开列表无需账号；极大、负数边界或大量不同组合从 Controller 进入 Service/数据库，并在排行榜路径形成高基数 Redis key。
- 影响：可放大数据库查询、VO 转换、JSON 序列化内存和 Redis 缓存基数，造成资源耗尽或拒绝服务。
- 修复与验证：在入口、Service 和分页插件三层规范化，例如 `page_num >= 1`、`1 <= page_size <= 100`，缓存只使用规范化参数并设置容量/TTL；测试极大值、负值和等价参数不会绕过上限或生成额外键。

### SEC-10：图片解压炸弹 (已解决)

- 类别：CWE-409、CWE-400；置信度：高；级别：中危。
- 证据：`model/src/main/java/com/ayor/entity/Base64Upload.java:8-15` 无文本/请求体大小约束；`common/src/main/java/com/ayor/image/ImageProcessor.java:42-44,67-73,101-106,247-268` 在完整 Base64 解码后才检查 10 MiB，并在检查宽高、总像素或帧数前调用 `ImageReader.read(0)`。
- 攻击前提与数据流：普通账号提交压缩体积小但像素尺寸巨大的合法图片；压缩字节上限无法约束 `width × height × channels`，Sticker 缩放前和正文图片处理都会先完整解码。
- 影响：单次或少量请求即可造成长时间解码、大内存分配、GC 压力或 JVM OOM，影响所有用户。
- 修复与验证：先读元数据并限制宽、高、总像素、帧数、请求体大小和并发处理数，再执行完整解码；用受控的大尺寸小体积样本验证在分配大图前被拒绝，并监测内存上限。

### SEC-12：操作日志无脱敏和长度限制

- 类别：CWE-532；置信度：高；级别：低危。
- 证据：`web/web-app/src/main/java/com/ayor/aspect/oplog/OperationLogAspect.java:55-72,107-155` 展开并记录完整业务参数，既写应用日志也持久化数据库；`web/web-app/src/main/java/com/ayor/controller/permission/PermTopicController.java:33-48` 与 `web/web-app/src/main/java/com/ayor/entity/dto/TopicDTO.java:17-28` 使 Base64 封面进入该链路。
- 攻击前提与数据流：具有话题管理权限的用户执行创建/更新，或异常触发日志；参数对象被展开后写入应用日志和数据库，攻击者还需取得日志读取权限。当前未发现登录密码入口使用该注解，因此定为低危。
- 影响：日志可能保存大体积 Base64、内容字段或未来新增的秘密/隐私字段，扩大敏感数据副本并放大存储压力。
- 修复与验证：使用字段允许清单，默认屏蔽 password/token/secret/base64/content 等字段并统一截断；测试应用日志与持久化操作日志均不出现原始敏感字段或超长内容。

### SEC-13：认证失败仍返回 HTTP 200

- 类别：CWE-390；置信度：确定；级别：低危。
- 证据：`web/web-app/src/main/java/com/ayor/config/SecurityConfiguration.java:183-188,224-249` 与 `web/web-admin/src/main/java/com/ayor/config/SecurityConfiguration.java:98-103,125-144` 没有设置正确 HTTP 状态，未认证处理器还显式 `setStatus(200)`；`web/web-admin/src/test/java/com/ayor/config/SecurityConfigurationUnauthorizedTest.java:20-35` 固化了该行为。
- 攻击前提与数据流：攻击者触发登录失败、无效令牌或无权限访问；Spring Security 异常处理器只在 JSON 业务码中表达 401/403，反向代理、WAF、缓存和监控仍观察到 HTTP 200。
- 影响：基础设施层更难识别、限速和统计爆破、令牌失效与权限探测，客户端中间件也可能误处理失败；该问题本身不绕过认证。
- 修复与验证：认证失败返回 HTTP 401、权限不足返回 HTTP 403，同时保留一致 JSON 业务码；更新两端契约测试，分别断言 HTTP 状态与响应体。

## 自动化与动态验证结果

| 项目 | 结果 |
| --- | --- |
| 数据库备份 | 可移植逻辑备份已校验；44 个建表、41 个数据插入段；SHA-256 已记录在 `research/backup-manifest.md`；目录被 Git 忽略 |
| Maven 测试 | 6 模块成功；514 个测试，0 失败、0 错误、0 跳过 |
| Maven 打包 | 失败；根 POM 的 Spring Boot Maven 插件 `exclude` 错误包含 `version` 字段，插件配置无法解析 |
| 秘密检查 | 当前树与必要 Git 历史人工/指纹检查完成；本机无 gitleaks/trufflehog，未把人工检查宣称为专用扫描器等价替代 |
| 依赖与漏洞 | 四个生产模块依赖树已保存；OSV 官方 API 完整查询 146 个坐标并返回 80 条原始公告，已完成可达性复核；Dependency-Check 因无 NVD API Key 在首次同步约 11% 时主动停止，不能视为完成 |
| 管理端 HTTP | 匿名 API 文档 200；两个敏感 GET 进入业务层后 500，确认未经过认证拒绝；无业务写请求 |
| Redis/Elasticsearch | 无认证 localhost 只读探测确认；没有发送修改数据的命令 |
| WebSocket | 未发送可能影响订阅者的伪造广播；以拦截器、broker 配置和现有测试做数据流验证 |

## 已检查且未形成有效漏洞

- 生产 Mapper/XML 未发现外部输入进入 `${...}`；动态条件使用 `#{...}` 或 MyBatis-Plus 类型化条件，未确认 SQL 注入。
- 未发现用户输入进入 `Runtime.exec`、`ProcessBuilder`、Java 原生反序列化或服务端任意 URL 请求；未确认命令执行、原生反序列化或 SSRF。
- 帖子/回复编辑删除、私信会话访问/发送/撤回、会话未读、Passkey 删除、背包装备等关键用户端链路存在当前用户绑定与 Service 所有权断言。
- Passkey challenge 使用安全随机数、Redis 原子消费、RP ID/Origin 快照及 WebAuthn4J 验证，未确认认证绕过。
- 后端只解析 TipTap JSON，不执行 HTML；前端渲染器是否安全不在本仓库范围，因此持久化 XSS 保留为残余风险，不作为已确认漏洞。
- 未发现已跟踪 PEM、P12、PFX、JKS 或 SSH 私钥文件。

## 限制与残余风险

- 未接触生产配置、宿主防火墙、云端秘密状态、反向代理规则和前端渲染代码；无法判断本地暴露是否等同公网暴露，也无法证明仓库秘密是否仍被生产复用。
- 不创建测试账号、不读取或输出隐私数据，因此需要双账号的 Sticker IDOR 与普通用户 JWT 订阅管理端 STOMP 未做真实业务凭据动态复现；其调用链已二次复核。
- 管理端启动有无条件数据库刷新副作用，限制了完整 DAST。最终 HTTP 验证通过断开数据库连接保证请求无法读写业务库。
- 专用 SAST、容器镜像和秘密扫描器未安装。OSV 批量扫描已完整覆盖已解析的 Maven `compile` / `runtime` 坐标，但不提供调用级可达性证明，也不覆盖未解析、构建插件、容器镜像或运行环境组件。OWASP Dependency-Check 12.2.2 因无 NVD API Key 在首次同步约 11% 时主动停止，退出码为 1，不能表述为已完成。
- 安全审计只能说明本次范围与时间点未发现更多有效问题，不代表系统绝对安全。

## 整改顺序

1. 立即隔离管理端与基础设施端口；修复管理端默认授权策略，轮换所有仓库秘密和共享 JWT 密钥。
2. 修复 Sticker 所有权策略与两个 STOMP 信任边界，增加匿名、普通用户和管理员三类安全契约测试。
3. 升级 Spring Boot 到 3.5.16、MinIO Java 到至少 8.6.0，并修复打包 POM；随后规划 Spring Boot 下一受支持主版本迁移，重新运行测试、打包和依赖扫描。
4. 修复邮件限流/TTL、全会话撤销、分页上限和图片像素预算。
5. 完成日志脱敏、正确 HTTP 401/403、API 文档生产关闭和 Compose 安全基线。

建议把整改拆成独立任务，并在每批修复后做一次针对性回归审计，避免同时改动鉴权、会话和业务授权造成新的边界混淆。
