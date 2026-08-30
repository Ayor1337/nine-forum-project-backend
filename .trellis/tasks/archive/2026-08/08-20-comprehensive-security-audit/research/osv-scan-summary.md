# OSV 依赖漏洞扫描与可达性复核

扫描日期：2026-08-20（Asia/Taipei）  
数据源：[OSV 官方 API](https://osv.dev/docs/#tag/vulnerability/operation/OSV_QueryBatch)  
输入：四个生产模块 Maven 依赖树中的 `compile` / `runtime` 坐标

## 扫描结果

- 去重后共查询 146 个 Maven 坐标。
- OSV 返回 80 条原始公告记录：5 条 Critical、28 条 High、35 条 Moderate、12 条 Low。
- 上述级别是公告/扫描器原始分级，不等于 NineForum 的项目级严重度。最终评级还考虑调用链、运行配置、攻击前提和补偿控制。
- OWASP Dependency-Check 12.2.2 也曾启动，但因未配置 NVD API Key，首次 NVD 全量同步在约 40,000 / 380,852（约 11%）时主动停止；该次运行退出码为 1，不能视为完成，也不能据此得出“无已知漏洞”。

## 原始公告按组件分布

| 组件 | 原始公告数 |
| --- | ---: |
| Tomcat Embed Core | 16 |
| Spring Web MVC | 11 |
| RabbitMQ Java Client | 6 |
| Jackson Databind | 5 |
| Logback | 4 |
| Spring Data Commons | 4 |
| Spring Expression | 3 |
| Spring Security Core | 3 |
| Spring Security Web | 3 |
| Netty Handler | 3 |
| Netty Codec | 3 |
| Bouncy Castle Provider | 2 |
| Spring WebSocket | 2 |
| Spring AMQP | 2 |
| Jackson Core | 2 |
| Spring Core | 2 |
| 其他 9 个组件 | 9 |

“其他”分别为 Log4j API、OpenTelemetry API、Spring Web、MinIO Java、Spring Boot、Spring Boot Starter Mail、Spring Data KeyValue、Spring Retry、Commons Lang3，各 1 条。

## 形成项目风险的公告

### Spring Security CVE-2026-22732

- 当前 Spring Security 6.5.3 落入受影响范围，修复版本为 6.5.9；项目使用 Spring Security 响应头写入器，因此组件和功能条件成立。
- 动态响应已观察到 `X-Content-Type-Options: nosniff`，但仓库中未确认自定义敏感缓存响应路径，尚不能证明公告中的敏感缓存场景可被完整利用。
- 厂商公告定为 Critical；结合当前项目可达性证据，本审计将其纳入 SEC-11，并把项目级供应链风险评为高危，而不是直接照搬厂商级别。
- 来源：[Spring Security 公告](https://spring.io/security/cve-2026-22732/)

### Spring WebSocket CVE-2026-41838

- 当前 Spring Framework 6.2.10 落入受影响范围，修复版本为 6.2.19。
- 公告描述的可预测 WebSocket 会话标识需要授权不足作为利用条件；本项目已确认用户端和管理端 STOMP 授权边界缺陷，因此该公告会放大 SEC-03、SEC-04 的风险。
- 来源：[Spring WebSocket 公告](https://spring.io/security/cve-2026-41838/)

### MinIO Java CVE-2025-59952

- 项目直接依赖 MinIO Java 8.5.17，低于修复版本 8.6.0。
- 风险要求客户端处理攻击者可影响的恶意 XML 响应；仓库未确认攻击者能够控制 MinIO 服务端响应，因此属于有条件风险。
- 由于本地 MinIO 使用静态凭据、HTTP 连接且端口发布到宿主接口，该前提不能完全忽略；纳入 SEC-11 的升级范围。
- 来源：[MinIO Java 安全公告](https://github.com/minio/minio-java/security/advisories/GHSA-h7rh-xfpj-hpcm)

### Spring Boot Mail CVE-2026-40992 与明文 SMTP

- Spring Boot 3.5.5 落入 CVE-2026-40992 的受影响范围；3.5.x 修复版本为 3.5.15。
- 当前配置没有启用 SMTP TLS，也没有配置主机名校验。Angus Mail 的 SMTP `STARTTLS` 与 SSL 默认均为关闭，因此当前更直接的风险是凭据和邮件内容可能经明文传输；即使后续启用 TLS，也必须同时启用严格主机名校验。
- 来源：[Spring Boot 公告](https://spring.io/security/cve-2026-40992/)、[Angus Mail SMTP 属性文档](https://eclipse-ee4j.github.io/angus-mail/docs/api/org.eclipse.angus.mail/org/eclipse/angus/mail/smtp/package-summary.html)

### RabbitMQ 明文传输

- 应用使用 5672 端口，运行日志中的连接协议为 AMQP 明文；仓库未配置 TLS。
- RabbitMQ 客户端的部分公告聚焦 TLS 信任或主机名校验，但当前项目在这些问题之前已缺少传输加密。本项与公开端口、静态凭据一起归入 SEC-06。

## 经复核未确认当前可达的高分线索

- Tomcat：未使用 Digest Auth、RewriteValve、WebDAV，未启用 HTTP/2，也未发现依赖 Servlet 扩展安全约束的路径；相关 Digest、HTTP/2、constraint、Rewrite/WebDAV 公告未形成当前可达证据。
- Bouncy Castle：未发现 GOST CTR 使用，CVE-2025-14813 的算法条件不成立。
- Netty：仅由 Lettuce/Redis 等传递引入，未发现应用使用 Bzip2 或其他相关解压 decoder。
- Jackson：未发现异步 parser、default typing、`PolymorphicTypeValidator` 或 `@JsonTypeInfo` 风险路径。
- Spring Data：排序字段来自固定枚举或字面量，未发现用户可控属性路径进入相关缓存。
- Spring Expression：SpEL 表达式来自编译期注解字面量，不是用户可控表达式。
- Spring Boot CVE-2026-40973：需要 `server.servlet.session.persistent=true`，仓库未配置该属性。
- Spring Framework / Security 泛型父类型方法注解问题：虽启用方法安全，但未发现安全注解声明在泛型父类或接口上的适用调用链。

这些结论只表示当前仓库与已验证运行配置未发现可达证据；部署配置变化或新增组件用法后需要重新评估。

## 升级建议

1. 将 Spring Boot 从 3.5.5 升级到 3.5.16，并使用其 BOM 统一升级 Spring Framework 6.2.19、Spring Security 6.5.11、Spring AMQP 3.2.12、Spring Data 2025.0.13、Spring Retry 2.0.13 等版本；不要零散覆盖核心 Spring 传递依赖。
2. 3.5.16 是 Spring Boot 3.5.x 的最后一个开源支持版本；完成补丁升级后，另行规划向受支持主版本迁移。
3. 将 MinIO Java 至少升级到 8.6.0，并重新验证对象存储连接、上传/下载和错误响应解析。
4. 为 SMTP 与 RabbitMQ 启用 TLS、证书链和主机名校验，随后轮换可能经明文传输的凭据。
5. 升级后重新生成四个模块的依赖树，运行根测试、打包和完整漏洞扫描。

来源：[Spring Boot 3.5.16 发布说明](https://spring.io/blog/2026/06/25/spring-boot-3-5-16-available-now)、[Spring Boot 3.5.16 BOM](https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/3.5.16/spring-boot-dependencies-3.5.16.pom)
