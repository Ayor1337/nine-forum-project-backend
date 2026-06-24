package com.ayor.minio;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import okhttp3.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {

    @Mock
    private MinioClient minioClient;

    private MinioService minioService;

    @BeforeEach
    void setUp() {
        minioService = new MinioService();
        ReflectionTestUtils.setField(minioService, "endpoint", "https://cdn.example.com");
        ReflectionTestUtils.setField(minioService, "bucketName", "forum");
        ReflectionTestUtils.setField(minioService, "minioClient", minioClient);
    }

    // 测试规范化 URL 接受完整桶路径并开头斜杠形式
    @Test
    void normalizeUrlAcceptsFullBucketAndLeadingSlashForms() {
        assertThat(minioService.normalizeUrl(" https://cdn.example.com/forum/path/image.webp "))
                .isEqualTo("forum/path/image.webp");
        assertThat(minioService.normalizeUrl("forum/path/image.webp"))
                .isEqualTo("forum/path/image.webp");
        assertThat(minioService.normalizeUrl("/forum/path/image.webp"))
                .isEqualTo("forum/path/image.webp");
        assertThat(minioService.normalizeUrl("https://other.example.com/forum/path/image.webp"))
                .isNull();
    }

    // 测试提取对象名时拒绝外部URL
    @Test
    void extractObjectNameRejectsForeignUrl() {
        assertThat(minioService.extractObjectName("forum/content/a.webp")).isEqualTo("content/a.webp");

        assertThatThrownBy(() -> minioService.extractObjectName("https://other.example.com/forum/a.webp"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("图片地址不属于当前平台");
    }

    // 测试构建对象 URL 并自有 URL 使用桶前缀
    @Test
    void buildObjectUrlAndOwnUrlUseBucketPrefix() {
        assertThat(minioService.buildObjectUrl("content/a.webp")).isEqualTo("forum/content/a.webp");
        assertThat(minioService.isOwnObjectUrl("/forum/content/a.webp")).isTrue();
        assertThat(minioService.isOwnObjectUrl("")).isFalse();
    }

    // 测试创建桶跳过已存在桶
    @Test
    void createBucketSkipsExistingBucket() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        minioService.createBucket();

        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
    }

    // 测试创建桶创建缺失桶
    @Test
    void createBucketCreatesMissingBucket() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        minioService.createBucket();

        verify(minioClient).makeBucket(any(MakeBucketArgs.class));
    }

    // 测试上传对象写入字节并返回平台 URL
    @Test
    void uploadObjectWritesBytesAndReturnsPlatformUrl() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenReturn(new ObjectWriteResponse(Headers.of(), "forum", null, "content/a.webp", "etag", null));

        String result = minioService.uploadObject(new byte[]{1, 2}, "content/a.webp", "image/webp");

        assertThat(result).isEqualTo("forum/content/a.webp");
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    // 测试上传对象包装 MinIO 失败
    @Test
    void uploadObjectWrapsMinioFailure() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(new RuntimeException("down"));

        assertThatThrownBy(() -> minioService.uploadObject(new byte[]{1}, "content/a.webp", "image/webp"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("上传图片到对象存储失败")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    // 测试获取对象字节读取对象按规范化后的 URL
    @Test
    void getObjectBytesReadsObjectByNormalizedUrl() throws Exception {
        GetObjectResponse response = new GetObjectResponse(
                Headers.of(), "forum", null, "content/a.webp", new ByteArrayInputStream(new byte[]{4, 5, 6}));
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(response);

        byte[] result = minioService.getObjectBytes("forum/content/a.webp");

        assertThat(result).containsExactly(4, 5, 6);
    }

    // 测试获取对象字节包装读取失败
    @Test
    void getObjectBytesWrapsReadFailure() throws Exception {
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(new RuntimeException("down"));

        assertThatThrownBy(() -> minioService.getObjectBytes("forum/content/a.webp"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("读取对象存储图片失败")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    // 测试删除文件委托到 MinIO 客户端
    @Test
    void deleteFileDelegatesToMinioClient() throws Exception {
        minioService.deleteFile("content/a.webp");

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }
}
