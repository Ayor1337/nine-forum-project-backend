# 技术设计：STOMP 入站最小权限

## 架构边界

HTTP 握手只负责建立浏览器 WebSocket 连接，保持 `permitAll`。STOMP `CONNECT` 负责将有效 JWT 转化为会话 `Principal`；入站拦截器统一完成 `CONNECT`、`SUBSCRIBE` 和 `SEND` 的认证/授权。

simple broker 前缀是服务端输出边界，而非客户端输入 API。所有向这些前缀的发布继续由 `SimpMessagingTemplate` 执行，客户端不能直接写入。

## 入站规则

| 命令 | 访客 | 已认证用户 |
| --- | --- | --- |
| `CONNECT` | 无 JWT 时建立访客会话 | 有效 JWT 设置 `Principal`；无效非空 JWT 拒绝 |
| `SUBSCRIBE /broadcast/**` | 允许 | 允许 |
| `SUBSCRIBE /verify/{jwtId}` | 允许 | 允许 |
| `SUBSCRIBE /notif/**`、`/transfer/**`、`/user/**` | 拒绝 | 按用户/会话成员关系授权 |
| `SEND /app/conversations/{id}/typing` | 拒绝 | 会话成员允许 |
| 所有 broker 前缀或其他 `SEND` | 拒绝 | 拒绝 |

目的地匹配必须使用锚定的前缀或正则表达式，而不是 `contains()`。无法识别的握手端点或会话属性缺失均拒绝，避免内部测试/异常路径绕过端点限制。

## 配置与兼容性

- 新增或复用集中式来源配置，为 WebSocket 提供按环境注入的允许 Origin 列表；生产配置必须显式声明前端 HTTPS 域名。本地开发来源也需显式列出。
- 不引入 Spring Session，也不要求浏览器在 WebSocket 握手上传递 `Authorization` HTTP 头。
- 不启用额外的 Spring Security 消息授权框架；既有 `ChannelInterceptor` 已是单一授权点，减少重复规则与排序风险。
- 根 POM 覆盖 `spring-framework.version` 为 6.2.19，使 Spring 模块保持同一补丁版本。

## 风险与回滚

- 风险：前端若曾错误地直接 `SEND` broker 目的地，修复后将收到拒绝。代码检索显示这些目的地由服务端推送；以集成测试和预发布浏览器验证确认。
- 风险：Origin 列表遗漏部署域名会造成握手失败。上线前以环境变量/配置清单复核实际域名。
- 回滚：若出现兼容性问题，仅回退本任务提交即可恢复旧入站规则；不得回退 Spring Framework 安全补丁。
