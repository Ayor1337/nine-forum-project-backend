# 修复注册邮件滥用与错误 TTL

## Goal

修复公开注册验证邮件接口可被无限调用及邮箱验证 JWT Redis key 过期时间错误的问题，限制单一来源、单一邮箱和全站的邮件投递成本，并确保注册验证码仅能被成功消费一次。

## Background

- 安全编号：SEC-07；类别：CWE-400、CWE-799；置信度：确定；级别：中危。
- `web/web-app/src/main/java/com/ayor/controller/AuthorizeController.java:31-33` 暴露无需登录的 `POST /api/auth/register-verifications`。
- `web/web-app/src/main/java/com/ayor/service/impl/AuthorizeServiceImpl.java:31-35` 当前每次调用都会创建随机 JTI、写 Redis 并向 RabbitMQ 投递注册邮件。
- `common/src/main/java/com/ayor/util/JWTUtils.java:132-143` 当前把 `expire.getTime()` 的绝对 epoch 毫秒当作 Redis TTL，导致邮箱验证 key 存活远超 JWT 的三小时寿命。
- `web/web-app/src/main/java/com/ayor/service/impl/AccountServiceImpl.java:317-340` 当前只解析邮箱 JWT，不会原子消费对应 Redis key，token 可被重复提交。
- 仓库未发现注册邮件的 IP、邮箱或全局限流，也未发现可信代理配置或现成告警平台；`web/web-app` 已有 `StringRedisTemplate` 与 Lua 原子消费模式可复用。
- 接口成功响应中的 JTI 被 `/verify/{jti}` STOMP 订阅流程使用，必须保持该合同。

## Requirements

### R1：正确的邮箱 JWT TTL

- 邮箱 JWT Redis key 必须使用 JWT 过期时间减去当前时间所得的剩余时长，而不是绝对 epoch 时间。
- TTL 不得为负数；JWT 与 Redis key 的有效期应保持一致。

### R2：短窗口幂等

- 同一规范化邮箱在短窗口内的重复请求复用首次请求的 JTI。
- 幂等命中不得创建新 JWT key，也不得再次投递 RabbitMQ 邮件消息。
- 首次请求仍返回现有成功响应形状和 JTI 数据。

### R3：多维限流

- 对注册验证邮件请求同时实施来源 IP、规范化邮箱与全局配额限制。
- 限流检查与计数必须在 Redis 中原子执行，避免并发绕过。
- 被拒绝的请求不得创建邮箱 JWT key 或投递邮件消息。
- 超限响应使用 HTTP 429 和现有 `Result<T>` JSON 结构，并提供可理解但不泄露邮箱是否存在的信息。
- 来源 IP 默认使用 Servlet 已解析的 `remoteAddr`，不直接信任客户端可伪造的转发头；反向代理部署需在基础设施边界规范化来源地址。

### R4：配额可观测性

- 在全局配额接近上限时产生不包含邮箱、JWT 或原始 IP 的结构化告警日志。
- 告警在同一限流窗口内去重，避免攻击者反向制造日志洪泛。

### R5：注册 token 原子消费

- 账户注册流程必须以原子方式校验并消费邮箱 JWT 对应的 Redis key，确保并发请求中最多一个请求能继续注册。
- token 无效、过期、已消费或邮箱 JWT key 不存在时统一返回现有“验证失败”业务结果。

### R6：配置与兼容性

- 幂等窗口、IP/邮箱/全局限额、限流窗口及告警阈值必须集中配置。
- 默认策略为：同邮箱 60 秒幂等；单邮箱 15 分钟最多 3 封；单 IP 15 分钟最多 10 封；全局 10 分钟最多 100 封；全局用量达到 80% 时告警。
- 保持现有 API 路径、请求 DTO、成功响应形状、RabbitMQ exchange/routing key、邮件消息模型和 `/verify/{jti}` STOMP 合同不变。

## Acceptance Criteria

- [x] AC1（R1）：单元测试捕获 Redis 写入参数，断言邮箱 JWT key TTL 接近 JWT 的剩余寿命且不是 epoch 毫秒。
- [x] AC2（R2）：同一邮箱在幂等窗口内连续请求返回同一 JTI，且只创建一个 JWT key、只投递一封邮件。
- [x] AC3（R3）：IP、邮箱和全局三个维度分别在边界内放行、超过边界返回 HTTP 429；拒绝路径不创建 JWT key且不投递邮件。
- [x] AC4（R3）：并发限流状态由单次 Redis Lua 脚本决定，不存在分别读写计数器造成的竞态窗口。
- [x] AC5（R4）：达到告警阈值时产生一次脱敏告警，同窗口重复请求不会重复告警。
- [x] AC6（R5）：同一有效 token 首次原子消费成功，随后消费失败；账户注册仅在消费成功时进入持久化流程。
- [x] AC7（R6）：现有授权服务、控制器合同和受影响模块测试通过。

## Out of Scope

- 引入验证码、人机验证、第三方 WAF/CDN 或新的外部限流/告警产品。
- 修改邮件正文、SMTP 供应商、RabbitMQ 拓扑、STOMP destination 或前端注册交互。
- 为历史错误 TTL key 编写批量清理迁移；部署后由 Redis 自然回收或运维按 `JWT_EMAIL_VERIFY` 前缀清理。
- 重新设计整个 JWT、登录会话或统一异常体系。
