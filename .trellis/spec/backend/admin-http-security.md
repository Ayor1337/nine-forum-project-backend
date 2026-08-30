# 管理端 HTTP 安全

## 1. Scope / Trigger

适用于 `web/web-admin` 的 HTTP FilterChain、表单登录、JWT 恢复和登出。当前版本采用单一全局角色门禁：只有 `ROLE_OWNER` 可以进入管理端；不读取权限表，不增加接口级权限表达式或 Service 方法授权。

管理端与用户端共享 JWT，因此只要求 `authenticated()` 会让普通用户 token 进入管理端，必须直接校验 `ROLE_OWNER`。

## 2. Signatures

- 匿名入口：`POST /api/auth/login`，表单字段为 `username`、`password`。
- 登出入口：`POST /api/auth/logout`，JWT 从 `Authorization: Bearer <token>` 或现有同名 Cookie 恢复。
- 其他管理端 HTTP 路由：包括 `/api/**`、`/v3/api-docs` 和 Swagger UI，全部要求 `ROLE_OWNER`。
- 成功和失败响应继续使用 `Result<T>`：`code`、`message`、`data`。

## 3. Contracts

- OWNER 账号和正确密码登录成功并获得 JWT。
- 非 OWNER 账号即使密码正确也不能登录，不签发 token。
- JWT 过滤器从数据库读取当前角色并写入 `ROLE_<roleName>`；FilterChain 只接受 `ROLE_OWNER`。
- JWT 过滤器必须位于 `LogoutFilter` 之前，否则登出处理器无法读取 JWT 建立的认证上下文。
- 认证、授权失败同时设置真实 HTTP 401/403 和 `Result<T>` envelope，不能以 HTTP 200 伪装失败。

## 4. Validation & Error Matrix

| 条件 | HTTP 状态 | `Result.code` | `message` |
| --- | ---: | ---: | --- |
| 未认证访问受保护路由 | 401 | 401 | `未认证` |
| JWT 存在但无法解析 | 401 | 601 | `token过期` |
| 已认证但不是 OWNER | 403 | 403 | 包含“权限不足” |
| 非 OWNER 登录 | 403 | 403 | `权限不足` |
| 用户名不存在或密码错误 | 401 | 401 | `用户名或密码错误` |
| OWNER 登录成功 | 200 | 200 | `成功`，`data.token` 非空 |

登录失败不得把“用户不存在”透传给客户端，避免账号枚举。

## 5. Good / Base / Bad Cases

- Good：OWNER 使用正确密码登录，随后携带 JWT 访问 `/api/accounts`，得到 200。
- Base：匿名访问 `/api/accounts` 或 `/v3/api-docs`，在进入 Controller/Service 前得到 401。
- Bad：普通用户从 `web-app` 获得有效 JWT 后调用管理端 API，必须得到 403；不能因为 token 有效而放行。
- Bad：普通用户使用正确密码登录管理端，必须得到 403 且响应中不能出现 token。
- Bad：匿名或普通用户调用登出，分别得到 401/403；OWNER 可正常使 JWT 失效。

## 6. Tests Required

管理端安全改动使用真实 `SecurityFilterChain` + MockMvc，并至少断言：

- 匿名访问管理 API、角色 API、OpenAPI 文档为 HTTP 401，且管理 Service 没有调用；
- `ROLE_USER` 访问管理 API 为 HTTP 403，消息包含“权限不足”；
- 非 OWNER 正确密码登录为 403，响应不包含 token；错误密码为 401；
- OWNER 登录返回 token，`ROLE_OWNER` 可访问管理 API；
- 匿名、普通用户、OWNER 三类登出合同分别为 401、403、成功；
- 无法解析的 JWT 同时断言 HTTP 401 和既有 token envelope code。

验证命令：

```powershell
.\mvnw.cmd -pl web/web-admin -am test
```

## 7. Wrong vs Correct

Wrong：只判断是否登录，普通用户共享 JWT 会绕过管理端边界。

```java
auth.anyRequest().authenticated();
```

Correct：只匿名放行登录 POST，其余请求统一要求 OWNER，并让 JWT 在 LogoutFilter 前恢复认证。

```java
auth.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll();
auth.anyRequest().hasAuthority("ROLE_OWNER");
http.addFilterBefore(jwtAuthorizeFilter, LogoutFilter.class);
```
