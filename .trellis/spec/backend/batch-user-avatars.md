# 批量用户头像查询契约

## 1. Scope / Trigger

适用于用户端需要一次解析多位用户头像的公开读取场景。目标是提供 ID 到头像 URL 的轻量映射，避免客户端对每位用户调用单用户头像与装饰接口。

不适用于头像上传、头像框或徽章渲染、用户资料隐私规则、账户状态重定义或数据库结构变更。

## 2. Signatures

- HTTP：`GET /api/users/avatars?user_ids=7,18`
- Controller：`UserController#getUserAvatars(List<Integer> userIds)`
- Service：`AccountService#getUserAvatars(List<Integer> accountIds)`
- 持久化复用：`AccountMapper#getAccountsByIds(List<Integer> accountIds)`

## 3. Contracts

- `user_ids` 为必填查询参数，Spring 支持逗号分隔（`user_ids=7,18`）或重复参数（`user_ids=7&user_ids=18`）。
- 单次请求最多 100 个 ID；重复 ID 以首次出现的位置为准去重。
- 成功数据为 `Result<List<UserAvatarItemVO>>`，每项仅含 `accountId` 与 `avatarUrl`。
- 返回项按去重后的输入顺序排列；不存在的账户不生成占位项。合法但没有匹配账户时，返回成功的空列表。
- 批量接口不得改动 `GET /api/users/{user_id}/avatar` 的 `UserAvatarVO`：后者仍负责单用户头像与装饰信息。

## 4. Validation & Error Matrix

| 条件 | 结果 |
| --- | --- |
| 缺少 `user_ids` 或值无法转换为整数 | 现有 `ValidateController` 返回 code `203` |
| 空参数、空列表、列表中含空元素、超过 100 个 ID | Controller 返回 `Result.fail(203, "请求参数内容有误")` |
| 合法 ID 全部不存在 | `Result.ok([])` |
| 部分 ID 不存在 | 成功返回存在账户的映射项 |

## 5. Good / Base / Bad Cases

- Good：`user_ids=18,7,18,99`，数据库存在 18 与 7，返回顺序为 18、7，且只执行一次批量查询。
- Base：`user_ids=99`，数据库无匹配记录，返回成功空列表。
- Bad：`user_ids=`、`user_ids=1,abc` 或 101 个 ID，返回 code `203`，不得查询账户表。

## 6. Tests Required

- Controller MockMvc：断言逗号列表与重复参数均绑定为 `List<Integer>`；`user_ids=` 返回 code `203`。
- Controller 单元测试：断言空列表和超过 100 个 ID 不调用 Service。
- Service 单元测试：Mock `getAccountsByIds`，断言只调用一次、首次顺序去重、缺失 ID 被省略，空输入不查询。
- 回归：运行 `./mvnw.cmd -pl web/web-app -am test`。

## 7. Wrong vs Correct

### Wrong

逐个调用 `getUserAvatar(accountId)`，或在批量接口复用 `UserAvatarVO`：前者造成 N+1 查询，后者既没有账户 ID，又会暴露不需要的装饰字段。

### Correct

一次调用 `getAccountsByIds`，在 `AccountService` 将账户投影为 `UserAvatarItemVO` 并按照去重后的请求 ID 重排。Controller 只负责参数边界和 `Result<T>` 响应。
