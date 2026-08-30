# SEC-04 技术设计

## 安全边界

管理端 WebSocket 继续在 STOMP 帧阶段认证，不把 HTTP 握手是否成功当作消息授权。token 证明账号身份，数据库中的当前角色决定是否允许进入管理控制台实时通道：

```text
CONNECT Authorization
  -> JWT 有效性/黑名单/会话校验
  -> 读取 token name claim
  -> RoleMapper 按 username 查询当前角色
  -> role == OWNER
  -> 建立 Principal，并把 username 写入 STOMP session attributes

SUBSCRIBE /topic/reports
  -> 已有 Principal + session username
  -> RoleMapper 再查当前角色
  -> role == OWNER
  -> 精确 destination 允许列表
```

CONNECT 与 SUBSCRIBE 都查库是刻意的：前者阻止普通用户进入，后者覆盖连接建立后角色被撤销的场景。JWT 内 authorities 不参与管理端 STOMP 的授权决策。

## 实现边界

- 改动收敛在 `web-admin` 的 `StompAuthInterceptor`、对应测试和安全规范。
- 复用现有 `RoleMapper#getRoleNameByUsername`，不新增 Mapper、Service 或权限抽象。
- 将 `JWTUtils` 与 `RoleMapper` 改为构造器注入，符合项目规范并让测试不依赖反射字段注入。
- 使用私有常量表达 `OWNER`、`/topic/reports` 和 session attribute key，避免散落字符串；新增常量前已全仓搜索消费者。
- 保持 `AccessDeniedException` 作为 STOMP 通道拒绝语义，不引入 HTTP `Result<T>` envelope。

## STOMP 合同

| 帧/条件 | 行为 |
| --- | --- |
| CONNECT 缺失、无效或失效 JWT | 拒绝，`未授权连接` |
| CONNECT token 缺少账号名、账号/角色不存在 | fail-closed，拒绝 |
| CONNECT 当前角色不是 OWNER | 拒绝，`权限不足` |
| CONNECT 当前角色是 OWNER | 建立含 `ROLE_OWNER` 的 Principal，保存 username 会话属性 |
| SUBSCRIBE 无 Principal 或无 username 会话属性 | 拒绝，`未授权订阅` |
| SUBSCRIBE 当前角色已不是 OWNER | 拒绝，`权限不足` |
| SUBSCRIBE destination 不是 `/topic/reports` | 拒绝，`无权订阅该地址` |
| SUBSCRIBE 当前 OWNER 且 destination 精确匹配 | 允许 |

错误信息只表达拒绝类别，不回显 token、账号查询细节或数据库状态。

## 测试设计

1. `StompAuthInterceptorTest` 直接覆盖帧级分支，并验证普通用户/OWNER 的数据库角色查询、Principal authorities、session username 和重新授权行为。
2. `AdminStompSecurityContractTest` 启动最小 Spring Messaging 配置，向实际 `clientInboundChannel` 发送 STOMP CONNECT/SUBSCRIBE 帧，确认 `WebsocketConfiguration` 注册的拦截器生效；外部依赖使用 Spring 测试 mock。
3. `WebsocketConfigurationTest`（如契约测试不能直接证明注册细节）补充 inbound interceptor 注册断言。
4. Maven 模块测试验证回归；dependency tree 验证两个 Spring Messaging 组件使用同一修复版本。

## 依赖与兼容性

- Spring Framework `6.2.19` 是 CVE-2026-41838 的 6.2.x OSS 修复版本，仓库已满足该门槛。本任务不写入无变化的版本号。
- 不升级 Spring Framework 7，因为当前 Spring Boot `3.5.5` 基线属于 Framework 6.2 代际，跨代升级不属于本漏洞的最小修复。
- 管理端前端继续使用相同 endpoint、Authorization 原生头和 `/topic/reports`，OWNER 正常路径不变；普通用户连接被关闭是预期行为。

## 回滚

不涉及数据库或消息结构迁移。代码可按拦截器、测试/规范两组回滚；回滚拦截器会重新暴露普通用户订阅风险，因此只用于紧急定位，不应作为长期兼容方案。

