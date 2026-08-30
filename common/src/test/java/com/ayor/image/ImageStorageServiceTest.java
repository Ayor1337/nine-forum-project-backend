package com.ayor.image;

import com.ayor.entity.Base64Upload;
import com.ayor.minio.MinioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageStorageServiceTest {

    @Mock
    private ImageProcessor imageProcessor;

    @Mock
    private MinioService minioService;

    // 测试存储贴纸处理贴纸并上传生成对象
    @Test
    void storeStickerProcessesStickerAndUploadsGeneratedObject() {
        ImageStorageService service = new ImageStorageService(imageProcessor, minioService);
        Base64Upload upload = new Base64Upload("data:image/png;base64,abc", "sticker.png");
        ProcessedImage processedImage = processedImage("webp");
        when(imageProcessor.processSticker(upload)).thenReturn(processedImage);
        when(minioService.uploadObject(eq(processedImage.getBytes()), org.mockito.ArgumentMatchers.anyString(), eq("image/webp")))
                .thenReturn("bucket/stickers/generated.webp");

        StoredImage result = service.storeStickerBase64Image(upload, "stickers");

        ArgumentCaptor<String> objectNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(minioService).uploadObject(eq(processedImage.getBytes()), objectNameCaptor.capture(), eq("image/webp"));
        assertThat(objectNameCaptor.getValue()).startsWith("stickers/").endsWith(".webp");
        assertThat(result.getUrl()).isEqualTo("bucket/stickers/generated.webp");
        assertThat(result.getOutputExt()).isEqualTo("webp");
        assertThat(result.getSha256()).isEqualTo("hash");
    }

    // 测试存储图片时规范化路径并去掉尾部斜杠
    @Test
    void storeImageNormalizesPathWithoutTrailingSlash() {
        ImageStorageService service = new ImageStorageService(imageProcessor, minioService);
        Base64Upload upload = new Base64Upload("data:image/jpeg;base64,abc", "image.jpg");
        ProcessedImage processedImage = processedImage("jpg");
        when(imageProcessor.processImage(upload)).thenReturn(processedImage);
        when(minioService.uploadObject(eq(processedImage.getBytes()), org.mockito.ArgumentMatchers.anyString(), eq("image/webp")))
                .thenReturn("bucket/content/generated.jpg");

        StoredImage result = service.storeImageBase64Image(upload, "content/");

        ArgumentCaptor<String> objectNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(minioService).uploadObject(eq(processedImage.getBytes()), objectNameCaptor.capture(), eq("image/webp"));
        assertThat(objectNameCaptor.getValue()).startsWith("content/").doesNotContain("//").endsWith(".jpg");
        assertThat(result.getObjectName()).isEqualTo(objectNameCaptor.getValue());
        assertThat(result.getBytes()).containsExactly(1, 2, 3);
    }

    @Test
    void storeImagesReturnsUrlsInInputOrder() {
        ImageStorageService service = new ImageStorageService(imageProcessor, minioService);
        Base64Upload first = new Base64Upload("data:image/png;base64,first", "first.png");
        Base64Upload second = new Base64Upload("data:image/png;base64,second", "second.png");
        ProcessedImage processedImage = processedImage("png");
        when(imageProcessor.processImage(first)).thenReturn(processedImage);
        when(imageProcessor.processImage(second)).thenReturn(processedImage);
        when(minioService.uploadObject(eq(processedImage.getBytes()), org.mockito.ArgumentMatchers.anyString(), eq("image/webp")))
                .thenReturn("bucket/content/first.png", "bucket/content/second.png");

        assertThat(service.storeImageBase64Images(java.util.List.of(first, second), "content"))
                .containsExactly("bucket/content/first.png", "bucket/content/second.png");
    }

    private ProcessedImage processedImage(String outputExt) {
        return new ProcessedImage(new byte[]{1, 2, 3}, "png", outputExt, "image/webp", 3, 10, 20, "hash");
    }
}
