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

    public void createBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName).build());
        }
    }

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


    public void deleteFile(String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
    }


    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
