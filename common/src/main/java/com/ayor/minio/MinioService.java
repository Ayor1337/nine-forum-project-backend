package com.ayor.minio;

import com.ayor.entity.ImageUploadLimits;
import com.ayor.image.ImageValidationException;
import io.minio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
public class MinioService {

    @Value("${spring.minio.endpoint}")
    private String endpoint;

    @Autowired
    private MinioClient minioClient;

    @Value("${spring.minio.bucket}")
    private String bucketName;

    /**
     * 创建 MinIO 桶，如果桶已存在则直接返回。
     *
     * @throws Exception MinIO 操作失败时抛出
     */
    public void createBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName).build());
        }
    }

    /**
     * 上传已经处理好的图片字节到对象存储。
     *
     * @param bytes 图片字节
     * @param objectName 对象名
     * @param contentType MIME 类型
     * @return 平台内可访问地址
     */
    public String uploadObject(byte[] bytes, String objectName, String contentType) {
        try (InputStream stream = new ByteArrayInputStream(bytes)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(stream, bytes.length, -1)
                            .contentType(contentType)
                            .build()
            );
            return buildObjectUrl(objectName);
        } catch (Exception exception) {
            throw new IllegalStateException("上传图片到对象存储失败", exception);
        }
    }

    /**
     * 读取对象存储中的图片字节。
     *
     * @param rawUrl 平台内图片地址
     * @return 图片字节
     */
    public byte[] getObjectBytes(String rawUrl) {
        String objectName = extractObjectName(rawUrl);
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        )) {
            byte[] bytes = stream.readNBytes(ImageUploadLimits.MAX_SOURCE_BYTES + 1);
            if (bytes.length > ImageUploadLimits.MAX_SOURCE_BYTES) {
                throw new ImageValidationException("图片体积过大");
            }
            return bytes;
        } catch (ImageValidationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("读取对象存储图片失败", exception);
        }
    }

    /**
     * 构造平台内对象地址。
     *
     * @param objectName 对象名
     * @return 平台内地址
     */
    public String buildObjectUrl(String objectName) {
        return String.format("%s/%s", bucketName, objectName);
    }

    /**
     * 判断给定地址是否属于当前平台对象存储。
     *
     * @param rawUrl 待判断地址
     * @return 是否属于平台
     */
    public boolean isOwnObjectUrl(String rawUrl) {
        return normalizeUrl(rawUrl) != null;
    }

    /**
     * 从平台地址中提取对象名。
     *
     * @param rawUrl 平台内图片地址
     * @return 对象名
     */
    public String extractObjectName(String rawUrl) {
        String normalizedUrl = normalizeUrl(rawUrl);
        if (normalizedUrl == null) {
            throw new IllegalArgumentException("图片地址不属于当前平台");
        }
        return normalizedUrl.substring(bucketName.length() + 1);
    }

    /**
     * 将完整地址规范化为平台内标准形式。
     *
     * @param rawUrl 原始地址
     * @return 标准化后的平台地址；不属于平台则返回 null
     */
    public String normalizeUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return null;
        }
        String trimmed = rawUrl.trim();
        String fullPrefix = endpoint.endsWith("/") ? endpoint + bucketName + "/" : endpoint + "/" + bucketName + "/";
        if (trimmed.startsWith(fullPrefix)) {
            return trimmed.substring(fullPrefix.length() - bucketName.length() - 1);
        }
        if (trimmed.startsWith(bucketName + "/")) {
            return trimmed;
        }
        if (trimmed.startsWith("/" + bucketName + "/")) {
            return trimmed.substring(1);
        }
        return null;
    }

    /**
     * 删除指定对象文件。
     *
     * @param objectName 对象名
     * @throws Exception MinIO 操作失败时抛出
     */
    public void deleteFile(String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
    }
}
