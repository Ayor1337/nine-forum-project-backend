package com.ayor.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DecorationConfigValidatorTest {

    // 测试公共结构校验：空配置、非法 JSON、非对象、schemaVersion 缺失、未知类型
    @Test
    void shouldRejectInvalidCommonStructure() {
        assertThat(DecorationConfigValidator.validate("avatar_frame", " ")).isEqualTo("配置不能为空");
        assertThat(DecorationConfigValidator.validate("avatar_frame", "{oops")).isEqualTo("配置不是合法的 JSON");
        assertThat(DecorationConfigValidator.validate("avatar_frame", "[1]")).isEqualTo("配置必须是 JSON 对象");
        assertThat(DecorationConfigValidator.validate("avatar_frame", "{}"))
                .isEqualTo("schemaVersion 缺失或不合法");
        assertThat(DecorationConfigValidator.validate("unknown", "{\"schemaVersion\": 1}"))
                .isEqualTo("装扮类型不合法");
    }

    // 测试头像框配置合法样例（图片与纯 CSS 两种模式）
    @Test
    void shouldAcceptValidAvatarFrame() {
        assertThat(DecorationConfigValidator.validate("avatar_frame", """
                {"schemaVersion": 1, "mode": "image", "imageUrl": "https://example.com/frame.webp", "scale": 1.2}
                """)).isNull();
        assertThat(DecorationConfigValidator.validate("avatar_frame", """
                {"schemaVersion": 1, "mode": "css",
                 "border": {"width": 4, "color": "#ffd700", "gradient": {"from": "#ff8a00", "to": "#da1b60"}},
                 "animation": {"type": "pulse", "durationMs": 1500}, "scale": 1.0}
                """)).isNull();
    }

    // 测试头像框配置非法样例
    @Test
    void shouldRejectInvalidAvatarFrame() {
        assertThat(DecorationConfigValidator.validate("avatar_frame", "{\"schemaVersion\": 1}"))
                .isEqualTo("mode 必须为 image 或 css");
        assertThat(DecorationConfigValidator.validate("avatar_frame",
                "{\"schemaVersion\": 1, \"mode\": \"image\"}"))
                .isEqualTo("mode 为 image 时 imageUrl 必填");
        assertThat(DecorationConfigValidator.validate("avatar_frame",
                "{\"schemaVersion\": 1, \"mode\": \"css\", \"animation\": {\"type\": \"spin\"}}"))
                .isEqualTo("animation.type 必须为 none、rotate 或 pulse");
        assertThat(DecorationConfigValidator.validate("avatar_frame",
                "{\"schemaVersion\": 1, \"mode\": \"css\", \"scale\": 3}"))
                .isEqualTo("scale 必须为 0.5-2.0 的数字");
        assertThat(DecorationConfigValidator.validate("avatar_frame",
                "{\"schemaVersion\": 1, \"mode\": \"css\", \"border\": {\"color\": \"gold\"}}"))
                .isEqualTo("border.color 必须是十六进制颜色值");
    }

    // 测试头衔配置合法与非法样例
    @Test
    void shouldValidateTitle() {
        assertThat(DecorationConfigValidator.validate("title", """
                {"schemaVersion": 1, "color": "#ffffff", "fontWeight": 700,
                 "background": {"color": "#000000", "radius": 8}, "glow": {"color": "#ffd700", "blur": 8}}
                """)).isNull();
        assertThat(DecorationConfigValidator.validate("title",
                "{\"schemaVersion\": 1, \"gradient\": {\"from\": \"#ff8a00\", \"to\": \"#da1b60\"}}"))
                .isNull();

        assertThat(DecorationConfigValidator.validate("title", "{\"schemaVersion\": 1}"))
                .isEqualTo("color 与 gradient 至少提供一个");
        assertThat(DecorationConfigValidator.validate("title",
                "{\"schemaVersion\": 1, \"color\": \"white\"}"))
                .isEqualTo("color 必须是十六进制颜色值");
        assertThat(DecorationConfigValidator.validate("title",
                "{\"schemaVersion\": 1, \"color\": \"#fff\", \"fontWeight\": 1000}"))
                .isEqualTo("fontWeight 必须为 100-900 的整数");
    }

    // 测试徽章配置合法与非法样例
    @Test
    void shouldValidateBadge() {
        assertThat(DecorationConfigValidator.validate("badge", """
                {"schemaVersion": 1, "mode": "icon", "iconKey": "medal", "shape": "circle",
                 "color": "#ffffff", "background": "#3b82f6", "size": 20}
                """)).isNull();
        assertThat(DecorationConfigValidator.validate("badge",
                "{\"schemaVersion\": 1, \"mode\": \"image\", \"imageUrl\": \"https://example.com/badge.webp\", \"shape\": \"hex\"}"))
                .isNull();

        assertThat(DecorationConfigValidator.validate("badge",
                "{\"schemaVersion\": 1, \"mode\": \"icon\", \"shape\": \"circle\"}"))
                .isEqualTo("mode 为 icon 时 iconKey 必填");
        assertThat(DecorationConfigValidator.validate("badge",
                "{\"schemaVersion\": 1, \"mode\": \"icon\", \"iconKey\": \"medal\"}"))
                .isEqualTo("shape 必须为 circle、square 或 hex");
        assertThat(DecorationConfigValidator.validate("badge",
                "{\"schemaVersion\": 1, \"mode\": \"icon\", \"iconKey\": \"medal\", \"shape\": \"circle\", \"size\": 100}"))
                .isEqualTo("size 必须为 8-64 的整数");
    }
}
