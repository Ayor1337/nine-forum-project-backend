package com.ayor.minio;

import com.ayor.entity.Base64Upload;
import io.minio.*;
import kotlin.Deprecated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;

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
     * 上传 Base64 编码文件到指定目录。
     *
     * @param dto 文件内容与文件名
     * @param path 目标目录前缀
     * @return 文件对象路径
     * @throws Exception MinIO 或 Base64 处理失败时抛出
     */
    public String uploadBase64(Base64Upload dto, String path) throws Exception {
        String prefix = endpoint + "/" + bucketName + "/";

        if (dto.getBase64().startsWith(prefix)) {
            int index = dto.getBase64().indexOf(bucketName);
            return dto.getBase64().substring(index);
        }

        // 3. 解析 Base64 数据
        String[] base64Parts = dto.getBase64().split(",");
        byte[] bytes = Base64.getDecoder().decode(
                base64Parts.length > 1 ? base64Parts[1] : base64Parts[0]
        );
        InputStream stream = new ByteArrayInputStream(bytes);

        // 4. 提取文件扩展名（不含点）
        String originalName = dto.getFileName();
        String ext = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < originalName.length() - 1) {
            ext = originalName.substring(dotIndex + 1);
        }

        // 5. 生成无横杠的 UUID 随机名，并拼接扩展名
        String uuidNoDash = UUID.randomUUID().toString().replaceAll("-", "");
        String randomFileName = uuidNoDash + (ext.isEmpty() ? "" : "." + ext);

        String objectName = path + randomFileName;


        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(stream, bytes.length, -1)
                        .contentType("image/" + getFileExtension(dto.getFileName()))
                        .build());

        return String.format("%s/%s", bucketName, objectName);
    }

    /**
     * 上传头像文件。
     *
     * @param dto 文件内容与文件名
     * @return 当前实现保留占位返回
     * @throws Exception MinIO 或 Base64 处理失败时抛出
     */
    @Deprecated(message = "")
    public String uploadAvatar(Base64Upload dto) throws Exception {
//        // 1. 定义访问前缀
//        String prefix = endpoint + "/" + bucketName + "/";
//
//        if (dto.getBase64().startsWith(prefix)) {
//            int index = dto.getBase64().indexOf(bucketName);
//            return dto.getBase64().substring(index);
//        }
//
//        // 3. 解析 Base64 数据
//        String[] base64Parts = dto.getBase64().split(",");
//        byte[] bytes = Base64.getDecoder().decode(
//                base64Parts.length > 1 ? base64Parts[1] : base64Parts[0]
//        );
//        InputStream stream = new ByteArrayInputStream(bytes);
//
//        // 4. 提取文件扩展名（不含点）
//        String originalName = dto.getFileName();
//        String ext = "";
//        int dotIndex = originalName.lastIndexOf('.');
//        if (dotIndex >= 0 && dotIndex < originalName.length() - 1) {
//            ext = originalName.substring(dotIndex + 1);
//        }
//
//        // 5. 生成无横杠的 UUID 随机名，并拼接扩展名
//        String uuidNoDash = UUID.randomUUID().toString().replaceAll("-", "");
//        String randomFileName = uuidNoDash + (ext.isEmpty() ? "" : "." + ext);
//
//        // 6. 拼接在 avatar/ 目录下的对象名
//        String objectName = "avatar/" + randomFileName;
//
//        // 7. 上传到 MinIO
//        minioClient.putObject(
//                PutObjectArgs.builder()
//                        .bucket(bucketName)
//                        .object(objectName)
//                        .stream(stream, bytes.length, -1)
//                        .contentType("image/" + ext)
//                        .build()
//        );
//
//        // 8. 返回完整 URL，方便前端直接访问
//        return String.format("%s/%s", bucketName, objectName);
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

    /**
     * 获取文件扩展名。
     *
     * @param fileName 文件名
     * @return 扩展名
     */
    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
