# 错误与响应规范

## HTTP 响应

用户端 Controller 的统一响应为 `common/src/main/java/com/ayor/result/Result.java`：`code`、`message`、`data` 三字段。优先复用已有工厂方法：

- 有数据的成功响应：`Result.ok(data)`。
- Service 用 `null` 表示业务失败时：`Result.dataMessageHandler(...)`。
- Service 返回错误消息或 `null` 表示成功时：`Result.messageHandler(...)`。

`web/web-app/.../ThreadController.java` 展示了三种用法。不要在新接口中另造 JSON 响应形状，也不要绕过 `Result<T>` 直接返回 Map。

## 校验与访问控制

- 请求体使用 Jakarta Validation 的 `@Valid` 或 `@Validated`，例如 `ThreadController#postThread` 与 `addPost`。
- 参数校验、缺参和类型不匹配由 `controller/exception/ValidateController.java` 转为 code `203`。
- `AccessController.java` 将未认证与无权限分别转为 `401`、`403`；认证上下文通过 `SecurityUtils` 获取。
- Service、工具层目前会以返回错误消息、`IllegalArgumentException` 或 `IllegalStateException` 表达失败。新增代码应匹配调用链的既有模式，而非假设项目已有统一业务异常基类。

## 注意事项

- 不要在 Controller 捕获所有 `Exception` 并吞掉异常；已有 `@RestControllerAdvice` 只覆盖特定边界错误。
- 面向客户端的信息应可理解且不泄露凭据、堆栈或内部基础设施细节。
- 增加新的异常转换规则时，同时补充 `web/web-app/src/test/java/com/ayor/controller/exception/ValidateControllerTest.java` 风格的测试，确认状态码与响应体约定。
