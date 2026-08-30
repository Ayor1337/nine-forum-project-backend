# 修复改密后旧会话未全部撤销

## Goal

修复 SEC-08（CWE-613）：账号密码修改成功后撤销该账号的全部登录会话，避免已泄露的旧 JWT 在密码修改后继续访问；同时恢复密码修改入口对 DTO 约束的执行。

## Background

- `web/web-app/src/main/java/com/ayor/service/impl/AccountServiceImpl.java:428-452` 在密码更新成功后仅调用 `JWTUtils.invalidateJWT(token)`，只将当前 JWT 的 `jti` 加入黑名单。
- `common/src/main/java/com/ayor/util/JWTUtils.java:177-193` 对普通 JWT 只检查 `jti` 黑名单和 `LOGIN_SESSION_ACTIVE + sid`；同账号其他活跃 `sid` 不会因改密失效。
- `web/web-app/src/main/java/com/ayor/controller/UserController.java:266-270` 的 `PasswordChangeDTO` 请求体缺少 `@Valid`，导致 DTO 上的新密码长度和字符约束未在该入口执行。
- 当前会话记录已保存 `account_id`、`session_id`、`jwt_id` 和过期时间，可在不新增账号级 token version 或数据库字段的情况下完成全量撤销。

## Requirements

- R1：只有密码数据库更新成功后才撤销会话；密码校验失败或持久化失败时不得撤销任何会话。
- R2：密码更新成功后，撤销该账号全部未撤销的有效登录会话，包括发起改密的当前会话和同账号其他会话。
- R3：撤销每个会话时删除对应 `LOGIN_SESSION_ACTIVE + sid`，将其 `jwt_id` 加入黑名单至原过期时间，并记录 `revoked_time`。
- R4：`POST /api/users/me/password` 对 `PasswordChangeDTO` 执行 Jakarta Validation，沿用项目现有 code `203` 参数错误响应。
- R5：保持现有 RPC 路由、`Result<Void>` envelope、密码规则和成功后需要重新登录的行为，不新增 schema、配置或公开响应字段。
- R6：同步更新安全审计报告中 SEC-08 的修复状态和验证证据。
- R7：在安全审计报告记录并发认证窗口的残余风险：旧密码认证已通过但会话在撤销快照后才创建时，可能错过本次撤销；本次不扩大到 token version。

## Acceptance Criteria

- [x] AC1：回归测试构造同账号至少两个未撤销会话；改密成功后两个会话的活跃 Redis key 都被删除、两个 JWT 都被加入黑名单，并写入撤销时间。
- [x] AC2：密码修改服务测试证明数据库更新成功后调用账号级全会话撤销，而不是只失效当前 token。
- [x] AC3：密码校验或数据库更新失败的测试证明不会触发全会话撤销。
- [x] AC4：Controller 测试提交长度不足或含非法字符的新密码，返回参数校验失败且不调用账号服务。
- [x] AC5：有效 DTO 仍调用原账号密码修改服务，接口路径和 envelope 不变。
- [x] AC6：受影响模块测试 `./mvnw.cmd -pl web/web-app -am test` 通过。
- [x] AC7：`security-audit-report.md` 将 SEC-08 标记为已修复，并记录全会话撤销与 `@Valid` 验证。
- [x] AC8：`security-audit-report.md` 明确区分已修复的“既有会话继续有效”与未覆盖的并发认证后置建会话窗口。

## Out of Scope

- 修改 JWT 默认有效期、登录签发协议或前端重新登录交互。
- 撤销会话之外的设备信任、Passkey 或邮箱验证 token。
- 引入账号级 token version、`password_changed_at` 或跨登录/改密的串行化协议；并发认证窗口作为独立残余风险记录。
