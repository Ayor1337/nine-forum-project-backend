# 认证与权限说明

本文档说明当前用户端和管理端的认证、权限和安全过滤行为。具体实现以两个 Web 模块的 `SecurityConfiguration`、JWT 过滤器和相关 Service 为准。

## 用户端安全链路

用户端 `web-app` 使用 Spring Security，核心特征：

- 登录路径：`/api/auth/login`。
- 登出路径：`/api/auth/logout`。
- 会话策略：`SessionCreationPolicy.STATELESS`。
- JWT 过滤器：`JWTAuthorizeFilter` 在用户名密码过滤器之前执行。
- 静音行为过滤器：`MuteActionFilter` 在 JWT 过滤器之后执行。
- 关闭 CSRF，启用 CORS 默认配置。
- 启用方法级安全：`@EnableMethodSecurity`。

登录成功后返回 `AuthorizeVO`，包含账号信息、token 和过期时间。登出时会尝试失效当前 JWT，并撤销当前登录会话。

## 用户端公开接口

用户端明确放行的认证接口包括：

- `/api/auth/register-verifications`
- `/api/auth/registrations`
- `/api/auth/login`
- `/api/passkeys/authentication/options`
- `/api/passkeys/authentications`

部分 GET 接口公开可访问，例如用户公开资料、主题话题、帖子详情、帖子回复、点赞/收藏数量、公开搜索、聊天室历史、面包屑和当前生效页面广播。

文档页面和 WebSocket 端点相关页面也被放行：

- `/chat`
- `/chatboard`
- `/system`
- `/doc.html`
- `/swagger-ui.html`
- `/swagger-ui/**`
- `/v3/api-docs`
- `/v3/api-docs/**`
- `/webjars/**`

除上述公开接口外，其他请求默认需要认证。

## 管理端安全链路

管理端 `web-admin` 也配置了 Spring Security、JWT 过滤器、登录、登出、异常处理和无状态会话。但当前 `SecurityConfiguration` 中 `authorizeHttpRequests` 对所有请求执行 `permitAll()`。

这意味着当前管理端 HTTP 接口在过滤链授权阶段未强制认证。后续上线或联调前应明确管理端访问控制策略，并补充对应测试。

## JWT 行为

- 客户端通过 `Authorization` Header 携带 JWT。
- JWT 过期和非法 token 对应业务码 `601`、`602`。
- 未认证通常返回业务码 `401`。
- 用户端登出会同时调用 JWT 失效逻辑和登录会话撤销逻辑。
- 管理端登出仅执行 JWT 失效逻辑。

调用方应以响应体中的业务 `code` 判断认证状态，因为部分异常响应 HTTP 状态码可能仍为 `200`。

## Passkey / WebAuthn

用户端支持 Passkey：

- 注册参数生成：`/api/passkeys/registration/options`
- 完成注册：`/api/passkeys/registrations`
- 凭据列表：`/api/passkeys`
- 删除凭据：`/api/passkeys/{credential_id}`
- 登录参数生成：`/api/passkeys/authentication/options`
- 完成登录：`/api/passkeys/authentications`

Passkey 依赖 `spring.security.webauthn` 配置中的 `rp-id`、`rp-name`、`allowed-origins` 和 challenge 过期时间。生产环境必须使用真实域名配置。

## 权限与治理能力

- 用户端存在 `controller/permission` 下的权限类接口，用于主题、话题、标签、帖子等管理动作。
- 管理端提供账号、角色、权限、权限操作日志接口。
- 权限变更应记录操作日志，相关模型和接口位于 `permission_operation_log` 领域。
- 用户违规、封禁、广播、举报处理等治理行为会触发消息或通知链路，详见 `messaging.md`。

## 维护要求

- 修改公开接口列表时，同步更新本文档和对应安全测试。
- 管理端从 `permitAll()` 改为认证/授权后，需要补充未认证、无权限、角色权限和登录态测试。
- 修改 JWT Claim、Header、过期策略、黑名单策略或登录会话策略后，需要同步更新接口文档、配置文档和测试。
