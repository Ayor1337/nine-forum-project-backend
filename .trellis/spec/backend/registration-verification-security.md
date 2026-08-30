# 注册验证邮件安全

## 1. Scope / Trigger

修改公开注册验证邮件、邮箱 JWT、Redis 注册配额、RabbitMQ 邮件投递或账户注册 token 校验时适用。目标是保证所有高成本副作用都在原子门禁之后发生，并让 JWT 的密码学有效期、Redis 状态与一次性消费保持一致。

## 2. Signatures

- `POST /api/auth/register-verifications`：请求 `RegDTO`，成功返回 `Result<String>`，其中 data 是 `/verify/{jti}` 的 JTI。
- `AuthorizeService#createAuthorizeToken(String email, String remoteAddress)`：规范化邮箱并在发信前获取门禁结果。
- `RegistrationVerificationGate#acquire(String email, String remoteAddress, String candidateJwtId)`：返回 `GRANTED`、`REUSED` 或 `LIMITED`。
- `RegistrationVerificationGate#complete(String email, String jwtId)`：compare-and-delete 清理当前 JTI 拥有的幂等 key。
- `JWTUtils#createEmailJwt(String email, String jwtId)`：使用预定 JTI 签发并写入正确剩余 TTL。
- `JWTUtils#consumeEmailJwt(String token)`：验签、检查期限并以 Redis `DEL` 的返回值决定唯一消费者。

## 3. Contracts

- 邮箱先执行 `trim().toLowerCase(Locale.ROOT)`；限流 key 中邮箱和 IP 使用 SHA-256 摘要，不写入原始个人信息。
- 来源地址使用 Servlet 已解析的 `request.getRemoteAddr()`；应用不得直接信任客户端提供的 `X-Forwarded-For`。反向代理必须在部署边界规范化地址。
- 门禁以单次 Redis Lua 执行：先查邮箱幂等 key，再检查邮箱/IP/全局计数，全部放行后才递增计数并写幂等 key。
- 幂等命中返回原 JTI，不创建邮箱 JWT key、不递增配额、不投递 RabbitMQ。
- 默认配置位于 `nine-forum.registration-verification`：幂等 60 秒；邮箱 3/900 秒；IP 10/900 秒；全局 100/600 秒；全局 80% 告警。
- 全局告警用 Redis `SET NX PX` 按窗口去重，日志只能包含 count、limit 和 window。
- 邮箱 JWT Redis TTL 必须是 `expiresAt - now` 的剩余毫秒，不能传绝对 epoch 毫秒。
- 注册在数据库副作用前原子消费 token；成功完成账户初始化后清理匹配的幂等 key。注册后续事务失败时 token 不恢复。

## 4. Validation & Error Matrix

| 条件 | 行为 |
| --- | --- |
| 首次请求且所有计数低于上限 | `GRANTED`，创建 JWT、写正确 TTL、投递一封邮件、HTTP 200 |
| 同邮箱在幂等窗口内重复请求 | `REUSED`，HTTP 200 返回原 JTI，无新增计数/JWT/MQ 副作用 |
| 邮箱、IP 或全局任一计数已达上限 | `LIMITED`，HTTP 429、`Result.fail(429, ...)` 与 `Retry-After`，无 JWT/MQ 副作用 |
| 多个维度同时超限 | `Retry-After` 取最长剩余 TTL，确保返回时间后所有阻塞维度均已解除 |
| Redis 脚本返回空值、未知状态或字段不合法 | 抛出基础设施异常并失败关闭，不绕过门禁发信 |
| JWT 无效、过期、Redis key 不存在或已消费 | 注册返回“验证失败”，不得进入账户持久化 |
| 全局用量首次达到告警阈值 | 记录一次脱敏 WARN；同窗口后续请求不重复记录 |

## 5. Good / Base / Bad Cases

- Good：同一邮箱快速重试得到同一 JTI；正常用户不会重复收信，也不会额外消耗配额。
- Base：第三次邮箱请求仍可放行，第四次在 15 分钟窗口内返回 429；IP 与全局维度采用相同“边界内放行、下一次拒绝”语义。
- Bad：先创建 JWT 或先投递 RabbitMQ，再分别读取和递增三个计数器；并发请求可以绕过限制且拒绝路径仍产生资源成本。
- Bad：用 `expire.getTime()` 作为 Redis TTL；这会把 epoch 时间当持续时间，制造长期 key。

## 6. Tests Required

- `JWTUtils` 单测捕获 `ValueOperations#set` 的 TTL，断言接近三小时剩余寿命，并验证同一 token 仅一次 Redis 删除认领成功。
- 门禁单测捕获 Lua、keys 和 ARGV，断言原始邮箱/IP 不进入 key、所有配额检查发生在首次 `INCR` 前、错误返回失败关闭、告警不泄露敏感数据。
- 授权 service 测试断言 `REUSED` 与 `LIMITED` 均不调用 `createEmailJwt` 或 RabbitMQ；`GRANTED` 保持 exchange、routing key 和 JTI 合同。
- Controller 测试断言使用 `remoteAddr`，限流返回 HTTP 429、code 429 与 `Retry-After`。
- 真实 Redis 集成测试必须显式启用并隔离逻辑 DB，验证邮箱/IP/全局在边界内放行、下一次拒绝，以及幂等不增加第二组计数；不得通配删除非测试数据。
- 账户 service 测试断言已消费 token 不持久化，成功路径调用匹配 JTI 的 `complete`。

## 7. Wrong vs Correct

### Wrong

```java
template.opsForValue().set(key, "", expire.getTime(), TimeUnit.MILLISECONDS);
String token = jwtUtils.createJwt(email);
rabbitTemplate.convertAndSend("mail.direct", "mail", message);
// 之后才做非原子的限流检查
```

### Correct

```java
RegistrationVerificationGate.Acquisition acquisition =
        gate.acquire(normalizedEmail, request.getRemoteAddr(), candidateJwtId);
if (acquisition.status() == RegistrationVerificationGate.Status.GRANTED) {
    JWTUtils.EmailJwt jwt = jwtUtils.createEmailJwt(normalizedEmail, acquisition.jwtId());
    rabbitTemplate.convertAndSend("mail.direct", "mail", messageFor(jwt));
}
```

正确顺序把 Redis 原子门禁放在 JWT key 与 RabbitMQ 副作用之前；TTL 使用 JWT 剩余寿命，注册使用 `consumeEmailJwt` 获得唯一消费权。
