# 图片安全边界调研

## 仓库事实

- 图片处理依赖 Java 17 `ImageIO` 与 `com.github.usefulness:webp-imageio:0.10.2`。
- `ImageProcessor` 是共享 `@Component`，同时被用户端与管理端通过 `ImageStorageService` 复用。
- 当前解码后单图字节上限是 10 MiB，但没有宽、高、总像素、帧数或并发上限。
- 主题一次最多 7 张图片；评论图片数量当前不设上限。请求将 Base64 嵌入 JSON，因此编码文本约比原始字节多三分之一。
- 仓库没有 JVM `-Xmx`、容器内存或反向代理请求体上限，不能假设部署层会兜底。

## API 依据

- Java 17 `ImageReader#getWidth(int)` / `getHeight(int)` 返回指定图片的像素尺寸；`read(int)` 才返回完整 `BufferedImage`。因此可以在完整栅格分配前完成尺寸与像素门禁：<https://docs.oracle.com/en/java/javase/17/docs/api/java.desktop/javax/imageio/ImageReader.html>
- `ImageReader#getNumImages(boolean)` 在允许搜索时可能搜索输入源；实现帧数上限时应避免无界完整扫描，至多探测到“上限 + 1”帧即停止：<https://docs.oracle.com/en/java/javase/17/docs/api/java.desktop/javax/imageio/ImageReader.html>
- Tomcat `maxPostSize` 仅限制表单参数转换，不是 JSON POST 的通用请求体限制；`maxSwallowSize` 也只控制中止上传后吞咽的字节。应用需要读取时计数的请求包装器覆盖 JSON 与分块传输：<https://tomcat.apache.org/tomcat-10.1-doc/config/http.html>
- Spring Boot 暴露的 `server.tomcat.max-http-form-post-size` 同样是表单内容限制：<https://docs.spring.io/spring-boot/appendix/application-properties/>

## 推荐安全档

- 解码后单图：保留 10 MiB。
- 单边：8192 px。
- 总像素：16,777,216（约 16 MP；ARGB 完整栅格约 64 MiB，不含解码器额外开销）。
- 动图：最多 50 帧；贴纸仍固定 1 帧。
- JSON 请求体：16 MiB，作为一次请求内所有字段与 Base64 的总上限。
- 图片处理并发：每个 JVM 2 个，槽满快速失败。

## 取舍

- 推荐档允许多张小图，但不保证一次请求携带 7 张接近 10 MiB 的图片；要保留该极端兼容性，请求体上限需接近 96 MiB，会显著削弱请求内存防护。
- 16 MP 会拒绝部分 48 MP 手机原图；放宽到 40 MP 时单个 ARGB 栅格约 160 MiB，并发 2 个即可能仅栅格占用约 320 MiB，不适合当前未知堆上限的部署。
- 每 JVM 限流简单且能保护单实例；多实例全局限流需要外部协调，本任务不引入。
