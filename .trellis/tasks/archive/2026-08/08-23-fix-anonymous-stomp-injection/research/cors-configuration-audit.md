# Research: CORS and application configuration audit

- Query: 核对 HTTP CORS、WebSocket Origin 和应用配置的来源，找出需要配置化及可能影响的消费者。
- Scope: internal / external
- Date: 2026-08-23

## Findings

### 配置来源分离

| 应用 | HTTP CORS 来源 | WebSocket Origin 来源 | YAML 中相关属性 | 结论 |
| --- | --- | --- | --- | --- |
| `web-app` | `CorsConfig` 中硬编码精确 HTTP origins；`SecurityConfiguration` 通过 `.cors(Customizer.withDefaults())` 消费该 bean。见 `CorsConfig.java:20-32`、`SecurityConfiguration.java:123-152`。 | `WebsocketConfiguration` 对四个握手端点硬编码 `.setAllowedOrigins("*")`。见 `WebsocketConfiguration.java:27-30`。 | `spring.security.webauthn.allowed-origins` 只绑定 WebAuthn 属性，非 HTTP CORS/WS Origin。见 `application.yml:16-25`、`WebAuthnProperties.java:21-29`。 | 三者目前彼此独立；没有可供 WS 读取的统一部署配置。 |
| `web-admin` | `CorsConfig` 中硬编码一个本地 origin；`SecurityConfiguration` 同样启用默认 CORS bean。见 `CorsConfig.java:18-28`、`SecurityConfiguration.java:55-74`。 | `WebsocketConfiguration` 对 `/reports` 硬编码 `.setAllowedOrigins("*")`。见 `WebsocketConfiguration.java:20-23`。 | 未发现 admin WS origin 属性。 | 管理端也存在与 HTTP CORS 脱钩的通配 WS Origin。 |

- 全仓配置文件枚举仅有 `web/web-app/src/main/resources/application.yml`、`web/web-admin/src/main/resources/application.yml`、`model/src/main/resources/application.yml`、`.docker/docker-compose.yaml` 和数据库环境文件；未找到 profile 专用 application 文件、`.env` 或部署变量对 origin 的覆写。
- `WebsocketHandshakeInterceptor` 将请求 origin 仅保存为 session attribute，并不做验证；真正拒绝与否目前完全由 `.setAllowedOrigins(...)` 决定。见 `web/web-app/src/main/java/com/ayor/interceptor/WebsocketHandshakeInterceptor.java:23-30`。
- 用户端 HTTP 安全把 `/chat`、`/chatboard`、`/system`、`/forum` 设为匿名访问，符合“握手可匿名、STOMP 后续授权”的架构。见 `web/web-app/src/main/java/com/ayor/config/SecurityConfiguration.java:84-95,123-130`。

### 建议的配置归属（供实现方案使用）

- 将用户端 WebSocket 的显式 origin 列表放在 `web/web-app/src/main/resources/application.yml` 的应用专属命名空间（例如 `nine-forum.websocket.allowed-origins`），以 `@ConfigurationProperties` 注入 `WebsocketConfiguration`；不要复用 WebAuthn 的 `spring.security.webauthn.allowed-origins`，两者的安全边界和部署需求不一定一致。
- 若需要统一 HTTP CORS 与用户端 WebSocket origin，应由一个明确的应用级属性对象同时供 `CorsConfig` 和 `WebsocketConfiguration` 消费；在变更前先确认 HTTP API 是否仍须保留比 WebSocket 更宽/更窄的来源集。
- 管理端需要自己的同名/平行配置属性及其 endpoint `/reports` 的决定，不能由用户端 `web-app` 配置类跨模块控制。

### 兼容性风险

