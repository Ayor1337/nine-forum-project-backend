package com.ayor.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.ayor.entity.Base64Upload;
import com.ayor.minio.MinioService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class QuillUtils {

    @Resource
    private MinioService minioService;

    /**
     * 将 Quill Delta 字符串解析为 ops 列表。
     *
     * @param content Quill Delta 字符串
     * @return ops 列表
     */
    public List<Map<String, Object>> quillDeltaToArray(String content) {
        Map<String, List<Map<String, Object>>> delta = JSON.parseObject(content, new TypeReference<>() {});
        return delta.get("ops");

    }

    /**
     * 将 ops 列表重新序列化为 Quill Delta 字符串。
     *
     * @param delta ops 列表
     * @return Quill Delta 字符串
     */
    public String quillArrayToDeltaString(List<Map<String, Object>> delta) {
        return JSON.toJSONString(Map.of("ops",  delta));
    }

    /**
     * 将 Quill Delta 中的 Base64 图片上传到对象存储并替换为 URL。
     *
     * @param content Quill Delta 字符串
     * @param path 上传目录
     * @return 替换后的 Quill Delta 字符串
     */
    public String quillDeltaConvertBase64ToURL(String content, String path) {
        // 将 QuillDelta 转换为 List 对象
        List<Map<String, Object>> delta = quillDeltaToArray(content);

        // 遍历 ops, 获取 ops 中的 values
        delta.forEach(ops -> {
            // 遍历 values, 去获取所有的 value 为 JSON 对象的, 并转为 JSON 对象
            ops.forEach((key, value) -> {
                if (JSON.isValidObject(value.toString())) {
                    Map<String, Object> op = JSON.parseObject(value.toString(), new TypeReference<>() {});

                    // 判断 value 是否为图片
                    if (op.containsKey("image")) {
                        // 获取图片的 BASE64
                        String imageUrl = op.get("image").toString();
                        // 将图片上传至 OSS 服务器, 并将返回的 URL 替换掉 BASE64
                        try {
                            imageUrl = minioService.uploadBase64(new Base64Upload(imageUrl, "image.png"), path);
                            op.replace("image", imageUrl);
                            ops.replace(key, op);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        });

        // 将 List 重新封装成 JSON 字符串
        return quillArrayToDeltaString(delta);
    }

    /**
     * 过滤掉包含图片的 ops。
     *
     * @param content Quill Delta 字符串
     * @return 过滤后的 Quill Delta 字符串
     */
    public String quillDeltaFilterNonImage(String content) {
        List<Map<String, Object>> delta = quillDeltaToArray(content);
        List<Map<String, Object>> filteredDelta = new ArrayList<>();
        delta.forEach(ops -> {
            ops.forEach((key, value) -> {
                if (JSON.isValidObject(value.toString())) {
                    Map<String, Object> op = JSON.parseObject(value.toString(), new TypeReference<>() {});
                    if (!op.containsKey("image")) {
                        filteredDelta.add(ops);
                    }
                } else {
                    filteredDelta.add(ops);
                }
            });
        });
        return quillArrayToDeltaString(filteredDelta);
    }

    /**
     * 将 Quill Delta 中的文本内容拼接为普通字符串。
     *
     * @param content Quill Delta 字符串
     * @return 拼接后的文本
     */
    public String quillStringToString(String content) {
        StringBuilder builder = new StringBuilder();
        String s = quillDeltaFilterNonImage(content);
        List<Map<String, Object>> maps = quillDeltaToArray(s);
        for (Map<String, Object> map : maps) {
            if (map.containsKey("insert")) {
                builder.append(map.get("insert"));
            }
        }
        return builder.toString();
    }

    /**
     * 提取 Quill Delta 中的图片 URL，最多返回 3 个。
     *
     * @param content Quill Delta 字符串
     * @return 图片 URL 列表
     */
    public List<String> quillDeltaFilterImage(String content) {
        AtomicInteger count = new AtomicInteger();

        List<Map<String, Object>> delta = quillDeltaToArray(content);
        List<String> imageUrls = new ArrayList<>();
        delta.forEach(ops -> {
            ops.forEach((key, value) -> {
                if (JSON.isValidObject(value.toString())) {
                    Map<String, Object> op = JSON.parseObject(value.toString(), new TypeReference<>() {});
                    if (op.containsKey("image") && count.get() < 3) {
                        imageUrls.add(op.get("image").toString());
                        count.getAndIncrement();
                    }
                }
            });
        });
        return imageUrls;
    }
}
