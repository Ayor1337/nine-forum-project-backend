# 图片上传解压炸弹防护设计

## 1. 边界与责任

### model

- 新增唯一的图片上传限制常量契约，供 `Base64Upload` 注解、`common` 处理器和请求过滤器共同引用，避免数值漂移。
- `Base64Upload` 负责声明 Base64 文本和文件名的结构校验；嵌套 DTO 通过 `@Valid` 级联。

### common

- `RequestBodySizeLimitFilter` 位于 Servlet 读取边界，在 Jackson 反序列化前限制请求体字节数。
- `ImageProcessingLimiter` 独占并发槽管理，使用公平 `Semaphore` 和非阻塞获取；`finally` 保证释放。
- `ImageProcessor` 独占 Base64 预检、真实格式识别、元数据检查和完整解码顺序。
- `ImageUploadExceptionHandler` 统一把图片校验拒绝、请求过大和处理繁忙映射到 `Result<T>` 客户端响应。
- `MinioService` 只接收已经处理好的图片字节；读取已有对象时也使用有界读取，移除直接 Base64 上传旁路。

### web-app / web-admin

- Controller 与 DTO 补齐 `@Valid`，但不复制图片安全规则。
- 管理端话题封面的 data URL 改走 `ImageStorageService`；已有普通 URL 仍直接保留。

## 2. 请求数据流

```text
HTTP body
  -> RequestBodySizeLimitFilter（Content-Length 预拒绝 + 流式计数）
  -> Jackson / Jakarta Validation（Base64 文本长度与嵌套模型）
  -> 业务 Service
  -> ImageStorageService
  -> ImageProcessingLimiter（最多 2 个并发槽，槽满快速失败）
  -> ImageProcessor
       1. Base64 编码长度预检
       2. 解码为最多 10 MiB + 1 byte
       3. ImageIO 识别真实格式
       4. 读取各帧元数据，检查边长、单帧像素与帧数
       5. 仅在全部门禁通过后 read(0)
       6. 贴纸缩放/转 WebP，或正文原字节透传
  -> MinIO
```

任何拒绝都发生在对象存储和数据库副作用之前。

## 3. 合同细节

### 3.1 Base64 预检

- 从 data URL 中只切出 payload，不使用 `split` 复制多份字符串。
- 在调用 `Base64.Decoder#decode` 前按 payload 字符数计算最大可能解码长度；超过 10 MiB 直接拒绝。
- 解码后再次检查真实字节数，防止空白、padding 或计算误差绕过。
- `Base64Upload` 的注解上限包含最长受支持 data URL 头部；请求过滤器仍是防止 Jackson 先分配巨大字符串的第一道门禁。

### 3.2 元数据与真实格式

- 由 `ImageIO.getImageReaders(input)` 选择能识别真实字节的 reader，不信任文件名或 data URL 声明。
- `jpg` 与 `jpeg` 视为同一别名；其余扩展名必须与 reader 识别格式一致。
- `reader.read(0)` 前调用 `getWidth/getHeight`。像素计算使用 `long`，并拒绝零值、负值、超边长或超总像素。
- 先调用 `getNumImages(false)` 获取 reader 已知帧数；未知时只逐帧探测到第 51 帧便拒绝，不做无界全文件帧数扫描。探测每一帧时同时执行尺寸与像素检查。
- 贴纸只允许一帧；正文最多 50 帧。完整解码仍仅发生在首帧，因为正文 GIF 保留原字节。

### 3.3 请求体限制

- 过滤 POST、PUT、PATCH；其他方法跳过。
- `Content-Length > 16 MiB` 时不读取正文，立即返回 413。
- 长度未知、伪造或分块传输时，用 `HttpServletRequestWrapper` 包装 `ServletInputStream`，累计实际读取字节，在第 16 MiB + 1 byte 抛出内部超限异常。
- 过滤器捕获直接异常或 Jackson 包装后的 cause，清空尚未提交的缓冲区并返回 `Result.fail(413, "请求体过大")`。

### 3.4 并发限制

- `ImageProcessingLimiter` 是单例 Spring Bean，每个 Web 应用 JVM 各持有两个公平信号量许可。
- 许可覆盖元数据解析、完整解码、缩放和转码；MinIO 网络上传不占许可，避免慢存储长期占槽。
- 无许可时抛出专用繁忙异常，由共享 Advice 返回 HTTP 429 和可重试消息。

## 4. 兼容性

- 保留现有 10 MiB 单图字节限制、格式白名单、贴纸 WebP 输出、正文 GIF 原样保存和对象路径。
- 主题仍最多 7 张；多张图片 Base64 与其他 JSON 字段合计必须不超过 16 MiB。
- 48 MP 或其他超过 16 MP 的图片、超过 50 帧的正文 GIF 将开始被拒绝，这是已确认的安全性变更。
- 旧平台 URL 不重新解码；仅新的 data URL / Base64 进入处理器。

## 5. 错误合同

| 条件 | HTTP | Result code | 消息语义 |
| --- | ---: | ---: | --- |
| 请求体超过 16 MiB | 413 | 413 | 请求体过大 |
| 图片处理槽已满 | 429 | 429 | 图片处理繁忙，请稍后重试 |
| Base64、格式、尺寸、像素或帧数不合法 | 400 | 203 | 具体、可理解的图片校验错误 |

## 6. 风险与回滚

- 不同 ImageIO reader 对未知帧数的越界异常可能不同；帧探测必须把“正常结束”和“损坏输入”区分，并以 PNG/JPEG/GIF/WebP 回归测试锁定。
- 全局 16 MiB 请求限制可能暴露未记录的大请求用例；回滚点是仅撤销过滤器注册，图片解码门禁仍可独立保留。
- 若上线后 16 MP 兼容性过严，应通过新的评审调整唯一常量，不得在调用点局部放宽。
- 反向代理仍应配置不高于 16 MiB；应用层防护不依赖该部署项。
