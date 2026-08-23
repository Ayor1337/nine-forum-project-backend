package com.ayor.image;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.ImageUploadLimits;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
 * Validates and processes uploaded images behind bounded memory and concurrency gates.
 */
@Component
public class ImageProcessor {

    private static final Set<String> STICKER_ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> IMAGE_ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final int STICKER_MAX_LONG_EDGE = 512;
    private static final float WEBP_QUALITY = 0.82F;

    private final ImageProcessingLimiter limiter;

    public ImageProcessor(ImageProcessingLimiter limiter) {
        this.limiter = limiter;
    }

    public ProcessedImage processSticker(Base64Upload upload) {
        return limiter.execute(() -> processStickerWithinLimit(upload));
    }

    public ProcessedImage processImage(Base64Upload upload) {
        return limiter.execute(() -> processImageWithinLimit(upload));
    }

    public ProcessedImage inspectStoredImage(byte[] bytes, String fileName) {
        return limiter.execute(() -> inspectStoredImageWithinLimit(bytes, fileName));
    }

    private ProcessedImage processStickerWithinLimit(Base64Upload upload) {
        String originalExt = validateUploadAndDetectDeclaredExtension(upload, ImageProcessMode.STICKER);
        byte[] sourceBytes = decodeBase64(upload.getBase64());
        BufferedImage decodedImage = decodeImageAfterMetadataValidation(sourceBytes, originalExt, 1);
        BufferedImage resizedImage = resizeIfNeeded(decodedImage, STICKER_MAX_LONG_EDGE);
        byte[] outputBytes = writeWebp(resizedImage);

        return new ProcessedImage(outputBytes, originalExt, "webp", "image/webp", outputBytes.length,
                resizedImage.getWidth(), resizedImage.getHeight(), sha256Hex(outputBytes));
    }

    private ProcessedImage processImageWithinLimit(Base64Upload upload) {
        String originalExt = validateUploadAndDetectDeclaredExtension(upload, ImageProcessMode.IMAGE);
        byte[] sourceBytes = decodeBase64(upload.getBase64());
        BufferedImage decodedImage = decodeImageAfterMetadataValidation(
                sourceBytes, originalExt, ImageUploadLimits.MAX_IMAGE_FRAMES);

        return new ProcessedImage(sourceBytes, originalExt, originalExt, toMimeType(originalExt), sourceBytes.length,
                decodedImage.getWidth(), decodedImage.getHeight(), sha256Hex(sourceBytes));
    }

    private ProcessedImage inspectStoredImageWithinLimit(byte[] bytes, String fileName) {
        if (bytes == null || bytes.length == 0) {
            throw new ImageValidationException("图片不存在或已损坏");
        }
        if (bytes.length > ImageUploadLimits.MAX_SOURCE_BYTES) {
            throw new ImageValidationException("图片体积过大");
        }
        String originalExt = safeExtension(fileName);
        if (originalExt == null) {
            throw new ImageValidationException("图片格式不合法");
        }
        validateAllowedExtension(originalExt, ImageProcessMode.IMAGE);
        BufferedImage image = decodeImageAfterMetadataValidation(bytes, originalExt, ImageUploadLimits.MAX_IMAGE_FRAMES);
        return new ProcessedImage(bytes, originalExt, originalExt, toMimeType(originalExt), bytes.length,
                image.getWidth(), image.getHeight(), sha256Hex(bytes));
    }

    private String validateUploadAndDetectDeclaredExtension(Base64Upload upload, ImageProcessMode mode) {
        if (upload == null || upload.getBase64() == null || upload.getBase64().isBlank()) {
            throw new ImageValidationException("图片内容不能为空");
        }
        String fileExtension = safeExtension(upload.getFileName());
        String dataUrlExtension = dataUrlExtension(upload.getBase64());
        if (fileExtension == null && dataUrlExtension == null) {
            throw new ImageValidationException("图片格式不合法");
        }
        if (fileExtension != null && dataUrlExtension != null
                && !canonicalExtension(fileExtension).equals(canonicalExtension(dataUrlExtension))) {
            throw new ImageValidationException("图片声明格式不一致");
        }
        String declaredExtension = fileExtension == null ? dataUrlExtension : fileExtension;
        validateAllowedExtension(declaredExtension, mode);
        return declaredExtension;
    }

    private String dataUrlExtension(String base64) {
        if (!base64.startsWith("data:")) {
            if (base64.indexOf(',') >= 0) {
                throw new ImageValidationException("图片内容不是合法的 Base64 数据");
            }
            return null;
        }
        int commaIndex = base64.indexOf(',');
        if (commaIndex < 0) {
            throw new ImageValidationException("图片内容不是合法的 Base64 数据");
        }
        String header = base64.substring(0, commaIndex).toLowerCase(Locale.ROOT);
        String prefix = "data:image/";
        String suffix = ";base64";
        if (!header.startsWith(prefix) || !header.endsWith(suffix)) {
            throw new ImageValidationException("图片格式不合法");
        }
        String extension = header.substring(prefix.length(), header.length() - suffix.length());
        return extension.isBlank() ? null : normalizeExtension(extension);
    }

