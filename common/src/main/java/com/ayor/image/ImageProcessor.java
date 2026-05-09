package com.ayor.image;

import com.ayor.entity.Base64Upload;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * 静态图片处理器。
 * 负责对 Base64 图片做格式识别、合法性校验、解码、必要的缩放与转码，
 * 并统一产出带有元数据的 {@link ProcessedImage}。
 */
@Component
public class ImageProcessor {

    /**
     * 表情包上传只接受静态位图格式，统一转为 WebP。
     */
    private static final Set<String> STICKER_ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    /**
     * 正文图片允许保留 GIF 原始格式，其余静态格式直接透传。
     */
    private static final Set<String> IMAGE_ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    /**
     * Base64 解码后的原始字节上限，避免一次性读入过大的图片。
     */
    private static final int MAX_SOURCE_BYTES = 10 * 1024 * 1024;
    /**
     * 表情包最长边限制，确保贴图尺寸与传输成本可控。
     */
    private static final int STICKER_MAX_LONG_EDGE = 512;
    /**
     * WebP 输出质量。取一个偏稳妥的默认值，兼顾体积与清晰度。
     */
    private static final float WEBP_QUALITY = 0.82F;

    /**
     * 按表情包规则处理静态图片。
     *
     * @param upload Base64 图片输入
     * @return 处理后的图片结果
     */
    public ProcessedImage processSticker(Base64Upload upload) {
        if (upload == null || upload.getBase64() == null || upload.getBase64().isBlank()) {
            throw new IllegalArgumentException("图片内容不能为空");
        }
        String originalExt = detectOriginalExt(upload);
        validateAllowedExtension(originalExt, ImageProcessMode.STICKER);

        byte[] sourceBytes = decodeBase64(upload.getBase64());
        if (sourceBytes.length > MAX_SOURCE_BYTES) {
            throw new IllegalArgumentException("图片体积过大");
        }

        BufferedImage decodedImage = decodeImage(sourceBytes, originalExt, false);
        BufferedImage resizedImage = resizeIfNeeded(decodedImage, STICKER_MAX_LONG_EDGE);
        byte[] outputBytes = writeWebp(resizedImage);

        return new ProcessedImage(
                outputBytes,
                originalExt,
                "webp",
                "image/webp",
                outputBytes.length,
                resizedImage.getWidth(),
                resizedImage.getHeight(),
                sha256Hex(outputBytes)
        );
    }

    /**
     * 按正文图片规则处理图片。
     *
     * @param upload Base64 图片输入
     * @return 处理后的图片结果
     */
    public ProcessedImage processImage(Base64Upload upload) {
        if (upload == null || upload.getBase64() == null || upload.getBase64().isBlank()) {
            throw new IllegalArgumentException("图片内容不能为空");
        }
        String originalExt = detectOriginalExt(upload);
        validateAllowedExtension(originalExt, ImageProcessMode.IMAGE);

        byte[] sourceBytes = decodeBase64(upload.getBase64());
        if (sourceBytes.length > MAX_SOURCE_BYTES) {
            throw new IllegalArgumentException("图片体积过大");
        }

        BufferedImage decodedImage = decodeImage(sourceBytes, originalExt, true);
        return new ProcessedImage(
                sourceBytes,
                originalExt,
                originalExt,
                toMimeType(originalExt),
                sourceBytes.length,
                decodedImage.getWidth(),
                decodedImage.getHeight(),
                sha256Hex(sourceBytes)
        );
    }

