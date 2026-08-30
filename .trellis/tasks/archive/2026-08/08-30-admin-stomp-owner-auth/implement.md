# SEC-04 Implementation Plan

## Task List

- [x] 1. 先补 `StompAuthInterceptorTest` 与 Spring Messaging 契约测试，复现普通用户有效 JWT 可 CONNECT/订阅的问题，并覆盖角色撤销后的 SUBSCRIBE。
- [x] 2. 修改管理端 `StompAuthInterceptor`：构造器注入 `JWTUtils` / `RoleMapper`，CONNECT 从 token 提取账号名并查库校验 OWNER，建立最小 Principal 和 username 会话属性。
- [x] 3. 修改 SUBSCRIBE：要求合法会话身份，每次查库确认当前仍为 OWNER，再精确允许 `/topic/reports`；保留其他帧现有行为。
- [x] 4. 补齐配置注册测试与边界用例，确认非 OWNER、缺失账号 claim、角色不存在、非法 destination 均 fail-closed。
- [x] 5. 新增 `.trellis/spec/backend/admin-stomp-security.md`、更新规范索引，并在不覆盖 SEC-01 现有改动的前提下标记审计报告 SEC-04 已修复。
- [x] 6. 运行管理端测试和 dependency tree；检查实际 diff 后执行 `trellis-check` 全量质量门禁。

## Validation

```powershell
.\mvnw.cmd -pl web/web-admin -am test
.\mvnw.cmd -pl web/web-admin -am dependency:tree '-Dincludes=org.springframework:spring-websocket,org.springframework:spring-messaging'
```

重点断言：

- 普通用户有效共享 JWT 在 CONNECT 阶段失败。
- OWNER CONNECT 后可订阅唯一允许的 `/topic/reports`。
- 连接后角色从 OWNER 变更为 USER，SUBSCRIBE 阶段失败。
- token authorities 即使伪装为 `ROLE_OWNER`，数据库当前角色非 OWNER 时仍失败。
- `spring-websocket` 与 `spring-messaging` 都解析为 `6.2.19`。

## Risk / Rollback Points

- 风险：异步 inbound channel 测试的异常可能包装为 `MessageDeliveryException`；断言应验证根因和授权结果，不依赖线程时序文本。
- 风险：修改 Principal username 可能影响会话语义；设计通过独立 session attribute 保存数据库查询键，避免改变当前 ID Principal 约定。
- 风险：`security-audit-report.md` 已有用户未提交修改；只编辑 SEC-04 对应行和段落，提交前逐块核对 diff。
- 回滚点：无持久化变化；拦截器改动可单独回滚，但会恢复漏洞。
