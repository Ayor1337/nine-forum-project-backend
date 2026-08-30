# SEC-04：管理端 STOMP OWNER 授权加固

## Goal

修复管理端 STOMP 只校验 JWT 有效性而不校验当前管理员角色的问题，使当前版本只有数据库中仍为 `OWNER` 的账号能够连接管理端 WebSocket 并订阅实时举报流；普通论坛用户即使持有用户端签发的有效共享 JWT 也必须被拒绝。

## Background

- 漏洞分类：CWE-287、CWE-863；审计置信度：确定。
- `web/web-admin/src/main/java/com/ayor/interceptor/StompAuthInterceptor.java:30-47` 的 CONNECT 只执行 `JWTUtils.resolveJwt` / `toUser`，SUBSCRIBE 只检查是否存在 `Principal` 和目的地是否为 `/topic/reports`。
- `JWTUtils.toUser` 信任 token 内的 authorities；管理端 HTTP 已采用“JWT 只确认身份、数据库角色决定当前权限”的模式，因此 STOMP 不能以 token 中的旧角色声明作为管理权限来源。
- 用户端与管理端共享登录态。普通论坛用户可取得有效 JWT，并可借当前缺陷监听含举报双方标识、目标和内容摘要的 `/topic/reports`。
- 当前唯一管理员角色是 `OWNER`；沿用已归档 `08-30-admin-auth-hardening` 的角色模型，不新增 `ADMIN` 或权限表门禁。
- 根 `pom.xml:44` 已锁定 Spring Framework `6.2.19`。Spring 官方公告确认 `6.2.19` 已修复 CVE-2026-41838；本任务验证实际解析版本，不重复修改到相同版本。

## Requirements

### SEC-04-R1：CONNECT 校验当前 OWNER 角色

- CONNECT 必须要求有效的 `Authorization: Bearer <JWT>`；缺失、无效或已失效 token 均拒绝。
- JWT 只提供账号身份。拦截器必须使用 token 中的账号名查询数据库当前角色，只有精确等于 `OWNER` 才能建立已认证 Principal。
- 账号不存在、角色不存在或角色不是 `OWNER` 均 fail-closed，不得回退使用 token 中的 authorities。
- 成功连接后，在会话中保存后续重新授权所需的账号身份；不得把敏感 token 保存到会话或日志。

### SEC-04-R2：SUBSCRIBE 重新校验当前 OWNER 角色

- SUBSCRIBE 必须存在由合法 CONNECT 建立的身份和账号会话信息。
- 每次 SUBSCRIBE 都必须按账号重新查询数据库当前角色；账号在连接后被降权或删除时，订阅必须失败。
- 只有当前角色为 `OWNER` 且 destination 精确为 `/topic/reports` 时允许订阅；其他 destination 继续拒绝。

### SEC-04-R3：测试、依赖与文档

- 增加拦截器测试，覆盖缺失/无效 token、普通用户有效 token、OWNER token、非允许 destination，以及连接后角色被撤销。
- 增加 Spring Messaging 契约测试，通过实际 `clientInboundChannel` 和已注册拦截器证明普通用户 token 的 CONNECT 失败、OWNER CONNECT/SUBSCRIBE 成功。
- 使用 Maven dependency tree 确认 `spring-websocket` 与 `spring-messaging` 均解析为 `6.2.19`；若解析结果低于该修复版本才调整依赖。
- 新增管理端 STOMP 安全规范并更新后端规范索引；修复完成后只更新 `security-audit-report.md` 中 SEC-04 的状态，保留文件内现有 SEC-01 用户改动。

## Acceptance Criteria

- [x] AC-1：缺失或无效 JWT 的 CONNECT 被拒绝，且不查询/建立管理员 Principal。
- [x] AC-2：普通用户有效共享 JWT 的 CONNECT 在查询到当前角色 `USER` 后被拒绝，无法订阅 `/topic/reports`。
- [x] AC-3：数据库当前角色为 `OWNER` 的有效 JWT 可以 CONNECT，并可订阅 `/topic/reports`。
- [x] AC-4：OWNER 建立连接后若数据库角色被改为非 OWNER，后续 SUBSCRIBE 被拒绝。
- [x] AC-5：任何主体订阅 `/topic/reports` 之外的 destination 都被拒绝。
- [x] AC-6：Spring Messaging 契约测试覆盖实际 inbound channel 注册，并包含“普通用户 token 必须失败”断言。
- [x] AC-7：`spring-websocket` 与 `spring-messaging` 的解析版本均为 `6.2.19`，`./mvnw.cmd -pl web/web-admin -am test` 通过。
- [x] AC-8：管理端 STOMP 安全规范记录身份来源、OWNER 查库门禁、重授权时点、目的地允许列表和必需测试；审计报告 SEC-04 标记为已修复。

## Out of Scope

- 新增 `ADMIN` 角色、权限表授权、细粒度举报权限或数据库迁移。
- 用户端 `web-app` 的 STOMP 授权规则和公开订阅合同。
- 管理端 STOMP SEND 业务、举报消息字段或 `/topic/reports` destination 重命名。
- 升级到 Spring Framework 7 / Spring Boot 4，或处理与本漏洞无关的其他依赖公告。
