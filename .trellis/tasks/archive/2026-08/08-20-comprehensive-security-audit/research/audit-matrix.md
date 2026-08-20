# 安全审计矩阵

审计日期：2026-08-20（Asia/Taipei）

## 基线

- Git 分支：`develope`
- Git 提交：`ce6f2fa40facf6b7be3a08f520d56d4dfd9e13f5`
- Java：Oracle JDK 17.0.12
- Maven Wrapper：Apache Maven 3.9.11
- Docker Engine：29.4.0
- 本地服务：MySQL、Redis、MinIO、RabbitMQ、Elasticsearch、Kibana 均在运行；MySQL 健康检查通过
- 数据库备份：见 `backup-manifest.md`
- 专用扫描器：`gitleaks`、`trivy`、`semgrep`、`grype`、`syft`、`osv-scanner`、`trufflehog`、`nuclei` 均未安装
- 初始工作树：安全审计任务文件与用于忽略数据库快照的根 `.gitignore` 为本任务改动；产品代码无改动

## 覆盖矩阵

| ID | 检查面 | 状态 | 证据/限制 |
| --- | --- | --- | --- |
| R1 | 秘密与配置安全 | 完成 | 当前树、37 个相关历史提交、Spring/Docker 配置；未复制秘密值 |
| R2 | 认证与会话安全 | 完成 | JWT、登录会话、邮件验证、Passkey、WebSocket/STOMP 调用链 |
| R3 | 授权与业务边界 | 完成 | 用户端/管理端 URL 与方法授权、关键资源所有权；严重/高危已二次复核 |
| R4 | 输入、数据与注入 | 完成 | DTO、Controller、Service、Mapper/XML、富文本、对象存储与资源耗尽路径 |
| R5 | 基础设施与供应链 | 完成 | Maven 依赖树、OSV 官方 API、官方公告、Docker 配置与运行态 |
| R6 | 测试、动态验证与报告 | 完成 | Maven 全量测试、localhost 非破坏性请求、交叉复核和最终报告 |

## 自动化运行记录

| 时间 | 命令/工具 | 结果 |
| --- | --- | --- |
| 2026-08-20 11:05 | `./mvnw.cmd clean test` | 通过；6 个模块构建成功，514 个测试，0 失败、0 错误、0 跳过 |
| 2026-08-20 11:10 | `./mvnw.cmd package -DskipTests` | 失败；根 POM 中 Spring Boot 插件的 Lombok `exclude` 含不受支持的 `version` 字段，打包配置无法解析 |
| 2026-08-20 11:22 | 管理端 localhost 匿名 GET | `/v3/api-docs` 返回 200；`/api/accounts` 与 `/api/roles` 未返回 401/403，而是进入业务层后因故意断开的数据库连接返回 500，动态确认全局放行 |
| 2026-08-20 11:23 | Redis/Elasticsearch localhost 只读探测 | Redis 无密码 PING 返回 PONG；Elasticsearch 根接口匿名返回 200；未执行写命令 |
| 2026-08-20 | OSV 官方 API 批量查询 | 完成；查询四个生产模块的 146 个去重 `compile` / `runtime` Maven 坐标，返回 80 条原始公告，已做项目可达性复核 |
| 2026-08-20 | OWASP Dependency-Check 12.2.2 | 未完成；无 NVD API Key，首次同步到约 40,000 / 380,852（约 11%）后主动停止，退出码 1 |

## 秘密扫描线索

- 两个 Web 应用的已跟踪 `application.yml` 含数据库、JWT、MinIO、RabbitMQ 等硬编码凭据；用户端还含邮件服务凭据。这里只记录位置，不复制秘密值。
- `.docker/environment/mysql.env` 是已跟踪文件并含数据库根密码。
- 当前邮件凭据指纹可在 37 个提交的历史树中找到；即使从当前文件删除，也必须考虑轮换与历史清理。
- 未发现已跟踪的 PEM、P12、PFX、JKS、SSH 私钥或明显 credentials/secrets 文件名。

## 已知限制

- 本机没有专用秘密、SAST、SBOM、容器或 DAST 扫描器；不会把人工搜索误称为这些工具的等价替代。
- OSV 批量扫描覆盖已解析的 Maven `compile` / `runtime` 坐标，但不等于调用级可达性扫描，也不覆盖构建插件、容器镜像、未解析组件或运行环境全部软件；Dependency-Check 的 NVD 同步未完成。
- 动态阶段不创建、删除或修改真实业务记录；需要认证身份的场景可能只能由既有测试和源码证据覆盖。

## 运行态漂移线索

- 当前工作树 Compose 声明 Elasticsearch 8.18.8，但运行容器报告 9.2.1，且容器标签中的配置文件路径来自另一 WSL 工作目录。需在基础设施审计中判定这是开发环境漂移还是预期状态。

## 动态验证安全边界

- 管理端存在 `@PostConstruct` 启动任务，会执行 `dashboard_activity` 的幂等刷新。首次发现后即停止；后续一次尝试使用 JDBC 会话变量强制只读，但驱动未阻止该语句，因此再次停止。两次均未发送 HTTP 请求，统计表仍为 13 行、最大活动 ID 仍为 581，未发现净业务数据变化。
- 最终管理端鉴权验证将数据源指向不存在的 localhost 端口，从连接层阻止数据库读写；只发送三个匿名 GET，并在取得状态码后停止进程。
- 容器运行态确认 MySQL、Redis、MinIO、Elasticsearch 的宿主映射绑定 `0.0.0.0` 与 `[::]`；外部可达性仍取决于宿主防火墙与上游网络。

## 供应链核对结果

- OSV 官方 API 完整批量查询 146 个去重坐标，共返回 80 条原始公告：5 条 Critical、28 条 High、35 条 Moderate、12 条 Low。原始分级不等于项目级严重度，详见 `osv-scan-summary.md`。
- 确认可适用或具有意义的条件风险包括 Spring Security CVE-2026-22732、Spring WebSocket CVE-2026-41838、MinIO Java CVE-2025-59952、Spring Boot Mail CVE-2026-40992；SMTP 与 RabbitMQ 当前还缺少传输加密。
- Tomcat Digest/HTTP2/Servlet constraint、Bouncy Castle GOST、Netty 解压、Jackson async/default typing、Spring Data 用户属性路径、用户可控 SpEL 等线索未发现当前可达证据，未把扫描器原始命中直接计为项目漏洞。
- 建议升级 Spring Boot 3.5.16，由 BOM 统一带入 Spring Framework 6.2.19、Spring Security 6.5.11 等修复版本；3.5.16 是 3.5.x 最后一个开源支持版本，之后需要规划迁移到受支持主版本。MinIO Java 需单独升级到至少 8.6.0。
