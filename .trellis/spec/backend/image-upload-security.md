# 图片上传资源安全合同

## 1. 范围与触发条件

本合同适用于所有把 Base64 图片写入对象存储，或从对象存储读取图片并交给 `ImageIO` 检查的路径。当前入口包括主题、评论、贴纸、头像、横幅、用户端话题封面、管理端话题封面和装饰资源。

新增图片上传入口、调整图片格式/体积、修改 `ImageProcessor`、直接读取 MinIO 图片或调整请求体边界时，必须执行本合同。安全目标是防止 CWE-409/CWE-400：压缩字节上限不能替代解码前的尺寸、像素、帧数与并发限制。

## 2. 签名

- 共享限制：`com.ayor.entity.ImageUploadLimits`
- 上传模型：`Base64Upload { String base64; String fileName; }`
- 贴纸处理：`ImageProcessor#processSticker(Base64Upload)`
- 正文处理：`ImageProcessor#processImage(Base64Upload)`
- 已存图片检查：`ImageProcessor#inspectStoredImage(byte[], String)`
- 图片处理并发门禁：`ImageProcessingLimiter#execute(Supplier<T>)`
- 对象存储写入：`ImageStorageService -> MinioService#uploadObject(byte[], String, String)`
- 对象存储有界读取：`MinioService#getObjectBytes(String)`
- HTTP 请求边界：`RequestBodySizeLimitFilter`
- 图片错误映射：`ImageUploadExceptionHandler`

`MinioService` 不得重新出现接收 `Base64Upload` 或直接 Base64 解码的上传方法。

## 3. 合同

### 输入与资源上限

所有数值只能定义在 `ImageUploadLimits`：

| 限制 | 合同值 |
| --- | ---: |
| 解码后单图字节 | 10 MiB |
| Base64 payload 字符 | `ceil(MAX_SOURCE_BYTES / 3) × 4` |
| data URL Base64 文本 | payload 上限 + 23 字符 |
| 文件名 | 255 字符 |
| 任一边长 | 8192 px |
| 任一帧总像素 | 16,777,216 |
| 正文动图 | 50 帧 |
| 贴纸 | 1 帧 |
| POST/PUT/PATCH 请求体 | 16 MiB |
| 每 JVM 图片处理并发 | 2 |

- `Base64Upload.base64` 与 `fileName` 必须 `@NotBlank`、`@Size`；DTO 字段及列表元素必须用 `@Valid` 级联，直接 Controller 参数也必须 `@Valid`。
- `RequestBodySizeLimitFilter` 同时检查声明的 `Content-Length` 和实际读取字节。未知长度、chunked 或伪造较小长度不能绕过 16 MiB。
- `ImageProcessor` 在调用 `Base64.Decoder#decode` 前检查编码长度，解码后再次检查真实字节数。
- 文件名/data URL 只声明期望格式；真实格式必须来自 `ImageIO.getImageReaders(input)`。除 `jpg`/`jpeg` 别名外，声明与 reader 格式不一致时拒绝。
- 对每个允许帧调用 `ImageReader#getWidth/getHeight`，以 `long` 计算 `width × height`；全部通过后才能调用唯一的 `reader.read(0)`。
- 未知帧数只能探测到上限加一帧并停止，不得用无界扫描替代帧数门禁。
- `ImageProcessingLimiter` 使用公平 `Semaphore` 非阻塞获取；许可覆盖元数据读取、首帧解码、缩放和转码，并在 `finally` 释放。MinIO 网络写入不占处理许可。
- `MinioService#getObjectBytes` 最多读取 10 MiB + 1 byte；超限后拒绝，不使用无界 `transferTo`。

### 成功与错误响应

- 成功响应和对象路径保持既有业务合同。
- 请求体超限：HTTP 413，`Result.code=413`，消息 `请求体过大`。
- 并发槽已满：HTTP 429，`Result.code=429`，消息 `图片处理繁忙，请稍后重试`。
- Base64、格式、尺寸、像素或帧数非法：HTTP 400，`Result.code=203`，返回可理解且不泄露内部实现的消息。
- Service 中兼容旧返回值的 `catch (RuntimeException)` 必须先捕获并重新抛出 `ImageUploadException`，避免安全拒绝被吞掉或降级成未知失败。

## 4. 校验与错误矩阵

