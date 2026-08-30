# 实施计划

## 1. 共享边界

- [x] 在 `model` 建立唯一图片上传限制常量，并给 `Base64Upload` 增加 `@NotBlank` / `@Size` 校验与 OpenAPI 说明。
- [x] 给 `ThreadDTO`、`PostDTO`、`PostEditDTO`、用户资料与话题 DTO 的 `Base64Upload` 字段补 `@Valid`；给直接接收 `Base64Upload` 的 Controller 参数补 `@Valid`。
- [x] 新增请求体大小过滤器及其声明长度、未知长度/分块读取、边界值和非写请求测试。

## 2. 解码前门禁

- [x] 新增专用图片校验/繁忙异常和共享异常 Advice。
- [x] 新增可独立测试的 `ImageProcessingLimiter`，验证槽满快速失败、成功释放和异常释放。
- [x] 重构 `ImageProcessor`：Base64 长度预检、真实格式匹配、帧元数据有界探测、边长/像素 `long` 校验全部位于 `read(0)` 前。
- [x] 为 PNG/JPEG/WebP 静态图、GIF 帧边界、扩展名伪装和损坏输入增加回归。
- [x] 构造 CRC 正确但声明超大尺寸的小体积 PNG；在 128 MiB 测试堆下确认拒绝而非分配大图。

## 3. 旁路清理

- [x] 将管理端话题封面 data URL 改走 `ImageStorageService`，保留普通 URL 行为。
- [x] 删除 `MinioService#uploadBase64` 及只验证该旁路的测试；全仓确认没有生产调用点。
- [x] 将 `MinioService#getObjectBytes` 改为最多读取 10 MiB + 1 byte，保护已有对象检查路径。
- [x] 确认主题、评论、贴纸、头像、横幅、用户端话题封面与管理端装饰资源全部进入共享处理器。

## 4. 验证

- [x] `./mvnw.cmd -pl model -am test`
- [x] `./mvnw.cmd -pl common -am test`
- [x] `./mvnw.cmd -pl web/web-app -am test`
- [x] `./mvnw.cmd -pl web/web-admin -am test`
- [x] `./mvnw.cmd test`
- [x] 使用 Surefire `-Xmx128m` 单独执行超大尺寸样本测试并记录结果。
- [x] `rg -n "uploadBase64|Base64\.getDecoder\(\)\.decode" common web --glob "*.java"`，人工确认不存在上传旁路。
- [x] `git diff --check` 与最终全范围 Trellis check。

## 5. 审查与回滚点

- [x] 审查请求过滤器对 chunked/未知 Content-Length 的真实读取限制，避免只检查请求头。
- [x] 审查每个 ImageIO reader 在 `read(0)` 前均已完成尺寸、像素与帧数门禁。
- [x] 审查并发许可的 `finally` 释放，以及繁忙/校验异常不会落为 500。
- [x] 若全局请求限制产生非图片接口兼容问题，只回滚过滤器注册；保留 Base64、元数据和并发防护。