    /**
     * 读取对象存储中的图片元数据。
     *
     * @param bytes 图片字节
     * @param fileName 文件名或对象名
     * @return 图片元数据
     */
    public ProcessedImage inspectStoredImage(byte[] bytes, String fileName) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("图片不存在或已损坏");
        }
        String originalExt = safeExtension(fileName);
        if (originalExt == null) {
            originalExt = "webp";
        }
        BufferedImage image = decodeImage(bytes, originalExt, true);
        return new ProcessedImage(
                bytes,
                originalExt,
                originalExt,
                toMimeType(originalExt),
                bytes.length,
                image.getWidth(),
                image.getHeight(),
                sha256Hex(bytes)
        );
    }

    /**
     * 推断上传图片的原始扩展名。
     * 优先取文件名后缀；若文件名缺失，则尝试从 data URL 头部提取。
     *
     * @param upload Base64 上传对象
     * @return 归一化后的扩展名
     */
    private String detectOriginalExt(Base64Upload upload) {
        String fromFileName = safeExtension(upload.getFileName());
        if (fromFileName != null) {
            // 文件名后缀优先，便于兼容非 data URL 场景。
            return fromFileName;
        }
        String base64 = upload.getBase64();
        if (!base64.startsWith("data:image/")) {
            throw new IllegalArgumentException("仅支持 jpg、jpeg、png、webp 静态图片，禁止 GIF 或其他动图");
        }
        int slashIndex = base64.indexOf('/') + 1;
        int semicolonIndex = base64.indexOf(';');
        if (slashIndex <= 0 || semicolonIndex <= slashIndex) {
            throw new IllegalArgumentException("图片格式不合法");
        }
        return normalizeExt(base64.substring(slashIndex, semicolonIndex));
    }

    /**
     * 安全读取文件名中的扩展名。
     *
     * @param fileName 文件名
     * @return 小写扩展名；若不存在则返回 {@code null}
     */
    private String safeExtension(String fileName) {
        if (fileName == null || fileName.isBlank() || !fileName.contains(".")) {
            return null;
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return null;
        }
        return normalizeExt(fileName.substring(dotIndex + 1));
    }

    /**
     * 将扩展名统一转为小写，避免大小写差异影响格式判断。
     *
     * @param ext 原始扩展名
     * @return 归一化后的扩展名
     */
    private String normalizeExt(String ext) {
        return ext == null ? null : ext.toLowerCase(Locale.ROOT);
    }

    /**
     * 按处理模式校验扩展名是否在允许列表中。
     *
     * @param ext 待校验扩展名
     * @param mode 图片处理模式
     */
    private void validateAllowedExtension(String ext, ImageProcessMode mode) {
        Set<String> allowedExtensions = mode == ImageProcessMode.STICKER
                ? STICKER_ALLOWED_EXTENSIONS
                : IMAGE_ALLOWED_EXTENSIONS;
        if (!allowedExtensions.contains(ext)) {
            throw new IllegalArgumentException("仅支持 jpg、jpeg、png、webp 静态图片，禁止 GIF 或其他动图");
        }
        if (mode == ImageProcessMode.STICKER && "gif".equals(ext)) {
            throw new IllegalArgumentException("仅支持 jpg、jpeg、png、webp 静态图片，禁止 GIF 或其他动图");
        }
    }

    /**
     * 将 Base64 文本解码为原始图片字节。
     * 兼容带有 data URL 前缀的输入。
     *
     * @param base64 Base64 文本
     * @return 解码后的图片字节
     */
    private byte[] decodeBase64(String base64) {
        String payload = base64;
        int commaIndex = base64.indexOf(',');
        if (commaIndex >= 0) {
            // 兼容 data URL 前缀：data:image/png;base64,...
            payload = base64.substring(commaIndex + 1);
        }
        try {
            return Base64.getDecoder().decode(payload);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("图片内容不是合法的 Base64 数据", exception);
        }
    }

    /**
     * 将图片字节解码为 {@link BufferedImage}。
     * 在禁止动画的场景下，会尝试通过帧数判断并拒绝多帧图片。
     *
     * @param sourceBytes 原始图片字节
     * @param originalExt 原始扩展名
     * @param allowAnimated 是否允许动画图片
     * @return 解码后的图片对象
     */
    private BufferedImage decodeImage(byte[] sourceBytes, String originalExt, boolean allowAnimated) {
        try (ImageInputStream inputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(sourceBytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);
            while (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    inputStream.seek(0);
                    reader.setInput(inputStream, false, false);
                    int imageCount;
                    try {
                        // 部分 reader 能直接给出帧数，可用来拦截 GIF/WebP 动图。
                        imageCount = reader.getNumImages(true);
                    } catch (UnsupportedOperationException exception) {
                        // 不支持帧数统计时，退化为按单帧图片处理。
                        imageCount = 1;
                    }
                    if (!allowAnimated && imageCount > 1) {
                        throw new IllegalArgumentException("仅支持静态图片，当前文件包含动画帧");
                    }
                    BufferedImage image = reader.read(0);
                    if (image != null) {
                        return image;
                    }
                } finally {
                    reader.dispose();
                }
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("图片解析失败", exception);
        }

        if ("webp".equals(originalExt)) {
            throw new IllegalArgumentException("当前环境无法解析 WebP 图片");
        }
        throw new IllegalArgumentException("图片已损坏或格式不受支持");
    }

    /**
     * 在图片最长边超出限制时按比例缩放，否则仅转换为更适合编码的像素类型。
     *
     * @param image 原始图片
     * @param maxLongEdge 允许的最长边
     * @return 缩放或兼容化后的图片
     */
    private BufferedImage resizeIfNeeded(BufferedImage image, int maxLongEdge) {
        int width = image.getWidth();
        int height = image.getHeight();
        int longEdge = Math.max(width, height);
        if (longEdge <= maxLongEdge) {
            // 即使不缩放，也转换为兼容类型，减少后续 WebP 编码时的类型分支。
            return toCompatibleImage(image);
        }

        double scale = (double) maxLongEdge / longEdge;
        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));
        int imageType = image.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, imageType);

        Graphics2D graphics = resized.createGraphics();
        try {
            // 贴图缩放以质量优先，避免文字或边缘在缩小后明显发糊。
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        } finally {
            graphics.dispose();
        }
        return resized;
    }

    /**
     * 将图片转换为常规 RGB/ARGB BufferedImage，减少编码器兼容性问题。
     *
     * @param image 原始图片
     * @return 兼容格式的图片
     */
    private BufferedImage toCompatibleImage(BufferedImage image) {
        int imageType = image.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        if (image.getType() == imageType) {
            return image;
        }
        // 将自定义色彩模型或索引色图片转成常规 ARGB/RGB，避免编码器兼容性问题。
        BufferedImage compatible = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
        Graphics2D graphics = compatible.createGraphics();
        try {
            graphics.drawImage(image, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return compatible;
    }

    /**
     * 使用当前环境中的 WebP 编码器输出图片字节。
     *
     * @param image 待编码图片
     * @return WebP 字节数组
     */
    private byte[] writeWebp(BufferedImage image) {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");
        if (!writers.hasNext()) {
            throw new IllegalStateException("当前环境未启用 WebP 编码器");
        }

        ImageWriter writer = writers.next();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream)) {
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            if (writeParam.canWriteCompressed()) {
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                String[] compressionTypes = writeParam.getCompressionTypes();
                if (compressionTypes != null && compressionTypes.length > 0) {
                    // 不依赖具体编码器实现，优先使用当前 writer 暴露的第一个压缩类型。
                    writeParam.setCompressionType(compressionTypes[0]);
                }
                writeParam.setCompressionQuality(WEBP_QUALITY);
            }
            writer.setOutput(imageOutputStream);
            writer.write(null, new IIOImage(image, null, null), writeParam);
            writer.dispose();
            return outputStream.toByteArray();
        } catch (IOException exception) {
            writer.dispose();
            throw new IllegalStateException("图片压缩失败", exception);
        }
    }

    /**
     * 计算字节内容的 SHA-256 十六进制摘要，用于去重或完整性标识。
     *
     * @param bytes 原始字节
     * @return SHA-256 十六进制字符串
     */
    private String sha256Hex(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(bytes);
            StringBuilder builder = new StringBuilder(hashBytes.length * 2);
            for (byte hashByte : hashBytes) {
                builder.append(String.format("%02x", hashByte));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 不可用", exception);
        }
    }

    /**
     * 根据扩展名构造 MIME type。
     *
     * @param extension 图片扩展名
     * @return MIME type 字符串
     */
    private String toMimeType(String extension) {
        return "image/" + extension;
    }
}