| 条件 | 结果 | 必须发生在 |
| --- | --- | --- |
| `Content-Length > 16 MiB` | HTTP/code 413 | 读取请求体前 |
| 实际读取超过 16 MiB | HTTP/code 413 | Jackson/Controller 副作用前 |
| Base64 编码长度不可能落在 10 MiB 内 | HTTP 400/code 203 | Base64 解码前 |
| 解码后字节超过 10 MiB | HTTP 400/code 203 | ImageIO reader 创建前 |
| 真实格式与声明不一致 | HTTP 400/code 203 | 帧元数据/完整解码前 |
| 宽、高超过 8192 | HTTP 400/code 203 | `read(0)` 前 |
| `width × height > 16,777,216` | HTTP 400/code 203 | `read(0)` 前 |
| 贴纸超过 1 帧 | HTTP 400/code 203 | `read(0)` 前 |
| 正文超过 50 帧 | HTTP 400/code 203 | `read(0)` 前 |
| 两个处理槽均占用 | HTTP/code 429 | ImageIO 处理前，不排队 |
| 任一处理动作异常 | 释放许可 | `finally` |
| MinIO 读取超过 10 MiB | HTTP 400/code 203 | 返回完整 byte[] 前 |

## 5. Good / Base / Bad 案例

- Good：小于 10 MiB、4096×4096、单帧 PNG，通过真实格式和元数据门禁后解码；贴纸按既有规则缩到最长边 512 并转 WebP。
- Good：正文 GIF 恰好 50 帧且每帧在边长/像素限制内，保留 GIF 原字节。
- Base：请求体恰好 16 MiB、图片恰好 8192 单边或 16,777,216 像素时允许；限制判断使用严格大于。
- Bad：几十 KiB 的 PNG 头声明 4096×4097；总像素超过上限，在 `read(0)` 前拒绝，不能按声明尺寸分配栅格。
- Bad：请求头谎报较小 `Content-Length`，实际正文超过 16 MiB；流式计数仍在第 16 MiB + 1 byte 拒绝。
- Bad：文件名为 `.png`、真实字节为 GIF；格式不一致，拒绝。
- Bad：第三个并发处理请求到达时，前两个仍占槽；第三个立即 429，不进入无界等待。

## 6. 必需测试

- `Base64UploadValidationTest`：空内容、Base64 文本边界、文件名边界。
- `ImageUploadCascadeValidationTest`：主题、评论、话题与用户资料嵌套字段/列表元素确实触发 `@Valid`。
- `RequestBodySizeLimitFilterTest`：声明超限、未知长度读取超限、伪造较小长度、恰好边界和非限制方法；断言 HTTP 与 `Result.code`。
- `ImageProcessingLimiterTest`：默认两个并发成功、第三个快速失败、成功和异常路径均释放许可。
- `ImageProcessorTest`：正常 PNG/JPEG/WebP、GIF 50/51 帧、多帧贴纸、格式伪装、损坏数据、解码前 Base64 长度、超宽/超高/超像素。
- 超像素样本必须是 CRC 正确、压缩体积小的合法 PNG 结构，并以 Surefire `-Xmx128m` 单独验证；若在元数据门禁前完整解码，该测试应无法安全通过。
- `MinioServiceTest`：10 MiB 有界读取与 10 MiB + 1 byte 拒绝。
- 每次修改运行根 `./mvnw.cmd test`、旁路搜索和 `git diff --check`。

旁路搜索至少包括：

```powershell
rg -n "uploadBase64|Base64\.getDecoder\(\)\.decode|reader\.read\(0\)|putObject\(" common web --glob "*.java"
```

生产代码应只有共享 `ImageProcessor` 内的 Base64 解码与 `read(0)`，以及 `MinioService#uploadObject` 内的对象写入。

## 7. 错误与正确示例

### 错误

```java
// 错误：完整解码后才读取尺寸；解压炸弹已经完成大栅格分配。
BufferedImage image = reader.read(0);
if ((long) image.getWidth() * image.getHeight() > MAX_IMAGE_PIXELS) {
    throw new IllegalArgumentException("图片过大");
}

// 错误：绕过共享处理器，直接解码并上传。
byte[] bytes = Base64.getDecoder().decode(upload.getBase64());
minioService.uploadObject(bytes, objectName, contentType);
```

### 正确

```java
// 正确：元数据门禁先于唯一的完整解码调用。
int width = reader.getWidth(frameIndex);
int height = reader.getHeight(frameIndex);
long pixels = (long) width * height;
validateDimensions(width, height, pixels);
BufferedImage image = reader.read(0);

// 正确：所有新 Base64 图片通过共享存储编排。
StoredImage stored = imageStorageService.storeImageBase64Image(upload, path);
```
