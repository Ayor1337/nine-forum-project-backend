# 修复匿名 STOMP 消息注入

## 目标

在不改变浏览器使用 STOMP `CONNECT` 原生头传递 JWT 的架构下，消除客户端向 broker 目的地伪造消息的能力；保留公开论坛/聊天室订阅及登录前邮箱验证的必要读取能力。

## 已确认事实

- HTTP 安全配置将 `/chat`、`/chatboard`、`/system`、`/forum` 设为 `permitAll`，以便浏览器先完成 WebSocket 握手：`web/web-app/src/main/java/com/ayor/config/SecurityConfiguration.java:84-88,125-130`。
- `StompAuthInterceptor` 在 `CONNECT` 没有可用 JWT 时不拒绝会话；对通过端点白名单且不含 `/transfer` 的 `SEND` 默认放行：`web/web-app/src/main/java/com/ayor/interceptor/StompAuthInterceptor.java:67-81,150-180`。
- simple broker 接受 `/broadcast`、`/transfer`、`/notif`、`/verify`；WebSocket 端点允许任意 Origin：`web/web-app/src/main/java/com/ayor/config/WebsocketConfiguration.java:28-30,41-47`。
- `/broadcast`、`/transfer`、`/notif`、`/verify` 的现有业务消息由服务端 `SimpMessagingTemplate` 发出；客户端唯一现有的业务 `SEND` 入口为 `/app/conversations/{conversationId}/typing`。
- Spring Boot 3.5.5 解析 Spring Framework 6.2.10，落在 CVE-2026-41838 的受影响范围内。

## 需求

### R1：认证边界

- 保持 WebSocket 握手端点可匿名访问；认证延后至 STOMP `CONNECT`，不改为 Cookie/Session 认证。
- `CONNECT` 带有 JWT 时必须验证；无效或不可解析的非空 JWT 必须拒绝。
- 没有 JWT 的 `CONNECT` 可建立访客会话，但访客不具有任何写权限。

### R2：客户端发送授权

- 客户端 `SEND` 必须默认拒绝。
- 无论客户端是否认证，均禁止其向 `/broadcast/**`、`/transfer/**`、`/notif/**`、`/verify/**`、`/user/**` 等 broker 或用户目的地发送。
- 仅允许认证且通过会话成员校验的用户发送 `/app/conversations/{conversationId}/typing`。
- 未显式列出的 `/app/**` 目的地必须拒绝。

### R3：订阅授权

- 匿名访客仅可订阅公开 `/broadcast/**` 及登录前验证结果 `/verify/{jwtId}`。
- 用户通知、私聊和用户目的地订阅必须要求认证；私聊继续执行会话成员校验。
- 未知端点、缺失握手端点属性或不匹配的目的地必须失败关闭。

### R4：Origin 与依赖

- WebSocket Origin 必须改为配置化的显式允许列表，不得包含 `*`。
- 将整个 Spring Framework 版本统一升级到 6.2.19，修复 CVE-2026-41838；不得只覆盖单个 Spring 模块。

## 验收标准

- [ ] 无 JWT 访客能建立公开订阅所需的连接，但向任一 broker/用户目的地发送 STOMP `SEND` 均被拒绝。
- [ ] 有效 JWT 用户只能向会话成员关系通过校验的 typing 应用目的地发送；所有其他客户端 `SEND` 均被拒绝。
- [ ] 服务端现有论坛、聊天室、验证、私聊和通知推送行为不受影响。
- [ ] 所有 WebSocket Origin 来自部署配置中的显式列表，配置及测试中均不存在 `*`。
- [ ] `spring-websocket`、`spring-messaging` 等 Spring Framework 模块均解析为 6.2.19。
- [ ] 受影响模块测试通过，并新增上述授权边界的回归测试。

## 不在范围内

- 将 JWT 认证迁移为 Cookie/Session、替换 STOMP/simple broker，或重写现有消息负载协议。
- 修改邮箱验证令牌的业务生命周期；本任务只禁止客户端伪造验证事件。
- 对现有公开广播的内容可见性做产品策略调整。
