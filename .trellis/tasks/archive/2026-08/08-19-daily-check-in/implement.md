# 每日签到获取信用点：实施清单

1. 在完整 schema 和增量 SQL 中新增 `daily_check_in` 表及账号-日期唯一约束。
2. 在 `model` 添加签到实体，并扩展 `CreditChangeType` 的签到流水类型。
3. 在 `web-app` 添加签到 Mapper；在 `CreditService` 和实现中以事务完成唯一签到、余额更新和流水写入。
4. 在 `CreditController` 暴露 `POST /api/credits/check-ins`，复用认证上下文、OpenAPI 注解和 `Result.messageHandler`。
5. 为 Service 覆盖首次签到、重复签到、空账号和每日边界；为 Controller 添加路由合同测试。
6. 运行 `./mvnw.cmd -pl web/web-app -am test`，随后按质量检查复核模式同步、并发保护、事务一致性和接口响应。

## 高风险点与回滚点

- 高风险点：遗漏完整 schema、缺少唯一约束，或在非东京时区计算日期都会导致环境漂移或重复奖励。
- 回滚点：数据库迁移仅新增独立表；先撤销接口部署即可停止新的奖励发放，已产生流水保留审计记录。
