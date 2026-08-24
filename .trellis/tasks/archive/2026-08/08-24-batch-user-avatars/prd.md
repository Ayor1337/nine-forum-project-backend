# 批量查询用户头像接口

## Goal

提供一个轻量、只读的用户端接口，使调用方能够用一组用户 ID 一次取得对应的头像 URL，避免针对每个用户逐一请求现有单用户头像接口。

## Confirmed Facts

- 现有公开接口 `GET /api/users/{user_id}/avatar` 返回 `UserAvatarVO`，其中还包含已装备的头像框与徽章；该对象不含用户 ID，不能直接满足批量映射需求。
- 头像存储在 `account.avatar_url`，账户主键为 `account.account_id`。
- `AccountMapper#getAccountsByIds(List<Integer>)` 已提供按 ID 批量读取账户的能力，但返回完整账户实体；新接口仅需要 `account_id` 和 `avatar_url` 投影。
- 用户端 Controller 统一通过 `Result<T>` 返回数据，公开用户头像接口当前不读取登录态。

## Requirements

- 新增批量查询公开用户头像的用户端只读接口。
- 接口为 `GET /api/users/avatars`，使用必填查询参数 `user_ids` 接收用户 ID 列表（支持逗号分隔或重复参数）。
- 单次最多接受 100 个用户 ID；缺少或无法转换为整数的参数沿用现有参数校验响应，空列表或超出上限由 Controller 返回标准参数错误（code `203`）。
- 输出条目仅包含 `accountId` 与 `avatarUrl`；成功但没有任何匹配用户时返回空列表。
- 重复 ID 按首次出现去重，结果按去重后的请求顺序排列。
- 不存在的 ID 直接省略；账户状态与软删除字段的可见性遵循现有 `GET /api/users/{user_id}/avatar`（该接口按主键直接读取账户）行为。
- 保持现有单用户头像及装饰接口的路由、字段与行为不变。
- 不修改数据库结构，也不返回头像框、徽章、账户资料或其他账户字段。

## Acceptance Criteria

- [ ] 调用方能通过 `GET /api/users/avatars?user_ids=7,18` 一次获得用户 ID 到头像 URL 的映射列表。
- [ ] 响应继续使用项目标准 `Result<T>` 包装，列表项只序列化用户 ID 和头像 URL。
- [ ] 不存在的用户 ID 被省略，重复 ID 不产生重复条目，且返回顺序与去重后的请求顺序一致。
- [ ] 空、非法或超过 100 个 ID 的请求返回标准参数错误（code `203`）；无匹配用户的合法请求成功返回空列表。
- [ ] 新增的查询避免逐 ID 查询（N+1），且不影响现有 `GET /api/users/{user_id}/avatar`。
- [ ] 受影响的 `web-app` 测试通过。

## Out of Scope

- 头像上传、默认头像生成、头像框和徽章查询。
- 用户资料隐私设置或账户状态规则的重构。
- 数据库 schema、迁移和缓存策略变更。

## Key Decisions

- 已确认：不存在的用户 ID 省略，不返回 `avatarUrl: null` 占位项。
- 复用现有批量账户查询，在 Service 中投影为应用专属的轻量 VO；不改变现有 `UserAvatarVO`，避免单用户接口新增 `accountId` 或泄露装饰字段到批量接口。
- 现有全局参数异常处理未覆盖 `@Size` 的方法参数校验；数量限制在 Controller 中显式处理，避免为单一接口扩大全局异常处理范围。
- 本任务为轻量改动，仅维护 PRD；无需独立 `design.md` 或 `implement.md`。

