# SEC-01 Implementation Plan

## Task List

- [x] 1. 在 `web-admin` 增加 Spring Security 测试依赖和真实 FilterChain/MockMvc 回归测试，先复现匿名放行、普通用户 token 可达及登录角色错误合同。
- [x] 2. 收紧 `SecurityConfiguration`：仅匿名放行 `POST /api/auth/login`，其余请求默认要求 `ROLE_OWNER`，统一输出 HTTP 401/403 + `Result<T>` envelope。
- [x] 3. 调整后台账号加载与认证失败映射：只有 `OWNER` 可登录，其他角色稳定返回 403“权限不足”，错误凭据保持统一 401。
- [x] 4. 补齐/调整测试，覆盖匿名、普通用户、`OWNER` 和错误凭据，不引入权限表或细粒度方法授权。
- [x] 5. 新增 `.trellis/spec/backend/admin-http-security.md` 并更新规范索引。
- [x] 6. 运行 `./mvnw.cmd -pl web/web-admin -am test`，检查 diff，执行 `trellis-check` 全量质量门禁。

## Validation

```powershell
.\mvnw.cmd -pl web/web-admin -am test
```

重点断言：

- 匿名访问管理 API 和 OpenAPI 文档为 HTTP 401。
- 普通用户认证访问管理 API、普通用户登录管理端为 HTTP 403 且提示“权限不足”。
- `OWNER` 登录和访问成功。
- 受保护请求未进入 Controller/Service。

## Risk / Rollback Points

- 风险：管理端前端若只处理 HTTP 200，可能需要同步适配 401/403；本任务保持 envelope 字段不变以降低影响。
- 风险：共享 JWT 使“只限制管理端登录”不足以封堵普通用户；验收必须包含用户端普通 JWT 等价身份的 403 合同。
- 延后：权限表、接口级权限表达式和 Service 方法授权均不在当前版本实现。
- 回滚点：实现不改数据库，可按安全链、登录映射两个原子改动分别回滚。
