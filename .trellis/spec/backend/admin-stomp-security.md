# 管理端 STOMP 安全

## 1. Scope / Trigger

适用于 `web/web-admin` 的 STOMP 入站消息通道。管理端与用户端共享登录态，因此 WebSocket 握手成功不代表客户端已经获得管理端消息权限；管理权限必须在 STOMP `CONNECT` 和 `SUBSCRIBE` 阶段执行。

## 2. Signatures

- 管理端 endpoint：`/reports`。
- CONNECT 原生头：`Authorization: Bearer <JWT>`，必填。
- JWT 身份 claim：`name`，作为数据库角色查询键。
- 会话属性：`adminUsername`，只保存账号名，不保存原始 JWT。
- 当前唯一允许的订阅 destination：`/topic/reports`。
- 当前管理员角色：数据库角色名 `OWNER`，Principal authority 为 `ROLE_OWNER`。

## 3. Contracts

- `JWTUtils` 校验 JWT 签名、有效期、黑名单和登录会话状态；JWT 只用于确认身份，token 内的 `authorities` 不参与管理权限决策。
- CONNECT 从 JWT 的 `name` claim 取得账号名，再调用 `RoleMapper#getRoleNameByUsername` 查询数据库当前角色。只有角色精确为 `OWNER` 才建立 Principal。
- 账号名缺失、账号不存在、角色不存在、角色不是 `OWNER`，或 JWT/数据库查询异常时必须失败关闭，不建立 Principal，也不暴露底层异常。
- 认证成功后 Principal 只授予 `ROLE_OWNER`，并把账号名写入 `adminUsername` 会话属性。
- 每次 SUBSCRIBE 都必须使用 `adminUsername` 重新查询数据库当前角色，以覆盖连接建立后账号被降权或删除的情况。
- SUBSCRIBE 只允许精确 `/topic/reports`；不得使用前缀或包含匹配。

## 4. Validation & Error Matrix

| 帧/条件 | 行为 |
| --- | --- |
| CONNECT 缺失、空白、无效 JWT，或 JWT 解析异常 | 拒绝，`未授权连接` |
| CONNECT 缺少 `name` claim，或当前角色不是 OWNER/查询失败 | 拒绝，`权限不足` |
| CONNECT 当前角色为 OWNER | 建立只含 `ROLE_OWNER` 的 Principal，并保存账号名 |
| SUBSCRIBE 没有 Principal 或 `adminUsername` | 拒绝，`未授权订阅` |
| SUBSCRIBE 当前角色已不是 OWNER/查询失败 | 拒绝，`权限不足` |
| SUBSCRIBE destination 不是 `/topic/reports` | 拒绝，`无权订阅该地址` |
| SUBSCRIBE 当前角色为 OWNER 且 destination 精确匹配 | 允许 |

## 5. Good / Base / Bad Cases

- Good：有效 JWT 的 `name=owner`，数据库当前角色为 `OWNER`；CONNECT 成功并订阅 `/topic/reports`。
- Base：缺失或无效 JWT 的 CONNECT 直接失败，不执行数据库角色查询。
- Bad：普通用户有效共享 JWT 即使携带旧的 `ROLE_OWNER` claim，数据库当前角色为 `USER` 时仍必须失败。
- Bad：OWNER CONNECT 后被降权或删除，后续 SUBSCRIBE 重新查库并失败。
- Bad：当前 OWNER 订阅 `/topic/reports/extra`，不得因前缀相同而放行。

## 6. Tests Required

- `StompAuthInterceptorTest` 覆盖缺失/无效 JWT、缺失 `name` claim、JWT/角色查询异常、账号/角色不存在和普通用户有效共享 JWT；即使 token authorities 伪装为 `ROLE_OWNER` 也必须失败。
- OWNER CONNECT 测试断言 Principal 只含 `ROLE_OWNER`，session 只保存账号名且不包含原始 token。
- SUBSCRIBE 测试覆盖 `/topic/reports` 成功、其他 destination 失败，以及连接后角色被撤销时重新查库失败。
- `AdminStompSecurityContractTest` 必须通过实际 `clientInboundChannel` 验证拦截器已注册，并断言普通用户 CONNECT 的失败根因为 `AccessDeniedException`。

验证命令：

```powershell
.\mvnw.cmd -pl web/web-admin -am test
.\mvnw.cmd -pl web/web-admin -am dependency:tree '-Dincludes=org.springframework:spring-websocket,org.springframework:spring-messaging'
```

## 7. Wrong vs Correct

Wrong：token 有效或 token 自带 OWNER claim 就直接建立管理端 Principal；共享登录态下，普通用户和旧角色 token 都能越过边界。

```java
UserDetails user = jwtUtils.toUser(jwt);
accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
```

Correct：token 只提供账号身份，CONNECT 和 SUBSCRIBE 都以数据库当前角色为准，并精确匹配举报目的地。

```java
String username = jwt.getClaim("name").asString();
if (!"OWNER".equals(roleMapper.getRoleNameByUsername(username))) {
    throw new AccessDeniedException("权限不足");
}
if (command == StompCommand.SUBSCRIBE && !"/topic/reports".equals(destination)) {
    throw new AccessDeniedException("无权订阅该地址");
}
```
