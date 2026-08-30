# Implementation Plan

## Task List

1. 在 `UserLoginSessionServiceImplTest` 先添加双会话全量撤销回归测试，运行单测确认修复前失败。
2. 在 `LoginSessionMapper` 增加账号有效会话查询；在 `UserLoginSessionService` 与实现中增加 `revokeAllSessions(accountId)`，复用现有单会话撤销副作用。
3. 在 `AccountServiceImplTest` 补齐改密成功、校验失败、持久化失败测试；让 `AccountServiceImpl` 在密码更新成功后调用账号级撤销。
4. 在 `UserControllerTest` 增加无效/有效密码 DTO 的 MockMvc 合同测试；给 `UserController#updatePassword` 添加 `@Valid`。
5. 更新 `security-audit-report.md` 的 SEC-08 状态、修复说明、验证命令和并发认证后置建会话残余风险，不覆盖用户现有其他审计内容。
6. 运行定向回归测试，再运行 `./mvnw.cmd -pl web/web-app -am test`。
7. 按 `trellis-check` 做规范、跨层数据流和测试完整性检查；若发现问题，修复后重跑相关验证。

## Validation Commands

```powershell
.\mvnw.cmd -pl web/web-app -am -Dtest=UserLoginSessionServiceImplTest,AccountServiceImplTest,UserControllerTest -Dsurefire.failIfNoSpecifiedTests=false test
.\mvnw.cmd -pl web/web-app -am test
```

## Risky Files and Rollback Points

- `web/web-app/src/main/java/com/ayor/service/impl/AccountServiceImpl.java`：必须保证仅密码持久化成功后撤销会话。
- `web/web-app/src/main/java/com/ayor/service/impl/UserLoginSessionServiceImpl.java`：Redis 与数据库副作用必须覆盖同一批会话。
- `web/web-app/src/main/java/com/ayor/mapper/LoginSessionMapper.java`：查询必须按账号隔离且排除已撤销/已过期记录。
- `security-audit-report.md`：工作区已有用户修改，只做 SEC-08 对应段落的最小合并。

由于无 schema 变更，各步骤可按文件级改动回滚；若全量撤销测试无法稳定覆盖真实鉴权语义，停止实现并重新评估是否需要账号级 token version。