- 用显式 WS 列表替代 `*` 后，漏掉生产域名、带端口的本地开发地址或管理前端 origin 会在握手阶段失败；STOMP `CONNECT` 头无法绕过该失败。
- HTTP CORS 的 `allowCredentials(true)` 目前与精确 origins 共存；Spring API 明确指出 `*` 不能与 credential 结合。尽管当前 WS 端点不启用 SockJS，仍应避免把 HTTP 的通配 header/方法策略机械复制为 WS origin pattern。外部参考：[Spring WebSocket endpoint API](https://docs.spring.io/spring-framework/docs/6.2.19/javadoc-api/org/springframework/web/socket/config/annotation/WebMvcStompWebSocketEndpointRegistration.html)。
- 现有 `WebsocketConfigurationTest` mock/断言的是 `setAllowedOrigins` 及端点数组（`web/web-app/src/test/java/com/ayor/config/WebsocketConfigurationTest.java:25-40`）；改为属性注入后应同步覆盖“配置值被传入”和“不得含 `*`”，否则测试会保留旧构造器契约。
- WebAuthn origin 保持独立会产生两套部署值，风险是环境配置漂移；若决定共享列表，需同时回归 passkey 的 `rp-id`/origin 契约，不宜在本漏洞修复中默认合并。

## Files found

- `web/web-app/src/main/java/com/ayor/config/CorsConfig.java` — 用户端 HTTP `CorsConfigurationSource`。
- `web/web-app/src/main/java/com/ayor/config/SecurityConfiguration.java` — 用户端 HTTP 公开握手路由与 CORS 启用。
- `web/web-app/src/main/java/com/ayor/config/WebsocketConfiguration.java` — 用户端 WebSocket endpoint origin 规则。
- `web/web-app/src/main/resources/application.yml` — 用户端应用配置；只含 WebAuthn allowed origins，未含 WS origin。
- `web/web-app/src/main/java/com/ayor/config/WebAuthnProperties.java` — WebAuthn 专用 origin 属性绑定。
- `web/web-app/src/main/java/com/ayor/interceptor/WebsocketHandshakeInterceptor.java` — origin 记录但不校验。
- `web/web-admin/src/main/java/com/ayor/config/CorsConfig.java` — 管理端 HTTP CORS。
- `web/web-admin/src/main/java/com/ayor/config/SecurityConfiguration.java` — 管理端 CORS 启用。
- `web/web-admin/src/main/java/com/ayor/config/WebsocketConfiguration.java` — 管理端 `/reports` WebSocket origin 规则。
- `web/web-app/src/test/java/com/ayor/config/WebsocketConfigurationTest.java` — 用户端 WS 配置现有测试契约。

## Related specs

- `.trellis/spec/backend/directory-structure.md` — 配置归属应留在各自 Web 应用，而非 `common` 反向依赖 Web 模块。
- `.trellis/spec/backend/quality-guidelines.md` — 配置值修改前须搜索所有消费者并补受影响测试。
- `.trellis/spec/guides/code-reuse-thinking-guide.md` — 修改配置时先寻找重复/并行消费者。

## Concurrent-change addendum

- 最终只读核对时，用户端 CORS/WS 实现已在本调研期间由其他工作更新；上文硬编码/通配的用户端描述应视为漏洞修复前基线。
- 当前用户端新增 `CorsProperties`，绑定 `nine-forum.cors.allowed-origins`，并被 HTTP `CorsConfig` 与 `WebsocketConfiguration` 共同消费。证据：`web/web-app/src/main/java/com/ayor/config/CorsProperties.java:1-17`、`CorsConfig.java:12-39`、`WebsocketConfiguration.java:20,28-31`、`application.yml:49-55`；现有测试也验证属性值传给 WS endpoint（`WebsocketConfigurationTest.java:22-40`）。
- 管理端仍保留 `/reports` 的 `.setAllowedOrigins("*")`（`web/web-admin/src/main/java/com/ayor/config/WebsocketConfiguration.java:20-23`）。若 R4 是全仓 WebSocket Origin 要求，这一配置仍是未处理消费者；若只限定用户端，应在任务范围中显式排除并登记后续安全任务。

## Caveats / Not Found

- 由于未发现前端项目或运行时环境变量，无法证明部署生产 origin 的完整清单；应由部署拥有者提供显式列表。
- 未发现 SockJS 调用（无 `.withSockJS()`）；因而限制 origin 不会触发 SockJS iframe transport 的兼容性问题，但若未来启用 SockJS，需要重新评估 Spring API 所述的旧浏览器 transport 限制。
