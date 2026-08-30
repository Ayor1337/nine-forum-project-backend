# 每日签到获取信用点：技术设计

## 范围与接口

- 在用户端既有 `CreditController` 新增 `POST /api/credits/check-ins`，从认证上下文取得当前账号 ID，不接收请求体。
- 成功返回既有 `Result<Void>` 成功响应；东京自然日内的重复调用返回统一业务失败消息“今日已签到”。
- 在既有 `CreditService` 增加 `checkIn(Integer accountId)`，避免为单一 Credit 发放动作新增跨层服务。

## 数据模型与并发控制

新增 `daily_check_in` 表，记录 `check_in_id`、`account_id`、`check_in_date` 与 `create_time`。`(account_id, check_in_date)` 唯一约束是每日奖励的最终并发保护；外键关联 `account`。

服务通过 `LocalDate.now(ZoneId.of("Asia/Tokyo"))` 计算业务日期，显式写入 `check_in_date`，因此不依赖 JVM 或 MySQL 的时区。事务内先尝试插入签到记录：唯一键冲突即返回“今日已签到”；插入成功后初始化并锁定 Credit 账户、增加 5 Credit，并插入一条 `daily_check_in` 类型的 `credit_transaction` 流水。任一步异常均回滚签到记录、余额和流水。

## 共享模型与兼容性

- 在 `model` 新增 `DailyCheckIn` POJO，并给共享的 `CreditChangeType` 增加 `DAILY_CHECK_IN("daily_check_in")`。
- `operator_id` 沿用商城用户主动操作的语义，写入当前用户 ID；备注固定为“每日签到奖励”。
- 现有余额、流水查询和管理员流水枚举解析可自然识别新增类型；不改变其响应结构。

## 模式同步与回滚

- 更新 `.docker/image/mysql/nine_forum_schema.sql`，供新环境初始化。
- 新增 `.sql/20260819_daily_check_in.sql`，供已有环境创建表。
- 回滚时先下线接口，再删除新表；只要生产环境已有签到流水，不应回滚 `CreditChangeType` 的新增常量或删除已产生的流水。

## 风险与缓解

- 仅靠服务层的“先查后写”在并发下会重复发放；由数据库唯一键兜底。
- MySQL 时间与应用时间可能不一致；业务日期由固定的东京时区在应用侧计算并存储为 `DATE`。
