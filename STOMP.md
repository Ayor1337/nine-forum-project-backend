# STOMP 接入说明

## 1. 配置与前缀
- 握手端点：`/chatboard`、`/chat`、`/system`（`web/web-app/src/main/java/com/ayor/config/WebsocketConfiguration.java`），当前 `setAllowedOrigins("*")`，未启用 SockJS。
- 目的地前缀：应用前缀 `/app`，用户前缀 `/user`，简单代理前缀 `/broadcast`、`/transfer`、`/notif`、`/verify`。
- HTTP 放行：`/chat*` 在 `web/web-app/src/main/java/com/ayor/config/SecurityConfiguration.java` 中 `permitAll`，其它接口需 JWT。

## 2. 认证与入站控制
- CONNECT：`StompAuthInterceptor` 从 `Authorization` 解析 JWT，构建 `UsernamePasswordAuthenticationToken`。
- SUBSCRIBE：放行 `/verify`、`/broadcast`；`/transfer`、`/notif` 需认证用户。
- SEND：当前 `canSend` 恒 true，未做目的地校验（安全风险见下）。

## 3. 服务端推送路由
- 公共聊天：`/broadcast/topic/{topicId}`（`ChatboardHistoryServiceImpl.insertChatboardHistory`）。
- 私聊消息：`/transfer/conversation/{conversationId}`（`ConversationMessageServiceImpl.sendMessage`，双向推送）。
- 未读通知：
  - `/notif/unread/whisper`、`/notif/unread/user`、`/notif/unread`（`ChatNotifAspect`）。
  - `/notif/unread/{type}`，`type`∈`reply|system|user`（`MessageUnreadNotifAspect`）。
  - `/notif/reply`（`PostServiceImpl.insertPost`）。
  - `/notif/system`（`BroadcastServiceImpl.userSystemBroadcast`）。
- 邮件验证：`/verify/{jti}`（`AuthorizeServiceImpl.validateAuthorizeToken` 推送 `VerifyMessage`）。

## 4. 未读计数与订阅判断
- 订阅检测：`STOMPUtils` 通过 `SimpUserRegistry` 判断用户是否订阅某目的地，决定推送或累加未读。
- 未读存储：`MessageUnreadServiceImpl` 使用 Redis，枚举 `UnreadMessageType`（`model/src/main/java/com/ayor/type/UnreadMessageType.java`）。
- AOP 触发：`ChatNotifAspect`、`MessageUnreadNotifAspect` 用 SpEL 取方法参数，在消息发送/阅读时更新未读并推送。

## 5. 客户端接入要点
- 连接头：`Authorization: Bearer <JWT>`，否则只能订阅 `/broadcast`、`/verify`。
- 订阅：用户队列由 `/user` 前缀处理，客户端不用显式添加 `/user/`。
- 发送：业务消息目前通过 REST，`/app/**` 未配置 `@MessageMapping`。

## 6. 已知风险与改进
- SEND 未校验：客户端可直接向 `/broadcast`、`/transfer`、`/notif`、`/verify` 发送伪造帧绕业务逻辑。建议收敛客户端发送到 `/app/**` 并在拦截器/消息安全中拒绝向 broker 前缀 `SEND`。
- 跨域过宽：`setAllowedOrigins("*")` + `/chat*` 放行使任意站点可建 WS。建议改为具体前端域名（Spring 6 可用 `setAllowedOriginPatterns`）。
