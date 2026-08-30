# SEC-01 技术设计

## 安全边界

管理端 HTTP 以 `ROLE_OWNER` 为唯一入口角色。安全链先处理 JWT 恢复，再按路由执行授权：

```text
POST /api/auth/login -> 匿名可达 -> 账号密码认证 -> 角色必须为 OWNER -> 签发 JWT
其他管理端 HTTP -> JWT 恢复当前数据库角色 -> 必须具备 ROLE_OWNER -> Controller -> Service
```

只改为 `authenticated()` 无法满足要求，因为普通用户可以从 `web-app` 获取共享 JWT。默认规则必须直接要求 `ROLE_OWNER`。

## HTTP 合同

| 场景 | HTTP 状态 | envelope code | message |
| --- | ---: | ---: | --- |
| 未认证访问受保护路由 | 401 | 401 | 未认证语义，不暴露内部异常 |
| 已认证但非 `OWNER` | 403 | 403 | 包含“权限不足” |
| 非 `OWNER` 登录管理端 | 403 | 403 | 包含“权限不足” |
| 用户名或密码错误 | 401 | 401 | 统一认证失败信息 |

认证与授权失败属于 HTTP 传输边界，使用真实 401/403；响应体继续沿用 `Result<T>`，不引入第二套 JSON 结构。

## 登录角色校验

`AccountServiceImpl#loadUserByUsername` 继续加载账号和角色。角色不是 `OWNER` 时抛出可由认证失败处理器明确识别的认证异常，失败处理器映射为 403“权限不足”；账号不存在和密码错误统一映射为 401，避免账号枚举。

允许角色使用单一常量表达，不保留只有一个元素的可变/集合抽象。

## 授权范围

- 当前版本只在管理端 HTTP FilterChain 做统一 `ROLE_OWNER` 角色门禁。
- 不读取权限表决定接口访问，不增加接口级权限表达式或 Service 方法授权。
- `AccountServiceImpl#loadUserByUsername` 只负责阻止非 `OWNER` 账号完成管理端登录。

## 测试设计

- 新增 `spring-security-test` 测试依赖。
- 使用 Spring MVC 测试上下文加载真实 `SecurityFilterChain`，对实际管理 Controller 注入 mock Service；验证请求是否在进入 Controller 前被 401/403 拦截。
- 用 `@WithMockUser` 分别构造普通用户和 `OWNER`，验证角色合同。
- 登录测试通过表单登录过滤器和可控的 `UserDetailsService`/账号数据覆盖非管理员拒绝与管理员成功。
- 单测账号加载的角色判断，确保非 `OWNER` 无法完成管理端登录。

## 兼容性与回滚

- 管理端前端需按 HTTP 401/403 读取既有 envelope；响应字段不变。
- `/v3/api-docs` 不再匿名公开，这是预期收紧。
- 如出现误拦截，可单独回滚安全链和登录异常映射；不涉及数据库或持久化数据回滚。
