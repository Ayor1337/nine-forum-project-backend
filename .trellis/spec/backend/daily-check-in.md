# 每日签到接口契约

## Scenario: 固定时区的每日 Credit 签到

### 1. Scope / Trigger

- 触发条件：新增用户端写接口、Credit 流水类型和数据库表，跨越 HTTP、业务服务与 MySQL 模式。
- 目标：用户每天仅能领取一次固定 Credit，且部署服务器与数据库时区改变时不改变业务日期。

### 2. Signatures

- HTTP：`POST /api/credits/check-ins`，无请求体，当前用户由 `SecurityUtils#getSecurityUserId()` 取得。
- 服务：`CreditService#checkIn(Integer accountId)`；成功返回 `null`，业务失败返回消息。
- 模式：`daily_check_in(check_in_id, account_id, check_in_date, create_time)`，必须有唯一键 `uk_daily_check_in_account_date(account_id, check_in_date)`。
- 流水：`credit_transaction.change_type = daily_check_in`，共享枚举常量为 `CreditChangeType.DAILY_CHECK_IN`。

### 3. Contracts

- 业务日期通过 `LocalDate.now(ZoneId.of("Asia/Tokyo"))` 计算后写入 `check_in_date`；不得依赖 JVM 默认时区或 `CURRENT_DATE`。
- 签到记录插入、Credit 账户初始化与行锁、余额增加、流水插入必须处于同一 `@Transactional` 事务中。
- 成功时使用 `Result.messageHandler` 生成统一成功响应；首次签到增加 5 Credit，流水备注为“每日签到奖励”，`operator_id` 为当前用户。
- 结构变更必须同时更新 `.docker/image/mysql/nine_forum_schema.sql` 与对应的 `.sql/` 增量迁移。

### 4. Validation & Error Matrix

| 条件 | 服务结果 | HTTP 响应 |
| --- | --- | --- |
| 已认证用户当天首次签到 | `null` | `Result<Void>` 成功 |
| 同一账号、同一东京日期再次签到 | `"今日已签到"` | 统一业务失败，消息为“今日已签到” |
| `accountId` 为 `null` | `"参数错误"` | 统一业务失败 |
| 未认证请求 | 不进入服务 | 既有 Spring Security 未认证响应 |

### 5. Good / Base / Bad Cases

- Good：唯一签到记录插入成功后，锁定账户、加 5 并写入一条 `daily_check_in` 流水。
- Base：该账号还没有 Credit 账户时，先按现有 `initAccount` 模式创建零余额账户后再发放。
- Bad：只查询当天是否已签到再写入；两个并发请求会同时通过查询并重复发放。

### 6. Tests Required

- Service：首次签到断言日期、余额增量、余额快照、交易类型、备注和操作人。
- Service：模拟唯一键冲突，断言返回“今日已签到”，且不调用账户或流水 Mapper。
- Controller：断言当前用户 ID 传递给服务；首次成功与重复签到失败都符合 `Result.messageHandler` 的 code/message。
- Migration：人工复核完整 schema 与增量 SQL 均包含相同的表、唯一键和外键；具备 MySQL 集成环境时，补充并发插入只保留一条记录的集成测试。

### 7. Wrong vs Correct

#### Wrong

```java
if (!hasCheckedInToday(accountId)) {
    grantCredit(accountId);
}
```

这会在并发请求间产生检查与写入的竞态。

#### Correct

```java
try {
    dailyCheckInMapper.insert(checkInForTokyoToday(accountId));
} catch (DuplicateKeyException exception) {
    return "今日已签到";
}
// 同一事务中锁定账户、增加余额并写入流水
```

唯一键是最终仲裁，事务确保成功占位后的 Credit 副作用要么全部提交、要么全部回滚。
