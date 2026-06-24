package com.ayor.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageUtilsTest {

    // 测试提取对象名从普通 URL
    @Test
    void extractObjectNameFromPlainUrl() {
        assertThat(ImageUtils.extractObjectName("https://cdn.example.com/bucket/avatar/photo.webp"))
                .isEqualTo("photo.webp");
    }

    // 测试提取对象名时忽略查询参数
    @Test
    void extractObjectNameIgnoresQueryString() {
        assertThat(ImageUtils.extractObjectName("https://cdn.example.com/bucket/path/photo.png?x=1&y=2"))
                .isEqualTo("photo.png");
    }

    // 测试提取对象名时跳过尾部斜杠
    @Test
    void extractObjectNameSkipsTrailingSlash() {
        assertThat(ImageUtils.extractObjectName("https://cdn.example.com/bucket/path/"))
                .isEqualTo("path");
    }

    // 测试提取对象名拒绝空白路径
    @Test
    void extractObjectNameRejectsBlankPath() {
        assertThatThrownBy(() -> ImageUtils.extractObjectName("////?token=abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid URL format");
    }
}
