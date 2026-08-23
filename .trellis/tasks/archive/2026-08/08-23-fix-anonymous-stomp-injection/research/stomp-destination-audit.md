# Research: STOMP destination audit

- Query: 核对仓库内所有 WebSocket/STOMP 客户端、配置消费者及其 `SEND` / `SUBSCRIBE` destination，并评估匿名消息注入修复的兼容性风险。
- Scope: internal / external
- Date: 2026-08-23

## Findings

### 范围与客户端证据

- 全仓未发现 JavaScript/TypeScript/Vue 包清单或源码，也未发现 `WebSocketStompClient`、`StandardWebSocketClient`、`SockJsClient` 等出站客户端实现；本仓库只含两个 Spring 服务端。因此无法从本仓库列出浏览器实际调用的 `client.send()` / `subscribe()`；下列“客户端允许/预期”由 WebSocket 配置、入站拦截器、唯一 `@MessageMapping` 及单元测试反推。
- 用户端配置四个握手端点：`/chatboard`、`/chat`、`/system`、`/forum`，应用前缀为 `/app`，simple broker 前缀为 `/broadcast`、`/transfer`、`/notif`、`/verify`。见 `web/web-app/src/main/java/com/ayor/config/WebsocketConfiguration.java:27-46`。
- 管理端是独立 STOMP 边界：握手端点 `/reports`、应用前缀 `/app`、simple broker 前缀 `/topic`。见 `web/web-admin/src/main/java/com/ayor/config/WebsocketConfiguration.java:20-28`。

### 用户端：实际/预期客户端方向

| 握手端点 | 客户端 SUBSCRIBE（由服务端契约推断） | 客户端 SEND（由服务端契约推断） | 证据与风险 |
| --- | --- | --- | --- |
| `/chatboard` | `/broadcast/**`，例如 `/broadcast/topic/{topicId}` | 无合法业务入口 | 白名单只含 `/broadcast`（`StompAuthInterceptor.java:40-45`）；但现行 `canSend` 对非 `/transfer` 直接放行，因此访客可伪造 `/broadcast/**`（`StompAuthInterceptor.java:150-180`）。 |
| `/forum` | `/broadcast/**`，包括论坛主题/回帖与页面广播 | 无合法业务入口 | 服务端推送 `/broadcast/forum/topics/{id}/threads`、`/broadcast/forum/threads/{id}/posts`（`ForumRealtimeServiceImpl.java:24-25,66,81`）和 `/broadcast/page/**`（`PageBroadcastEventListener.java:26,30-37`）。同样存在 broker `SEND` 注入。 |
| `/system` | 访客：`/verify/{jwtId}`；已认证：`/user/notif/**` | 无合法业务入口 | `canSubscribe` 对 `/verify` 直接允许、`/notif` 仅要求认证（`StompAuthInterceptor.java:118-139`）；注册验证结果由服务端发送到 `/verify/{jwtId}`（`AuthorizeServiceImpl.java:50`）。当前 `canSend` 可对 `/verify/**`、`/notif/**` 放行。 |
| `/chat` | 已认证且为会话成员：`/user/transfer/conversation/{id}`（可含 `/typing`）；已认证：`/user/notif/**` | **唯一合法业务入口：**`/app/conversations/{conversationId}/typing` | `@MessageMapping` 定义该入口，并再次校验会话成员后向伙伴发送 `/transfer/conversation/{id}/typing`（`ConversationTypingController.java:28-47`）。现行拦截器测试也只覆盖此 SEND（`StompAuthInterceptorTest.java:27-37`）。当前实现仍允许已认证会话成员直接向 `/transfer/conversation/{id}` / `/typing` `SEND`，并允许 `/notif/**` 的 `SEND`。 |

- 用户 destination 使用的是 Spring 的 `/user` 语义：服务端调用 `convertAndSendToUser(accountId, "/transfer/..." 或 "/notif/...")`，而浏览器应订阅 `/user/transfer/...`、`/user/notif/...`；`STOMPUtils` 同时识别带或不带具体用户段的形式。见 `web/web-app/src/main/java/com/ayor/util/STOMPUtils.java:41-55`。
- 服务端产生（不是浏览器 `SEND`）的主要 destination 家族：`/broadcast/topic/{id}`、`/broadcast/page/**`、`/broadcast/forum/**`、`/verify/{jwtId}`、`/user/transfer/conversation/{id}`（含 `/typing`）和 `/user/notif/**`。代表性代码：`ChatboardHistoryServiceImpl.java:57`、`PageBroadcastEventListener.java:26-37`、`ConversationMessageServiceImpl.java:184-201`、`AuthorizeServiceImpl.java:50`、`FollowMessageServiceImpl.java:181-196`。

### 管理端消费者

- 管理端唯一明确订阅为已认证用户的 `/topic/reports`；拦截器拒绝其他订阅。见 `web/web-admin/src/main/java/com/ayor/interceptor/StompAuthInterceptor.java:30-48`。
- 管理端服务端会向 `/topic/reports` 推送。见 `web/web-admin/src/main/java/com/ayor/service/impl/ReportServiceImpl.java:60`。
- 该拦截器只处理 `CONNECT` 与 `SUBSCRIBE`，不处理 `SEND`；所以其 `/topic/**` broker destination 也没有客户端写入拒绝规则。见 `web/web-admin/src/main/java/com/ayor/interceptor/StompAuthInterceptor.java:30-49`。

### 安全边界中的现存模式

