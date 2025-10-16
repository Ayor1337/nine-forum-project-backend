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

    public List<Map<String, Object>> QuillDeltaToArray(String content) {
        Map<String, List<Map<String, Object>>> delta = JSON.parseObject(content, new TypeReference<>() {});
        return delta.get("ops");

    }

    public String QuillArrayToDeltaString(List<Map<String, Object>> delta) {
        return JSON.toJSONString(Map.of("ops",  delta));
    }

    public String QuillDeltaConvertBase64ToURL(String content, String path) {
        // 将 QuillDelta 转换为 List 对象
        List<Map<String, Object>> delta = QuillDeltaToArray(content);

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
        return QuillArrayToDeltaString(delta);
    }

    // 过滤所有图片
    public String QuillDeltaFilterNonImage (String content) {
        List<Map<String, Object>> delta = QuillDeltaToArray(content);
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
        return QuillArrayToDeltaString(filteredDelta);
    }

    // 将图片滤出成一个 List, 限制为 5 个
    public List<String> QuillDeltaFilterImage (String content) {
        AtomicInteger count = new AtomicInteger();

        List<Map<String, Object>> delta = QuillDeltaToArray(content);
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