    private String safeExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return null;
        }
        return normalizeExtension(fileName.substring(dotIndex + 1));
    }

    private String normalizeExtension(String extension) {
        return extension == null ? null : extension.toLowerCase(Locale.ROOT);
    }

    private String canonicalExtension(String extension) {
        return "jpg".equals(extension) ? "jpeg" : extension;
    }

    private void validateAllowedExtension(String extension, ImageProcessMode mode) {
        Set<String> allowedExtensions = mode == ImageProcessMode.STICKER
                ? STICKER_ALLOWED_EXTENSIONS
                : IMAGE_ALLOWED_EXTENSIONS;
        if (!allowedExtensions.contains(extension)) {
            if (mode == ImageProcessMode.STICKER) {
                throw new ImageValidationException("仅支持 jpg、jpeg、png、webp 静态图片，禁止 GIF 或其他动图");
            }
            throw new ImageValidationException("仅支持 jpg、jpeg、png、webp、gif 图片");
        }
    }

    private byte[] decodeBase64(String base64) {
        int payloadStart = 0;
        int commaIndex = base64.indexOf(',');
        if (commaIndex >= 0) {
            payloadStart = commaIndex + 1;
        }
        int payloadLength = base64.length() - payloadStart;
        if (payloadLength > ImageUploadLimits.MAX_BASE64_PAYLOAD_CHARS) {
            throw new ImageValidationException("图片体积过大");
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(base64.substring(payloadStart));
            if (bytes.length > ImageUploadLimits.MAX_SOURCE_BYTES) {
                throw new ImageValidationException("图片体积过大");
            }
            return bytes;
        } catch (ImageValidationException exception) {
            throw exception;
        } catch (IllegalArgumentException exception) {
            throw new ImageValidationException("图片内容不是合法的 Base64 数据", exception);
        }
    }

    /**
     * Reads bounded metadata for every allowed frame before decoding frame zero.
     */
    private BufferedImage decodeImageAfterMetadataValidation(byte[] sourceBytes,
                                                             String declaredExtension,
                                                             int maximumFrames) {
        try (ImageInputStream inputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(sourceBytes))) {
            if (inputStream == null) {
                throw new ImageValidationException("图片已损坏或格式不受支持");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);
            if (!readers.hasNext()) {
                if ("webp".equals(declaredExtension)) {
                    throw new ImageValidationException("当前环境无法解析 WebP 图片");
                }
                throw new ImageValidationException("图片已损坏或格式不受支持");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(inputStream, false, false);
                String actualExtension = normalizeExtension(reader.getFormatName());
                if (!canonicalExtension(declaredExtension).equals(canonicalExtension(actualExtension))) {
                    throw new ImageValidationException("图片真实格式与声明格式不一致");
                }
                validateFrameMetadata(reader, maximumFrames);
                BufferedImage image = reader.read(0);
                if (image == null) {
                    throw new ImageValidationException("图片已损坏或格式不受支持");
                }
                return image;
            } finally {
                reader.dispose();
            }
        } catch (ImageValidationException exception) {
            throw exception;
        } catch (IOException | IndexOutOfBoundsException exception) {
            throw new ImageValidationException("图片解析失败", exception);
        }
    }

    private void validateFrameMetadata(ImageReader reader, int maximumFrames) throws IOException {
        int knownFrameCount;
        try {
            knownFrameCount = reader.getNumImages(false);
        } catch (UnsupportedOperationException exception) {
            knownFrameCount = -1;
        }
        if (knownFrameCount == 0) {
            throw new ImageValidationException("图片不包含有效帧");
        }
        if (knownFrameCount > maximumFrames) {
            throw frameLimitException(maximumFrames);
        }
        if (knownFrameCount > 0) {
            for (int frameIndex = 0; frameIndex < knownFrameCount; frameIndex++) {
                validateDimensions(reader.getWidth(frameIndex), reader.getHeight(frameIndex));
            }
            return;
        }

        int discoveredFrames = 0;
        for (int frameIndex = 0; frameIndex <= maximumFrames; frameIndex++) {
            try {
                validateDimensions(reader.getWidth(frameIndex), reader.getHeight(frameIndex));
                discoveredFrames++;
            } catch (IndexOutOfBoundsException exception) {
                break;
            }
            if (discoveredFrames > maximumFrames) {
                throw frameLimitException(maximumFrames);
            }
        }
        if (discoveredFrames == 0) {
            throw new ImageValidationException("图片不包含有效帧");
        }
    }

    private ImageValidationException frameLimitException(int maximumFrames) {
        return maximumFrames == 1
                ? new ImageValidationException("仅支持静态图片，当前文件包含动画帧")
                : new ImageValidationException("图片动画帧数不能超过" + maximumFrames + "帧");
    }

    private void validateDimensions(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new ImageValidationException("图片尺寸不合法");
        }
        if (width > ImageUploadLimits.MAX_IMAGE_EDGE || height > ImageUploadLimits.MAX_IMAGE_EDGE) {
            throw new ImageValidationException("图片宽高不能超过8192像素");
        }
        long pixels = (long) width * height;
        if (pixels > ImageUploadLimits.MAX_IMAGE_PIXELS) {
            throw new ImageValidationException("图片总像素不能超过16777216");
        }
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
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("图片压缩失败", exception);
        } finally {
            writer.dispose();
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
        return "image/" + canonicalExtension(extension);
    }
}
