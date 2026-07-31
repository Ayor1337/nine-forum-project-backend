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
        assertThat(DecorationConfigValidator.validate("unknown", "{\"schemaVersion\": 2}"))
                .isEqualTo("装扮类型不合法");
    }

    // 测试头像框与徽章强制 schemaVersion=2，头衔强制 schemaVersion=1（v1 协议已废弃）
    @Test
    void shouldEnforceExactSchemaVersionPerType() {
        assertThat(DecorationConfigValidator.validate("avatar_frame",
                "{\"schemaVersion\": 1, \"mode\": \"css\"}"))
                .isEqualTo("schemaVersion 必须为 2");
        assertThat(DecorationConfigValidator.validate("badge",
                "{\"schemaVersion\": 1, \"mode\": \"icon\", \"iconKey\": \"medal\"}"))
                .isEqualTo("schemaVersion 必须为 2");
        assertThat(DecorationConfigValidator.validate("title",
                "{\"schemaVersion\": 2, \"color\": \"#ffffff\"}"))
                .isEqualTo("schemaVersion 必须为 1");
    }

    // 测试头像框配置合法样例（图片与纯 CSS 两种模式，边框颜色支持纯色或渐变对象）
    @Test
    void shouldAcceptValidAvatarFrame() {
        assertThat(DecorationConfigValidator.validate("avatar_frame", """
                {"schemaVersion": 2, "mode": "image", "imageUrl": "frames/gold.webp", "scale": 1.2}
                """)).isNull();
        assertThat(DecorationConfigValidator.validate("avatar_frame", """
                {"schemaVersion": 2, "mode": "css",
                 "border": {"width": 0.08, "color": {"from": "#ff8a00", "to": "#da1b60", "direction": "135deg"}},
                 "animation": {"type": "rotate", "durationMs": 1500}, "scale": 1.5}
                """)).isNull();
        assertThat(DecorationConfigValidator.validate("avatar_frame", """
                {"schemaVersion": 2, "mode": "css",
                 "border": {"width": 0.12, "color": "#ffd700"},
                 "animation": {"type": "pulse", "durationMs": 300}, "scale": 1.0}
                """)).isNull();
    }

    // 测试头像框配置非法样例（v2 比例语义：border.width ≤ 0.5，scale 1.0~1.5，durationMs 300~10000）
    @Test
    void shouldRejectInvalidAvatarFrame() {
        assertThat(DecorationConfigValidator.validate("avatar_frame", "{\"schemaVersion\": 2}"))
                .isEqualTo("mode 必须为 image 或 css");
        assertThat(DecorationConfigValidator.validate("avatar_frame",
                "{\"schemaVersion\": 2, \"mode\": \"image\"}"))
                .isEqualTo("mode 为 image 时 imageUrl 必填");
        assertThat(DecorationConfigValidator.validate("avatar_frame",
                "{\"schemaVersion\": 2, \"mode\": \"css\", \"animation\": {\"type\": \"spin\"}}"))
                .isEqualTo("animation.type 必须为 none、rotate 或 pulse");
        assertThat(DecorationConfigValidator.validate("avatar_frame",
                "{\"schemaVersion\": 2, \"mode\": \"css\", \"animation\": {\"type\": \"none\", \"durationMs\": 200}}"))
                .isEqualTo("animation.durationMs 必须为 300-10000 的整数");
        assertThat(DecorationConfigValidator.validate("avatar_frame",
                "{\"schemaVersion\": 2, \"mode\": \"css\", \"scale\": 1.6}"))
                .isEqualTo("scale 必须为 1.0-1.5 的数字");
        assertThat(DecorationConfigValidator.validate("avatar_frame",
                "{\"schemaVersion\": 2, \"mode\": \"css\", \"border\": {\"width\": 4}}"))
                .isEqualTo("border.width 必须为大于 0 且不超过 0.5 的数字");
        assertThat(DecorationConfigValidator.validate("avatar_frame",
                "{\"schemaVersion\": 2, \"mode\": \"css\", \"border\": {\"width\": 0}}"))
                .isEqualTo("border.width 必须为大于 0 且不超过 0.5 的数字");
        assertThat(DecorationConfigValidator.validate("avatar_frame",
                "{\"schemaVersion\": 2, \"mode\": \"css\", \"border\": {\"color\": \"gold\"}}"))
                .isEqualTo("border.color 必须是十六进制颜色值或渐变对象");
        assertThat(DecorationConfigValidator.validate("avatar_frame",
                "{\"schemaVersion\": 2, \"mode\": \"css\", \"border\": {\"color\": {\"from\": \"#fff\"}}}"))
                .isEqualTo("border.color.from 与 border.color.to 必须为十六进制颜色值");
    }

    // 测试头衔配置合法与非法样例（schemaVersion=1，协议未变）
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

    // 测试徽章配置合法与非法样例（shape 字段已移除，size 为比例）
    @Test
    void shouldValidateBadge() {
        assertThat(DecorationConfigValidator.validate("badge", """
                {"schemaVersion": 2, "mode": "icon", "iconKey": "star",
                 "color": "#ffffff", "background": "#3b82f6", "size": 0.4}
                """)).isNull();
        assertThat(DecorationConfigValidator.validate("badge",
                "{\"schemaVersion\": 2, \"mode\": \"image\", \"imageUrl\": \"badges/vip.webp\", \"size\": 1}"))
                .isNull();

        assertThat(DecorationConfigValidator.validate("badge",
                "{\"schemaVersion\": 2, \"mode\": \"icon\"}"))
                .isEqualTo("mode 为 icon 时 iconKey 必填");
        assertThat(DecorationConfigValidator.validate("badge",
                "{\"schemaVersion\": 2, \"mode\": \"image\"}"))
                .isEqualTo("mode 为 image 时 imageUrl 必填");
        assertThat(DecorationConfigValidator.validate("badge",
                "{\"schemaVersion\": 2, \"mode\": \"icon\", \"iconKey\": \"star\", \"size\": 20}"))
                .isEqualTo("size 必须为大于 0 且不超过 1 的数字");
        assertThat(DecorationConfigValidator.validate("badge",
                "{\"schemaVersion\": 2, \"mode\": \"icon\", \"iconKey\": \"star\", \"size\": 0}"))
                .isEqualTo("size 必须为大于 0 且不超过 1 的数字");
    }
}
