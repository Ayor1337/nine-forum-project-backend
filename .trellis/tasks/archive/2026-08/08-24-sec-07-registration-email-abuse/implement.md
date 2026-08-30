# SEC-07 实施计划

## 1. 修复邮箱 JWT 生命周期

- [x] 在 `JWTUtils` 中分离邮箱 JWT 的创建元数据，允许发送门禁确定 JTI 后再签发并写 Redis。
- [x] Redis TTL 改为 `expiresAt - now` 的剩余毫秒，并保持非负。
- [x] 增加验签、过期检查和 Redis `DEL` 认领组成的 `consumeEmailJwt`，保留只读 `resolveEmailJwt` 给邮件链接验证。
- [x] 增加 TTL 与一次性消费回归测试。

## 2. 实现 Redis 注册邮件门禁

- [x] 新增带校验的 `RegistrationVerificationProperties` 并启用配置绑定。
- [x] 定义门禁接口与 `GRANTED`、`REUSED`、`LIMITED` 结果合同。
- [x] 用单个 Lua 脚本实现幂等优先、邮箱/IP/全局限流、各自 TTL 与全局告警去重。
- [x] 对邮箱和 IP 做稳定 SHA-256 key 摘要；不在日志或异常中暴露原值。
- [x] 增加 compare-and-delete 完成方法，只清理由当前 JTI 拥有的幂等 key。
- [x] 测试首次放行、幂等复用、三个配额边界、告警阈值/去重、key 和脚本参数。

## 3. 接入发送和 HTTP 边界

- [x] Controller 从 `HttpServletRequest#getRemoteAddr` 传入来源地址。
- [x] Authorize service 在创建 JWT 与投递 RabbitMQ 前调用门禁；复用时直接返回既有 JTI。
- [x] 新增专用限流异常和 advice，输出 HTTP 429、统一 `Result<T>` 与 `Retry-After`。
- [x] 更新 service/controller 测试，断言拒绝路径没有 JWT 或 RabbitMQ 副作用，成功路径保持现有 JTI、exchange、routing key 和消息体。

## 4. 接入注册成功清理

- [x] `AccountServiceImpl` 用 `consumeEmailJwt` 替换只读解析，使并发注册最多一个请求通过。
- [x] 全部账户初始化成功后，以邮箱和 JTI 清理对应短窗口幂等 key。
- [x] 增加账户 service 回归测试，覆盖无效/已消费 token 不进入持久化及成功清理。

## 5. 配置与验证

- [x] 在 `application.yml` 写入已确认的 60 秒、3/15 分钟、10/15 分钟、100/10 分钟与 80% 默认值。
- [x] 运行 `./mvnw.cmd -pl common test` 或 Windows 对应命令。
- [x] 运行 `./mvnw.cmd -pl web/web-app -am test`。
- [x] 运行 `./mvnw.cmd test` 做全仓回归。
- [x] 检查 `git diff`，确认未修改 RabbitMQ/STOMP/前端/数据库合同，且未把邮箱、IP、JWT 或配置秘密写入日志与新增文件。

## 风险文件与回滚点

- `common/.../JWTUtils.java` 同时服务登录 JWT；改动必须限定于邮箱 JWT 路径，并用现有登录会话测试防回归。
- `AccountServiceImpl` 在数据库事务内消费 Redis token；如果后续事务失败，token 不恢复。实现不得把消费移动到持久化之后而重新引入并发重放。
- Lua 返回协议、Java 解析器和测试必须同步修改；任何解析异常都应失败关闭。
- 若门禁接入导致兼容性问题，可整体回滚 gate/controller/service/config；不要回滚正确 TTL 的独立修复。

## 实施审核门

- [x] `prd.md`、`design.md`、`implement.md` 已由用户审核。
- [x] `implement.jsonl` 与 `check.jsonl` 已配置真实规范上下文。
- [x] 用户在最终规划摘要之后明确批准开始实施。
