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

@Component
public class StaticImageProcessor {

    private static final Set<String> STICKER_ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> IMAGE_ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final int MAX_SOURCE_BYTES = 10 * 1024 * 1024;
    private static final int STICKER_MAX_LONG_EDGE = 512;
    private static final float WEBP_QUALITY = 0.82F;

    /**
     * 按表情包规则处理静态图片。
     *
     * @param upload Base64 图片输入
     * @return 处理后的图片结果
     */
    public ProcessedStaticImage processSticker(Base64Upload upload) {
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

        return new ProcessedStaticImage(
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
    public ProcessedStaticImage processImage(Base64Upload upload) {
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
        return new ProcessedStaticImage(
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
    public ProcessedStaticImage inspectStoredImage(byte[] bytes, String fileName) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("图片不存在或已损坏");
        }
        String originalExt = safeExtension(fileName);
        if (originalExt == null) {
            originalExt = "webp";
        }
        BufferedImage image = decodeImage(bytes, originalExt, true);
        return new ProcessedStaticImage(
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

    private String detectOriginalExt(Base64Upload upload) {
        String fromFileName = safeExtension(upload.getFileName());
        if (fromFileName != null) {
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

    private String normalizeExt(String ext) {
        return ext == null ? null : ext.toLowerCase(Locale.ROOT);
    }

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

    private byte[] decodeBase64(String base64) {
        String payload = base64;
        int commaIndex = base64.indexOf(',');
        if (commaIndex >= 0) {
            payload = base64.substring(commaIndex + 1);
        }
        try {
            return Base64.getDecoder().decode(payload);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("图片内容不是合法的 Base64 数据", exception);
        }
    }

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
                        imageCount = reader.getNumImages(true);
                    } catch (UnsupportedOperationException exception) {
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

    private BufferedImage resizeIfNeeded(BufferedImage image, int maxLongEdge) {
        int width = image.getWidth();
        int height = image.getHeight();
        int longEdge = Math.max(width, height);
        if (longEdge <= maxLongEdge) {
            return toCompatibleImage(image);
        }

        double scale = (double) maxLongEdge / longEdge;
        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));
        int imageType = image.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, imageType);

        Graphics2D graphics = resized.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        } finally {
            graphics.dispose();
        }
        return resized;
    }

    private BufferedImage toCompatibleImage(BufferedImage image) {
        int imageType = image.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        if (image.getType() == imageType) {
            return image;
        }
        BufferedImage compatible = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
        Graphics2D graphics = compatible.createGraphics();
        try {
            graphics.drawImage(image, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return compatible;
    }

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

    private String toMimeType(String extension) {
        return "image/" + extension;
    }
}
