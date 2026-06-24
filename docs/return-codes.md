# 返回码说明

本文档整理当前代码中 HTTP 接口响应体 `Result.code` 可能出现的业务返回码。

统一响应结构定义在 `common/src/main/java/com/ayor/result/Result.java`：

```json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
```

注意：这里的 `code` 是响应体业务码，不完全等同于 HTTP 状态码。部分认证、授权、JWT 过期场景会把 HTTP 状态码设置为 `200`，但响应体 `code` 表示真实业务状态。

## 生产代码中实际可能返回的业务码

| code | 含义 | 主要来源 |
| --- | --- | --- |
| `200` | 成功；退出成功 | `Result.ok(...)`、`ResultCodeEnum.SUCCESS`、`ResultCodeEnum.LOGOUT_SUCCESS` |
| `201` | 通用失败 | `Result.fail()`、`Result.messageHandler(...)` 返回非空消息、`Result.dataMessageHandler(...)` 返回空数据 |
| `202` | 参数不正确 | `ResultCodeEnum.PARAM_ERROR`，当前用于管理端回帖列表参数缺失 |
| `203` | 服务异常；当前也用于参数校验异常响应 | `ResultCodeEnum.SERVICE_ERROR`、`ValidateController` |
| `400` | 退出失败；参数校验失败 | `ResultCodeEnum.LOGOUT_FAILURE`、`ResultCodeEnum.VALIDATE_FAILED` |
| `401` | 未认证；登录失败；账号被封禁 | `SecurityConfiguration`、`AccessController`、`JWTAuthorizeFilter`、`ResultCodeEnum.UNAUTHENTICATED_ERROR` |
| `403` | 权限不足；账号被禁言 | `SecurityConfiguration`、`AccessController`、`MuteActionFilter` |
| `500` | 表情包上传参数异常 | `StickerController.upload(...)` 捕获 `IllegalArgumentException` |
| `601` | token 过期 | `JWTAuthorizeFilter`、`ResultCodeEnum.TOKEN_EXPIRED` |

## ResultCodeEnum 已定义返回码

以下返回码来自 `common/src/main/java/com/ayor/result/ResultCodeEnum.java`。其中部分枚举目前未在生产代码中直接引用，但属于已定义的统一返回码。

| 枚举 | code | message | 备注 |
| --- | --- | --- | --- |
| `SUCCESS` | `200` | 成功 | `Result.ok(...)` 默认成功码 |
| `FAIL` | `201` | 失败 | `Result.fail()`、`messageHandler`、`dataMessageHandler` 默认失败码 |
| `PARAM_ERROR` | `202` | 参数不正确 | 管理端 `PostController` 有直接使用 |
| `SERVICE_ERROR` | `203` | 服务异常 | `ValidateController` 直接写入同码但消息不同 |
| `DATA_ERROR` | `204` | 数据异常 | 当前未发现生产代码直接引用 |
| `ILLEGAL_REQUEST` | `205` | 非法请求 | 当前未发现生产代码直接引用 |
| `REPEAT_SUBMIT` | `206` | 重复提交 | 当前未发现生产代码直接引用 |
| `DELETE_ERROR` | `207` | 请先删除子集 | 当前未发现生产代码直接引用 |
| `ADMIN_ACCOUNT_EXIST_ERROR` | `301` | 账号已存在 | 当前未发现生产代码直接引用 |
| `ADMIN_CAPTCHA_CODE_ERROR` | `302` | 验证码错误 | 当前未发现生产代码直接引用 |
| `ADMIN_CAPTCHA_CODE_EXPIRED` | `303` | 验证码已过期 | 当前未发现生产代码直接引用 |
| `ADMIN_CAPTCHA_CODE_NOT_FOUND` | `304` | 未输入验证码 | 当前未发现生产代码直接引用 |
| `ADMIN_LOGIN_AUTH` | `305` | 未登陆 | 当前未发现生产代码直接引用 |
| `ADMIN_ACCOUNT_NOT_EXIST_ERROR` | `306` | 账号不存在 | 当前未发现生产代码直接引用 |
| `ADMIN_ACCOUNT_ERROR` | `307` | 用户名或密码错误 | 当前未发现生产代码直接引用 |
| `ADMIN_ACCOUNT_DISABLED_ERROR` | `308` | 该用户已被禁用 | 当前未发现生产代码直接引用 |
| `ADMIN_ACCESS_FORBIDDEN` | `309` | 无访问权限 | 当前未发现生产代码直接引用 |
| `APP_LOGIN_AUTH` | `501` | 未登陆 | 当前未发现生产代码直接引用 |
| `APP_LOGIN_PHONE_EMPTY` | `502` | 手机号码为空 | 当前未发现生产代码直接引用 |
| `APP_LOGIN_CODE_EMPTY` | `503` | 验证码为空 | 当前未发现生产代码直接引用 |
| `APP_SEND_SMS_TOO_OFTEN` | `504` | 验证法发送过于频繁 | 当前未发现生产代码直接引用 |
| `APP_LOGIN_CODE_EXPIRED` | `505` | 验证码已过期 | 当前未发现生产代码直接引用 |
| `APP_LOGIN_CODE_ERROR` | `506` | 验证码错误 | 当前未发现生产代码直接引用 |
| `APP_ACCOUNT_DISABLED_ERROR` | `507` | 该用户已被禁用 | 当前未发现生产代码直接引用 |
| `UNAUTHENTICATED_ERROR` | `401` | 未认证 | 生产代码多处直接写入 `401`，但较少通过该枚举引用 |
| `LOGOUT_SUCCESS` | `200` | 退出成功 | 用户端、管理端退出登录成功 |
| `LOGOUT_FAILURE` | `400` | 退出失败 | 用户端、管理端退出登录失败 |
| `TOKEN_EXPIRED` | `601` | token过期 | 用户端、管理端 JWT 解析失败 |
| `TOKEN_INVALID` | `602` | token非法 | 当前未发现生产代码直接引用 |
| `VALIDATE_FAILED` | `400` | 参数校验失败 | 当前未发现生产代码直接引用 |
| `DATA_NOT_FOUND` | `402` | 数据不存在 | 当前未发现生产代码直接引用 |

## 直接写入但未集中定义的返回码

| code | message | 来源 |
| --- | --- | --- |
| `401` | 用户名或密码错误 | 用户端登录失败 |
| `401` | Spring Security 认证异常消息 | 用户端、管理端未认证入口 |
| `401` | 账号已被封禁 | 用户端 JWT 过滤器 |
| `403` | 权限不足 | 用户端访问拒绝异常处理 |
| `403` | 权限不足, 请联系管理员 | 用户端、管理端访问拒绝处理 |
| `403` | 账号已被禁言 | 用户端禁言动作过滤器 |
| `500` | `IllegalArgumentException` 的异常消息 | 用户端表情包上传 |

## HTTP 状态码差异

| 场景 | HTTP 状态码 | 响应体 `code` |
| --- | --- | --- |
| JWT 过期或非法导致解析失败 | `200` | `601` |
| 未认证入口 | `200` | `401` |
| 禁言用户执行受限动作 | `403` | `403` |
| 大多数控制器正常返回 | Spring 默认 `200` | 由 `Result.code` 决定 |

## 维护建议

- 新增统一业务码时，优先维护 `ResultCodeEnum`，避免在控制器或过滤器中继续硬编码数字。
- 新增或修改 `Result.fail(code, message)` 时，同步更新本文档。
- 调用方应优先以响应体 `code` 判断业务结果，不要只依赖 HTTP 状态码。
