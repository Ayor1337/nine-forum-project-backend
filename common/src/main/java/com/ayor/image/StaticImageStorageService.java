package com.ayor.image;

import com.ayor.entity.Base64Upload;
import com.ayor.minio.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 图片上传编排服务，负责选择处理模式并写入对象存储。
 */
@Service
@RequiredArgsConstructor
public class StaticImageStorageService {

    private final StaticImageProcessor staticImageProcessor;

    private final MinioService minioService;

    /**
     * 以表情包模式上传 Base64 图片。
     *
     * @param upload Base64 图片输入
     * @param path 对象前缀
     * @return 已上传图片结果
     */
    public StoredStaticImage storeStickerBase64Image(Base64Upload upload, String path) {
        ProcessedStaticImage processedImage = staticImageProcessor.processSticker(upload);
        String objectName = buildObjectName(path, processedImage.getOutputExt());
        String url = minioService.uploadObject(processedImage.getBytes(), objectName, processedImage.getMimeType());
        return new StoredStaticImage(processedImage, objectName, url);
    }

    /**
     * 以正文图片模式上传 Base64 图片。
     *
     * @param upload Base64 图片输入
     * @param path 对象前缀
     * @return 已上传图片结果
     */
    public StoredStaticImage storeImageBase64Image(Base64Upload upload, String path) {
        ProcessedStaticImage processedImage = staticImageProcessor.processImage(upload);
        String objectName = buildObjectName(path, processedImage.getOutputExt());
        String url = minioService.uploadObject(processedImage.getBytes(), objectName, processedImage.getMimeType());
        return new StoredStaticImage(processedImage, objectName, url);
    }

    private String buildObjectName(String path, String extension) {
        String normalizedPath = path == null ? "" : path;
        if (!normalizedPath.isEmpty() && !normalizedPath.endsWith("/")) {
            normalizedPath = normalizedPath + "/";
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        return normalizedPath + fileName;
    }
}