- 用户端 `CONNECT` 中 `resolveJwt()` 返回 `null` 时静默建立匿名会话；这同时覆盖“没有 header”与“非空但无效/不可解析的 header”。见 `web/web-app/src/main/java/com/ayor/interceptor/StompAuthInterceptor.java:67-81`。这不满足任务要求中“无 JWT 可访客连接、非空无效 JWT 必拒绝”的区分。
- 用户端目的地/端点匹配在 session attributes 缺失、`endpointPath` 缺失或端点未知时返回 `true`，为失败开放。见 `StompAuthInterceptor.java:190-208`。
- Spring 的 `StompWebSocketEndpointRegistration.setAllowedOrigins` 是浏览器 origin 限制接口，默认不放行任何 origin；官方 API 也说明 `*` 不能与 credential 组合，灵活模式应使用 `setAllowedOriginPatterns`。本任务要求显式列表，故应保留 `setAllowedOrigins` 并供应精确值，而非转为 pattern。外部参考：[Spring API](https://docs.spring.io/spring-framework/docs/6.2.19/javadoc-api/org/springframework/web/socket/config/annotation/WebMvcStompWebSocketEndpointRegistration.html)。

## Files found

- `web/web-app/src/main/java/com/ayor/config/WebsocketConfiguration.java` — 用户端握手端点、broker 和应用前缀、入站拦截器注册。
- `web/web-app/src/main/java/com/ayor/interceptor/StompAuthInterceptor.java` — 用户端 CONNECT/SUBSCRIBE/SEND 授权与端点白名单。
- `web/web-app/src/main/java/com/ayor/controller/ConversationTypingController.java` — 唯一 `@MessageMapping` 应用 SEND 消费者。
- `web/web-app/src/main/java/com/ayor/interceptor/WebsocketHandshakeInterceptor.java` — 写入 `endpointPath` 与 origin 握手属性。
- `web/web-app/src/main/java/com/ayor/util/STOMPUtils.java` — 服务端判断用户订阅时识别 `/user` destination。
- `web/web-app/src/main/java/com/ayor/listener/PageBroadcastEventListener.java` — 页面广播推送消费者。
- `web/web-app/src/test/java/com/ayor/interceptor/StompAuthInterceptorTest.java` — 现存 typing / 私聊 / presence 授权回归用例。
- `web/web-admin/src/main/java/com/ayor/config/WebsocketConfiguration.java` — 管理端独立 STOMP 配置。
- `web/web-admin/src/main/java/com/ayor/interceptor/StompAuthInterceptor.java` — 管理端 CONNECT/SUBSCRIBE 限制，未限制 SEND。
- `web/web-admin/src/main/java/com/ayor/service/impl/ReportServiceImpl.java` — 管理端 `/topic/reports` 服务端推送。

## Related specs

- `.trellis/spec/backend/directory-structure.md` — Web 模块的 config/interceptor/controller/service 责任边界。
- `.trellis/spec/backend/quality-guidelines.md` — 变更路由、配置、消息名时先搜全仓消费者，且为消息副作用补测试。
- `.trellis/spec/guides/cross-layer-thinking-guide.md` — 握手、STOMP 帧、controller、服务端推送之间的跨层契约检查。

## Concurrent-change addendum

- 最终只读核对时，用户端 `StompAuthInterceptor` 已在本调研期间被其他工作修改；上文的“现存模式”描述的是最初取证到的漏洞基线，不能再作为当前工作区状态。
- 当前用户端实现已区分空 `Authorization`（匿名连接）与非空但无效 token（抛出拒绝），见 `web/web-app/src/main/java/com/ayor/interceptor/StompAuthInterceptor.java:61-79`；`SEND` 只接受 `/chat` 端点、已认证主体、且正则匹配 `/app/conversations/{id}/typing` 的消息，并执行会话成员校验，见同文件 `36-38,152-165,206-214`。
- 当前 `SUBSCRIBE` 已对缺失/未知握手 endpoint 失败关闭（`StompAuthInterceptor.java:109-141,180-192`），并把访客范围收紧为 `/chatboard` 的 `/broadcast/topic/**`、`/forum` 的 `/broadcast/forum/**`、`/system` 的 `/verify/{single-segment}`。对应回归测试已出现于 `web/web-app/src/test/java/com/ayor/interceptor/StompAuthInterceptorTest.java:28-139`。
- **待确认兼容性风险：**服务端仍向 `/broadcast/page/home`、`/broadcast/page/themes/{id}`、`/broadcast/page/topics/{id}` 发送（`PageBroadcastEventListener.java:26,30-37`），但当前 `/forum` 只允许 `/broadcast/forum/**`、`/chatboard` 只允许 `/broadcast/topic/**`。若任何实际浏览器曾订阅 `/broadcast/page/**`，该订阅会在新规则下被拒绝；仓库不含前端，必须由前端调用或端到端测试确认目标握手端点后再定稿。

## Caveats / Not Found

- 未包含前端仓库或部署端的 STOMP 配置；上线前必须以实际前端的 `CONNECT`、`subscribe`、`publish/send` 调用复核此表，尤其是 `/user/**` 订阅写法。
- 本任务 PRD 聚焦用户端四个端点；管理端 `/reports` 不应被用户端规则误伤，但其开放 `SEND` 与通配 origin 是独立、同类的安全风险，需明确是否纳入本任务。
- 不应以“禁止客户端 SEND”为由删除 `SimpMessagingTemplate` 推送或收缩 broker 前缀；它们走服务端出站通道，不经过客户端入站 `ChannelInterceptor` 的授权分支。
