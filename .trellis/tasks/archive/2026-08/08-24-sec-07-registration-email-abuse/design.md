# SEC-07 技术设计

## 1. 变更边界

最小行为差距是：公开邮件入口缺少发送前的资源配额控制，邮箱 JWT key 的 TTL 写错，注册流程也没有一次性消费 token。修复落在实际拥有这些行为的三处：`AuthorizeController` 负责提取请求来源并映射 HTTP，`AuthorizeService` 负责发送前编排，`JWTUtils` 与 Redis 限流组件负责原子状态。

预计修改：

- `common/.../JWTUtils.java`：用剩余毫秒写邮箱 JWT TTL，并增加已验证 JWT 的原子消费入口。
- `web/web-app/.../config/RegistrationVerificationProperties.java` 与配置启用类：集中持有已确认的限流参数。
- `web/web-app/.../service/RegistrationVerificationGate.java` 及 Redis 实现：封装幂等、三维配额、告警去重和成功后的幂等 key 清理。
- `AuthorizeController`、`AuthorizeService`、`AuthorizeServiceImpl`：把 Servlet 解析后的来源地址送入发送前门禁，并保持成功 JTI 合同。
- 注册限流异常及 advice：返回 HTTP 429、`Result<T>` 和 `Retry-After`。
- `AccountServiceImpl`：注册持久化前原子认领 token，成功后清理对应幂等 key。
- `application.yml`：提供已确认的默认参数。
- 对应 `common`、service、controller 与配置测试：覆盖 TTL、幂等、三个限流边界、429、告警去重和 token 一次性。

明确不修改 RabbitMQ 拓扑、邮件消费者、STOMP destination、前端协议、数据库结构、登录 JWT 和历史 Redis 数据。

## 2. 请求数据流

```text
POST /api/auth/register-verifications
  -> 校验并规范化邮箱（trim + Locale.ROOT 小写）
  -> 使用 request.getRemoteAddr() 作为来源地址
  -> RegistrationVerificationGate.acquire(email, ip)
       -> 幂等命中：返回既有 JTI，不计数、不发信
       -> 任一配额超限：抛出限流异常，不创建 JWT、不发信
       -> 放行：原子递增邮箱/IP/全局计数，写入 email->JTI 幂等 key
  -> JWTUtils.createEmailJwt(email, JTI)
  -> RabbitMQ mail.direct/mail
  -> 返回 JTI
```

注册数据流：

```text
POST /api/auth/registrations
  -> 先检查用户名
  -> JWTUtils.consumeEmailJwt(token)
       -> 验签并检查 exp
       -> 以 Redis DEL 返回值原子决定唯一消费者
  -> 保存账户及初始化关联数据
  -> RegistrationVerificationGate.complete(email, JTI)
       -> Lua compare-and-delete，仅清理仍指向该 JTI 的幂等 key
```

token 在数据库副作用前被认领，避免并发注册同时通过。若后续数据库事务失败，该 token 不恢复；用户需重新获取邮件。这是以一次性安全性换取失败重试便利，且幂等窗口仅 60 秒。

## 3. Redis 合同

key 的邮箱与 IP 部分使用 SHA-256 十六进制摘要，避免把原始个人信息直接暴露在 Redis key 中：

| Key | Value | TTL |
| --- | --- | --- |
| `registration:verify:idem:{emailHash}` | JTI | 60 秒 |
| `registration:verify:email:{emailHash}` | 接受次数 | 15 分钟 |
| `registration:verify:ip:{ipHash}` | 接受次数 | 15 分钟 |
| `registration:verify:global` | 接受次数 | 10 分钟 |
| `registration:verify:global:alert` | `1` | 与全局计数剩余窗口一致 |
| `jwt:email:verify:{jti}` | 空字符串 | JWT 剩余寿命，约 3 小时 |

单次 Lua 脚本按以下顺序执行：

1. 读取幂等 key；存在则返回 `REUSED` 与原 JTI。
2. 读取三个计数器；任何计数已达到上限则返回统一 `LIMITED` 与相应剩余 TTL，不修改任何状态。
3. 三个计数器分别 `INCR`，首次写入时设置各自 `PEXPIRE`。
4. 写入 60 秒幂等 key。
5. 全局计数达到 80 时，以 `SET NX PX` 创建告警去重 key；只有创建者得到 `alert=true`。
6. 返回 `GRANTED`、JTI、全局计数和告警标志。

脚本返回值由一个明确的门禁结果类型解析，业务层不直接解释 Redis 字符串。Redis/JWT/MQ 基础设施故障保持异常失败，不降级为绕过限流发送邮件。

## 4. HTTP 与服务合同

- `AuthorizeService#createAuthorizeToken` 增加来源 IP 参数；返回值仍为供 `/verify/{jti}` 订阅使用的 JTI。
- 幂等命中仍返回 HTTP 200 与同一 JTI，调用者不需要区分首次与复用。
- 配额超限由专用异常携带真正可重试所需的等待秒数；多个维度同时阻塞时取最长剩余 TTL。advice 返回 HTTP 429、`Result.fail(429, "请求过于频繁，请稍后重试")` 和 `Retry-After`。
- 不向客户端说明触发的是邮箱、IP 还是全局维度，避免泄露策略细节。
- 全局 80% 告警只记录 count、limit 和 window，不记录邮箱、IP 或 JWT。

## 5. 配置

新增 `nine-forum.registration-verification`：

```yaml
idempotency-seconds: 60
email-limit: 3
email-window-seconds: 900
ip-limit: 10
ip-window-seconds: 900
global-limit: 100
global-window-seconds: 600
global-alert-percent: 80
```

属性在启动时校验为正数，告警百分比限制为 1–100。来源地址只读取容器解析后的 `remoteAddr`；可信代理/负载均衡必须在容器或部署层正确转换该值，本任务不直接信任 `X-Forwarded-For`。

## 6. 兼容性、风险与回滚

- API 路径、DTO JSON、成功返回、邮件消息和 STOMP 协议兼容。
- 429 是新增的正确 HTTP 语义；旧客户端若只读取响应体仍可看到 code/message。
- 共享出口 IP 可能触发 10/15 分钟限制，这是已确认策略的权衡；邮箱维度和幂等窗口会降低正常用户误伤。
- Redis 门禁与 RabbitMQ 不是分布式事务；门禁放行后若同步投递失败，60 秒幂等记录会暂时阻止重发，不会持续超过短窗口。
- 回滚时可撤销 controller/service/gate/config 改动；JWT TTL 修复与 token 一次性消费属于独立安全改进，不需要数据迁移。
