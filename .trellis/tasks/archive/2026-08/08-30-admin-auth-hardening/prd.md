# SEC-01：管理端认证与授权加固

## Goal

修复管理端 HTTP 安全链全局匿名放行问题，使当前版本只有 `OWNER` 管理员能够登录和访问管理端 API；普通用户不能登录管理端，即使持有用户端签发的有效 JWT 也不能访问管理端，并收到明确的“权限不足”响应。

## Background

- 漏洞分类：CWE-306、CWE-862；审计置信度：确定。
- `web/web-admin/src/main/java/com/ayor/config/SecurityConfiguration.java:49-54` 对唯一安全链执行 `anyRequest().permitAll()`。
- 匿名访问 `/api/accounts`、`/api/roles` 会进入 Controller/Service；匿名访问 `/v3/api-docs` 返回完整接口文档。
- 用户端与管理端复用 JWT 结构。若管理端仅改为 `authenticated()`，普通用户仍可携带用户端 token 进入管理端。
- `web/web-admin/src/main/java/com/ayor/service/impl/AccountServiceImpl.java:40-54` 已把 `OWNER` 作为唯一后台登录角色，但当前异常会进入通用认证失败处理，尚无稳定的 403“权限不足”合同。

## Requirements

### SEC-01-R1：管理端默认拒绝

- 管理端 HTTP 安全链只能匿名放行 `POST /api/auth/login`。
- 其余路由默认要求 `ROLE_OWNER`，包括 OpenAPI/Swagger 文档、账号、角色、权限、私信、数据修复和搜索重建接口。
- 未携带有效认证信息时返回 HTTP 401，并使用现有 `Result<T>` envelope 表达未认证。
- 已认证但不具备 `ROLE_OWNER` 时返回 HTTP 403，并使用现有 `Result<T>` envelope 提示“权限不足”。

### SEC-01-R2：只有管理员可登录管理端

- 当前版本唯一管理端角色为 `OWNER`，沿用已有角色名称，不新增 `ADMIN` 平行角色。
- `OWNER` 且账号密码正确时可以登录并获得管理端 JWT。
- 其他角色即使账号密码正确也不能登录管理端，返回 HTTP 403，响应消息包含“权限不足”，且不签发 JWT。
- 用户名不存在或密码错误仍按认证失败返回 HTTP 401，不向客户端泄露账号是否存在。

### SEC-01-R3：契约测试与文档

- 使用真实 Spring Security FilterChain + MockMvc 覆盖匿名、普通用户和 `OWNER` 三类访问。
- 覆盖普通用户登录被拒绝且显示“权限不足”、`OWNER` 登录成功的合同。
- 新增管理端 HTTP 安全规范，并同步 `.trellis/spec/backend/index.md`。

## Acceptance Criteria

- [x] AC-1：匿名请求 `GET /api/accounts`、`GET /api/roles` 和 `GET /v3/api-docs` 均返回 HTTP 401，且不会进入对应 Controller/Service。
- [x] AC-2：携带有效普通用户认证访问任一管理端 API 返回 HTTP 403，envelope 消息包含“权限不足”。
- [x] AC-3：普通用户使用正确账号密码请求 `POST /api/auth/login` 返回 HTTP 403，envelope 消息包含“权限不足”，响应中没有 token。
- [x] AC-4：`OWNER` 使用正确账号密码可登录；携带 `ROLE_OWNER` 认证可访问管理端 API。
- [x] AC-5：`./mvnw.cmd -pl web/web-admin -am test` 通过。
- [x] AC-6：管理端 HTTP 安全规范记录公开端点、角色合同、401/403 响应和测试要求。

## Out of Scope

- 管理端 STOMP 权限缺失（SEC-04），另行修复。
- 用户端 `web-app` 的登录、注册和公开路由策略。
- 新增管理员角色体系、权限表校验、接口级权限表达式、Service 方法授权或数据库迁移。
- 基础设施暴露、依赖漏洞和其他安全审计发现。
