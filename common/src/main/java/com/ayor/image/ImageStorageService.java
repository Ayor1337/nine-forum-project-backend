package com.ayor.image;

import com.ayor.entity.Base64Upload;
import com.ayor.minio.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 图片上传编排服务，负责选择处理模式并写入对象存储。
 */
@Service
@RequiredArgsConstructor
public class ImageStorageService {

    private final ImageProcessor imageProcessor;

    private final MinioService minioService;

    /**
     * 以表情包模式上传 Base64 图片。
     *
     * @param upload Base64 图片输入
     * @param path 对象前缀
     * @return 已上传图片结果
     */
    public StoredImage storeStickerBase64Image(Base64Upload upload, String path) {
        ProcessedImage processedImage = imageProcessor.processSticker(upload);
        String objectName = buildObjectName(path, processedImage.getOutputExt());
        String url = minioService.uploadObject(processedImage.getBytes(), objectName, processedImage.getMimeType());
        return new StoredImage(processedImage, objectName, url);
    }

    /**
     * 以正文图片模式上传 Base64 图片。
     *
     * @param upload Base64 图片输入
     * @param path 对象前缀
     * @return 已上传图片结果
     */
    public StoredImage storeImageBase64Image(Base64Upload upload, String path) {
        ProcessedImage processedImage = imageProcessor.processImage(upload);
        String objectName = buildObjectName(path, processedImage.getOutputExt());
        String url = minioService.uploadObject(processedImage.getBytes(), objectName, processedImage.getMimeType());
        return new StoredImage(processedImage, objectName, url);
    }

    /**
     * 按请求顺序以正文图片模式批量上传 Base64 图片，并返回最终 URL。
     *
     * @param uploads Base64 图片输入；为 null 时视为空列表
     * @param path 对象前缀
     * @return 与输入顺序一致的已上传图片 URL
     */
    public List<String> storeImageBase64Images(List<Base64Upload> uploads, String path) {
        List<String> imageUrls = new ArrayList<>();
        if (uploads == null) {
            return imageUrls;
        }
        for (Base64Upload upload : uploads) {
            imageUrls.add(storeImageBase64Image(upload, path).getUrl());
        }
        return imageUrls;
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
