package com.ayor.image;

import com.ayor.entity.Base64Upload;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * StaticImageProcessor 的单元测试。
 */
class ImageProcessorTest {

    private final ImageProcessor processor = new ImageProcessor();

    @Test
    void shouldConvertEmojiPngToWebpAndLimitLongEdgeTo512() throws Exception {
        Base64Upload upload = new Base64Upload(toBase64DataUrl("png", 1200, 600), "sample.png");

        ProcessedImage image = processor.processSticker(upload);

        assertEquals("png", image.getOriginalExt());
        assertEquals("webp", image.getOutputExt());
        assertEquals("image/webp", image.getMimeType());
        assertTrue(image.getFileSize() > 0);
        assertEquals(512, image.getWidth());
        assertEquals(256, image.getHeight());
        assertTrue(image.getBytes().length > 0);
    }

    @Test
    void shouldRejectGifEmojiUpload() throws Exception {
        Base64Upload upload = new Base64Upload(toBase64DataUrl("gif", 12, 8), "sample.gif");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> processor.processSticker(upload));

        assertEquals("仅支持 jpg、jpeg、png、webp 静态图片，禁止 GIF 或其他动图", exception.getMessage());
    }

    @Test
    void shouldKeepContentGifAsOriginalFormat() throws Exception {
        Base64Upload upload = new Base64Upload(toBase64DataUrl("gif", 120, 80), "sample.gif");

        ProcessedImage image = processor.processImage(upload);

        assertEquals("gif", image.getOriginalExt());
        assertEquals("gif", image.getOutputExt());
        assertEquals("image/gif", image.getMimeType());
        assertEquals(120, image.getWidth());
        assertEquals(80, image.getHeight());
        assertTrue(image.getBytes().length > 0);
    }

    private String toBase64DataUrl(String format, int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.ORANGE);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, outputStream);
        return "data:image/" + format + ";base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
}
