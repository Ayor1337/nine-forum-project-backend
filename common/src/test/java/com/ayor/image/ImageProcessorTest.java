package com.ayor.image;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.ImageUploadLimits;
import org.junit.jupiter.api.Test;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Iterator;
import java.util.zip.CRC32;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageProcessorTest {

    private final ImageProcessor processor = new ImageProcessor(new ImageProcessingLimiter());

    @Test
    void shouldConvertStickerPngToWebpAndLimitLongEdgeTo512() throws Exception {
        Base64Upload upload = new Base64Upload(toBase64DataUrl("png", 1200, 600), "sample.png");

        ProcessedImage image = processor.processSticker(upload);

        assertEquals("png", image.getOriginalExt());
        assertEquals("webp", image.getOutputExt());
        assertEquals("image/webp", image.getMimeType());
        assertEquals(512, image.getWidth());
        assertEquals(256, image.getHeight());
        assertTrue(image.getBytes().length > 0);
    }

    @Test
    void shouldAcceptStaticJpegAndWebp() throws Exception {
        ProcessedImage jpeg = processor.processImage(
                new Base64Upload(toBase64DataUrl("jpg", 24, 12), "sample.jpg"));
        ProcessedImage encodedWebp = processor.processSticker(
                new Base64Upload(toBase64DataUrl("png", 24, 12), "sample.png"));
        String webpData = "data:image/webp;base64," + Base64.getEncoder().encodeToString(encodedWebp.getBytes());

        ProcessedImage webp = processor.processImage(new Base64Upload(webpData, "sample.webp"));

        assertEquals("image/jpeg", jpeg.getMimeType());
        assertEquals("webp", webp.getOutputExt());
        assertEquals(24, webp.getWidth());
    }

    @Test
    void shouldRejectGifStickerUpload() throws Exception {
        Base64Upload upload = new Base64Upload(toBase64DataUrl("gif", 12, 8), "sample.gif");

        ImageValidationException exception = assertThrows(
                ImageValidationException.class, () -> processor.processSticker(upload));

        assertEquals("仅支持 jpg、jpeg、png、webp 静态图片，禁止 GIF 或其他动图", exception.getMessage());
    }

    @Test
    void shouldKeepContentGifAtFiftyFramesAndRejectFiftyFirst() throws Exception {
        ProcessedImage accepted = processor.processImage(
                new Base64Upload(toAnimatedGifDataUrl(50), "accepted.gif"));
        Base64Upload rejected = new Base64Upload(toAnimatedGifDataUrl(51), "rejected.gif");

        ImageValidationException exception = assertThrows(
                ImageValidationException.class, () -> processor.processImage(rejected));

        assertEquals("gif", accepted.getOutputExt());
        assertEquals("图片动画帧数不能超过50帧", exception.getMessage());
    }

    @Test
    void shouldRejectOversizedWidthFromPngMetadataBeforeRasterDecode() throws Exception {
        Base64Upload upload = pngWithDeclaredDimensions(9000, 1);

        ImageValidationException exception = assertThrows(
                ImageValidationException.class, () -> processor.processImage(upload));

        assertEquals("图片宽高不能超过8192像素", exception.getMessage());
    }

    @Test
    void shouldRejectOversizedHeightFromPngMetadataBeforeRasterDecode() throws Exception {
        Base64Upload upload = pngWithDeclaredDimensions(1, 9000);

        ImageValidationException exception = assertThrows(
                ImageValidationException.class, () -> processor.processImage(upload));

        assertEquals("图片宽高不能超过8192像素", exception.getMessage());
    }

    @Test
    void shouldRejectOversizedPixelCountFromPngMetadataBeforeRasterDecode() throws Exception {
        Base64Upload upload = pngWithDeclaredDimensions(4096, 4097);

        ImageValidationException exception = assertThrows(
                ImageValidationException.class, () -> processor.processImage(upload));

        assertEquals("图片总像素不能超过16777216", exception.getMessage());
    }

    @Test
    void shouldRejectBase64LengthBeforeDecode() {
        String oversizedPayload = "A".repeat(ImageUploadLimits.MAX_BASE64_PAYLOAD_CHARS + 1);
        Base64Upload upload = new Base64Upload(oversizedPayload, "sample.png");

        ImageValidationException exception = assertThrows(
                ImageValidationException.class, () -> processor.processImage(upload));

        assertEquals("图片体积过大", exception.getMessage());
    }

    @Test
    void shouldRejectExtensionDisguiseAndDamagedInput() throws Exception {
        Base64Upload disguised = new Base64Upload(toBase64DataUrl("png", 8, 8), "sample.jpg");
        Base64Upload damaged = new Base64Upload("data:image/png;base64,AAAA", "sample.png");

        assertThrows(ImageValidationException.class, () -> processor.processImage(disguised));
        assertThrows(ImageValidationException.class, () -> processor.processImage(damaged));
    }

    private Base64Upload pngWithDeclaredDimensions(int width, int height) throws Exception {
        byte[] png = decodeDataUrl(toBase64DataUrl("png", 1, 1));
        ByteBuffer.wrap(png, 16, 4).putInt(width);
        ByteBuffer.wrap(png, 20, 4).putInt(height);
        CRC32 crc = new CRC32();
        crc.update(png, 12, 17);
        ByteBuffer.wrap(png, 29, 4).putInt((int) crc.getValue());
        String dataUrl = "data:image/png;base64," + Base64.getEncoder().encodeToString(png);
        return new Base64Upload(dataUrl, "oversized.png");
    }

    private byte[] decodeDataUrl(String dataUrl) {
        return Base64.getDecoder().decode(dataUrl.substring(dataUrl.indexOf(',') + 1));
    }

    private String toAnimatedGifDataUrl(int frameCount) throws Exception {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("gif");
        ImageWriter writer = writers.next();
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(imageOutput);
            writer.prepareWriteSequence(null);
            for (int index = 0; index < frameCount; index++) {
                BufferedImage frame = createImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                writer.writeToSequence(new IIOImage(frame, null, null), writer.getDefaultWriteParam());
            }
            writer.endWriteSequence();
            imageOutput.flush();
            return "data:image/gif;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
        } finally {
            writer.dispose();
        }
    }

    private String toBase64DataUrl(String format, int width, int height) throws Exception {
        int imageType = "jpg".equals(format) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage image = createImage(width, height, imageType);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, outputStream);
        return "data:image/" + format + ";base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    private BufferedImage createImage(int width, int height, int imageType) {
        BufferedImage image = new BufferedImage(width, height, imageType);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.ORANGE);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        return image;
    }
}
