# 实施与校验计划

## 实施顺序

1. 检查当前工作区，确保不覆盖既有安全审计报告改动。
2. 在根 `pom.xml` 统一覆盖 `spring-framework.version` 至 6.2.19。
3. 将 WebSocket Origin 改为来自显式配置列表，并为本地/部署环境补齐配置。
4. 重构 `StompAuthInterceptor`：实现 CONNECT 的 JWT 失败处理、失败关闭的端点校验、严格订阅规则和 SEND 默认拒绝规则。
5. 保持 `/app/conversations/{conversationId}/typing` 的认证与会话成员校验；不修改服务端 `SimpMessagingTemplate` 发布代码。
6. 扩展单元测试与配置测试，覆盖访客、有效 JWT、无效 JWT、broker 写入拒绝、typing 授权和 Origin 配置。
7. 运行完整受影响模块测试和依赖树校验；必要时在本地以 STOMP 客户端进行连接/订阅/发送的负向验证。

## 必测场景

- 访客 CONNECT 后可订阅公开广播和验证结果，但向 `/broadcast`、`/verify`、`/notif`、`/transfer`、`/user` 的 SEND 都失败。
- 已认证用户也不能向上述 broker 前缀 SEND。
- 有效会话成员可发送 typing；访客、非成员、未知 `/app/**` 均失败。
- 已认证用户现有 `/user/notif/**`、`/user/transfer/**` 订阅仍可用；非成员私聊订阅失败。
- 服务端调用 `SimpMessagingTemplate` 的论坛、聊天室、通知、验证和私聊推送测试保持通过。
- WebSocket 配置的允许来源不含通配符。

## 验证命令

```powershell
.\mvnw.cmd -pl web/web-app -am test
.\mvnw.cmd -pl web/web-app dependency:tree -Dincludes=org.springframework
git diff --check
git status --short
```

## 评审门禁

- 检查所有 destination 判断是否失败关闭、是否仍使用宽松的 `contains()`。
- 检查不存在客户端可达的 broker 前缀 SEND 白名单。
- 确认 Spring 依赖树中 `spring-websocket` 与 `spring-messaging` 均为 6.2.19。
- 确认仅本任务文件、业务代码和必要测试发生变更；保留用户已有的审计报告改动。
