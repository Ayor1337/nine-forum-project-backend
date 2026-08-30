# 用户端 STOMP 安全契约

## 场景：用户端实时消息入口

### 1. 范围 / 触发条件

用户端 `web/web-app` 的 `/chatboard`、`/chat`、`/system`、`/forum` WebSocket 握手可以匿名，因为浏览器握手不能可靠地携带自定义 JWT 请求头。身份与目的地权限必须在 `StompAuthInterceptor` 的 STOMP 帧阶段执行；不要把 HTTP `permitAll` 当作消息发布许可。

### 2. 签名

- 配置：`nine-forum.cors.allowed-origins: List<String>`，由 HTTP CORS 与 WebSocket `setAllowedOrigins` 共同使用。
- CONNECT：原生头 `Authorization: Bearer <JWT>` 为可选字段。
- 唯一客户端 SEND：`/chat` 端点上的 `/app/conversations/{conversationId}/typing`。
- 服务端 broker 输出：`/broadcast/**`、`/verify/{jwtId}`、`/user/transfer/**`、`/user/notif/**`，由 `SimpMessagingTemplate` 产生。

### 3. 契约

- `allowed-origins` 必须为非空、无 `*` 的精确 Origin 列表；不得复用 WebAuthn 的 Origin 属性。
- 缺少 CONNECT `Authorization`：允许建立访客会话；非空但 JWT 无法验证：拒绝连接。
- 访客只能订阅 `/chatboard` 或 `/forum` 的 `/broadcast/**`，以及 `/system` 的单段 `/verify/{jwtId}`。
- 已认证用户才能订阅 `/chat`、`/system` 的 `/user/notif/**`；订阅 `/chat` 的 `/user/transfer/conversation/{id}`（可附 `/typing`）必须通过会话成员校验。
- 所有客户端对 broker 前缀或 `/user/**` 的 SEND 必须拒绝；typing SEND 必须已认证且通过同一会话成员校验。
- 缺失、未知或不匹配的握手端点一律失败关闭。

### 4. 校验与错误矩阵

| 条件 | 行为 |
| --- | --- |
| 空 CONNECT token | 允许访客连接 |
| 非空无效 CONNECT token | 抛出 `AccessDeniedException` |
| 访客 SEND，或 SEND 到 `/broadcast/**`、`/verify/**`、`/user/**` | 抛出 `AccessDeniedException` |
| 已认证 typing SEND 但非会话成员 | `AuthorizationService.assertCanAccessConversation` 拒绝 |
| Origin 为空、为空列表或含 `*` | 启动配置读取失败 |

### 5. 好 / 基准 / 坏案例

- 好：已认证会话成员向 `/app/conversations/7/typing` 发送；服务端校验后转发通知。
- 基准：未登录访客连接 `/forum` 并订阅 `/broadcast/page/home`。
- 坏：任何主体向 `/user/transfer/conversation/7` 或 `/broadcast/forum/...` 发送；不得因是会话成员而放行。

### 6. 必需测试

- `StompAuthInterceptorTest` 覆盖空 token、无效 token、有效 token 绑定主体、访客订阅、所有 broker 与 `/user/**` SEND 拒绝、typing 的成员校验，以及未知端点失败关闭。
- `WebsocketConfigurationTest` 断言配置 Origin 被传给 STOMP endpoint；`CorsPropertiesTest` 断言空值与通配符被拒绝。
- 更改 Spring Framework 版本时，运行根目录 `./mvnw.cmd test`，并用 dependency tree 确认两个 Web 模块的 `spring-websocket` 与 `spring-messaging` 使用同一已修复版本。

### 7. 错误与正确做法

#### 错误

```java
if (!destination.contains("/transfer")) {
    return true;
}
```

这会将 broker 目的地意外暴露给客户端。

#### 正确

```java
if (!CHAT_ENDPOINT.equals(endpointPath(accessor))) {
    return false;
}
Integer conversationId = resolveConversationId(destination, TYPING_SEND_DESTINATION);
if (conversationId == null || userId == null) {
    return false;
}
authorizationService.assertCanAccessConversation(userId, conversationId);
return true;
```

发送规则必须采用端点与目的地的精确允许列表；订阅用的 `/user/transfer/**` 匹配不可复用于 SEND。
