# 账号会话安全

## 1. Scope / Trigger

适用于用户端登录 JWT 的创建、鉴权、单会话撤销，以及 `POST /api/users/me/password` 修改密码后的会话失效。凡是修改密码字段、JWT `sid`/`jti`、`account_login_session`、`LOGIN_SESSION_ACTIVE` 或 JWT 黑名单逻辑，都必须按本文同步检查 Controller、Service、Mapper、Redis 和测试。

## 2. Signatures

- HTTP：`POST /api/users/me/password`
- 请求 DTO：`PasswordChangeDTO(oldPassword, newPassword)`
- 账号业务：`AccountService.updatePasswordWithOld(String token, PasswordChangeDTO dto)`
- 全量撤销：`UserLoginSessionService.revokeAllSessions(Integer accountId)`
- 会话查询：`LoginSessionMapper.findActiveByAccountId(Integer accountId, Date now)`
- 鉴权入口：`JWTUtils.resolveJwt(String token)`

`findActiveByAccountId` 必须限定 `account_id`、`revoked_time IS NULL` 和 `expire_time > now`，不能查询或撤销其他账号的会话。

## 3. Contracts

- `oldPassword`、`newPassword` 都必填；`newPassword` 长度为 6–16，只允许 ASCII 字母、数字和下划线。
- Controller 请求体必须使用 `@Valid`，并继续返回 `Result<Void>` envelope。
- 只有旧密码正确、新旧密码不同且密码持久化成功后，才调用 `revokeAllSessions(accountId)`。
- 对查询到的每个既有有效会话：删除 `LOGIN_SESSION_ACTIVE + sid`，按 JWT 原过期时间写入 `JWT_BLACK_LIST + jti`，并记录 `revoked_time`。
- 改密请求本身的当前会话也在撤销范围内；成功响应后客户端必须重新登录。
- 已接受的残余风险：旧密码认证已经通过、但会话在全量撤销查询快照之后才创建时，可能错过本次撤销。严格消除此窗口需要账号级 token version、`password_changed_at` 或登录/改密按账号串行化。

## 4. Validation & Error Matrix

| 条件 | 结果 |
| --- | --- |
| DTO 缺字段、密码长度或字符不合法 | Controller 参数校验失败，envelope code `203`；不得调用账号服务 |
| 当前账号不存在 | 返回 `当前用户不存在`；不得撤销会话 |
| 旧密码错误 | 返回 `当前密码有误`；不得更新密码或撤销会话 |
| 新密码与旧密码相同 | 返回 `新的密码不能和旧的密码相同`；不得撤销会话 |
| 密码持久化失败 | 返回 `更新密码失败`；不得撤销会话 |
| 密码持久化成功 | 撤销查询时已存在的全部有效会话并返回成功 |

## 5. Good / Base / Bad Cases

- Good：账号有两个不同 `sid` 的有效 JWT；改密成功后两个活跃 key 均删除、两个 `jti` 均进入黑名单，`resolveJwt` 对两枚 JWT 都返回 `null`。
- Base：账号只有当前会话；改密成功后当前 JWT 失效，用户使用新密码重新登录。
- Bad：仅调用 `JWTUtils.invalidateJWT(token)`，这只处理当前 `jti`，同账号其他活跃 `sid` 仍可通过鉴权。
- Residual：认证请求在改密并发窗口内已通过旧密码、但尚未创建 session；这是当前快照方案明确记录的边界，不应误写为已有会话未撤销。

## 6. Tests Required

- `UserLoginSessionServiceImplTest`：预置同账号两个活跃 `sid`，先断言两枚 JWT 可被真实 `JWTUtils.resolveJwt` 解析；执行全量撤销后断言两枚均失败，并验证 Redis 删除、黑名单 TTL 和 `revoked_time`。
- `AccountServiceImplTest`：验证密码持久化成功后调用账号级全撤销；旧密码错误或持久化失败时不得调用。
- `UserControllerTest`：验证无效新密码（当前回归样例同时覆盖非法字符和长度不足）及缺失 `newPassword` 返回 code `203` 且不进入账号服务；合法 DTO 保持原路由与委托行为。
- 受影响模块至少运行 `./mvnw.cmd -pl web/web-app -am test`。

## 7. Wrong vs Correct

### Wrong

```java
if (updateById(account)) {
    jwtUtils.invalidateJWT(token); // 只撤销当前 JWT
}
```

### Correct

```java
if (updateById(account)) {
    loginSessionService.revokeAllSessions(accountId);
}
```

Controller 同时必须保留入口校验：

```java
public Result<Void> updatePassword(@RequestBody @Valid PasswordChangeDTO dto,
                                   HttpServletRequest request) {
    // existing Result envelope flow
}
```
