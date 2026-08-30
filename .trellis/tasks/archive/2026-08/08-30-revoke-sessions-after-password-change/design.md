# Technical Design

## Boundaries

- `UserController`：仅负责请求 DTO 校验和现有 `Result<Void>` 封装。
- `AccountServiceImpl`：保持密码校验、编码和持久化的业务顺序；成功持久化后委托会话服务撤销该账号全部会话。
- `UserLoginSessionService` / `UserLoginSessionServiceImpl`：新增账号级全会话撤销能力，统一复用单会话撤销的 Redis、JWT 黑名单和数据库状态副作用。
- `LoginSessionMapper`：查询账号名下仍未撤销且尚未过期的会话，不修改 schema。

## Data Flow

```text
POST /api/users/me/password
  -> @Valid PasswordChangeDTO
  -> AccountServiceImpl 校验旧密码并更新密码哈希
  -> UserLoginSessionService.revokeAllSessions(accountId)
  -> LoginSessionMapper 查询该账号有效会话
  -> 对每个会话删除 LOGIN_SESSION_ACTIVE:{sid}
  -> 写入 JWT_BLACK_LIST:{jti}（TTL 至 JWT 原过期时间）
  -> 写入 revoked_time
  -> 当前请求返回成功，后续所有旧 JWT 鉴权失败
```

## Key Decisions

- 复用现有 session 表和 Redis key，而不是增加账号级 token version。这样改动范围小、无需 schema 迁移，并与现有手动撤销会话语义一致。
- 撤销范围包含当前会话。安全语义清晰：修改密码后所有既有凭据失效，用户必须用新密码重新登录。
- 查询仅包含 `revoked_time IS NULL` 且未过期的会话；已撤销或已自然过期的 JWT 不需要重复写 Redis 黑名单。
- 不单独保留 `JWTUtils.invalidateJWT(token)`；当前 JWT 由统一的全会话撤销路径处理，避免两套撤销逻辑漂移。

## Consistency and Failure Behavior

- `AccountServiceImpl` 已有 Spring 事务；数据库密码更新和 `revoked_time` 写入处于调用事务中。
- Redis 不参与数据库事务。若 Redis 操作抛错，请求失败且数据库事务回滚，但已执行的 Redis 删除不可回滚；这与当前单会话撤销的基础设施边界一致。本修复不引入分布式事务。
- 对已到期会话不写入零 TTL 黑名单；JWT 自身过期校验继续兜底。
- 质量检查发现快照方案无法覆盖并发认证后置建会话：旧密码认证已通过、但会话记录在 `findActiveByAccountId` 查询后才插入时会漏撤销。用户确认本次 SEC-08 的完成标准是撤销改密时已存在的全部有效会话；该窗口作为独立残余风险写入审计报告，不阻断本次交付。

## Compatibility and Rollback

- 对外路由、请求字段、响应结构不变；唯一用户可感知变化是改密成功后所有设备退出登录，以及原本未生效的新密码约束开始生效。
- 回滚时可撤回 Service/Mapper/Controller 及测试改动；无数据库迁移和数据回填。
