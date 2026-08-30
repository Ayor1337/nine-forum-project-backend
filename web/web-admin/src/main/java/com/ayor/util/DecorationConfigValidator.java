package com.ayor.util;

import com.ayor.type.ShopItemType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 装扮结构化配置校验器，按类型校验渲染协议 JSON 的必填字段、枚举与取值范围。
 * 校验通过返回 null，否则返回中文错误消息。
 */
public final class DecorationConfigValidator {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Pattern HEX_COLOR = Pattern.compile("^#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})$");

    private static final Set<String> FRAME_MODES = Set.of("image", "css");

    private static final Set<String> FRAME_ANIMATIONS = Set.of("none", "rotate", "pulse");

    private static final Set<String> BADGE_MODES = Set.of("icon", "image");

    // 头像框（avatar_frame）与徽章（badge）遵循协议 schemaVersion=2，尺寸为相对头像边长的比例；头衔（title）为 schemaVersion=1
    private static final int AVATAR_FRAME_SCHEMA_VERSION = 2;

    private static final int BADGE_SCHEMA_VERSION = 2;

    private static final int TITLE_SCHEMA_VERSION = 1;

    private DecorationConfigValidator() {
    }

    /**
     * 校验指定类型的配置 JSON。
     *
     * @param type 装扮类型（badge/avatar_frame/title）
     * @param configJson 配置 JSON 文本
     * @return 校验通过返回 null，否则返回错误消息
     */
    public static String validate(String type, String configJson) {
        if (!StringUtils.hasText(configJson)) {
            return "配置不能为空";
        }
        JsonNode root;
        try {
            root = OBJECT_MAPPER.readTree(configJson);
        } catch (Exception e) {
            return "配置不是合法的 JSON";
        }
        if (!root.isObject()) {
            return "配置必须是 JSON 对象";
        }
        JsonNode schemaVersion = root.get("schemaVersion");
        if (schemaVersion == null || !schemaVersion.isInt() || schemaVersion.intValue() < 1) {
            return "schemaVersion 缺失或不合法";
        }
        ShopItemType itemType = ShopItemType.fromType(type);
        if (itemType == null) {
            return "装扮类型不合法";
        }
        return switch (itemType) {
            case AVATAR_FRAME -> validateAvatarFrame(root, schemaVersion.intValue());
            case TITLE -> validateTitle(root, schemaVersion.intValue());
            case BADGE -> validateBadge(root, schemaVersion.intValue());
        };
    }

    private static String validateAvatarFrame(JsonNode root, int schemaVersion) {
        if (schemaVersion != AVATAR_FRAME_SCHEMA_VERSION) {
            return "schemaVersion 必须为 2";
        }
        String mode = textOrNull(root, "mode");
        if (mode == null || !FRAME_MODES.contains(mode)) {
            return "mode 必须为 image 或 css";
        }
        if ("image".equals(mode) && !StringUtils.hasText(textOrNull(root, "imageUrl"))) {
            return "mode 为 image 时 imageUrl 必填";
        }
        JsonNode animation = root.get("animation");
        if (animation != null) {
            if (!animation.isObject()) {
                return "animation 必须是 JSON 对象";
            }
            String animationType = textOrNull(animation, "type");
            if (animationType != null && !FRAME_ANIMATIONS.contains(animationType)) {
                return "animation.type 必须为 none、rotate 或 pulse";
            }
            JsonNode durationMs = animation.get("durationMs");
            if (durationMs != null && (!durationMs.isInt() || durationMs.intValue() < 300 || durationMs.intValue() > 10000)) {
                return "animation.durationMs 必须为 300-10000 的整数";
            }
        }
        JsonNode scale = root.get("scale");
        if (scale != null && (!scale.isNumber() || scale.doubleValue() < 1.0 || scale.doubleValue() > 1.5)) {
            return "scale 必须为 1.0-1.5 的数字";
        }
        JsonNode border = root.get("border");
        if (border != null) {
            if (!border.isObject()) {
                return "border 必须是 JSON 对象";
            }
            JsonNode width = border.get("width");
            if (width != null && (!width.isNumber() || width.doubleValue() <= 0 || width.doubleValue() > 0.5)) {
                return "border.width 必须为大于 0 且不超过 0.5 的数字";
            }
            String error = validateColorOrGradientValue(border.get("color"), "border.color");
            if (error != null) {
                return error;
            }
        }
        return null;
    }

    private static String validateTitle(JsonNode root, int schemaVersion) {
        if (schemaVersion != TITLE_SCHEMA_VERSION) {
            return "schemaVersion 必须为 1";
        }
        String color = textOrNull(root, "color");
        if (color == null && root.get("gradient") == null) {
            return "color 与 gradient 至少提供一个";
        }
        if (color != null && !isHexColor(color)) {
            return "color 必须是十六进制颜色值";
        }
        String error = validateGradient(root.get("gradient"), "gradient");
        if (error != null) {
            return error;
        }
        JsonNode background = root.get("background");
        if (background != null) {
            if (!background.isObject()) {
                return "background 必须是 JSON 对象";
            }
            error = validateColorOrGradient(background, "background");
            if (error != null) {
                return error;
            }
        }
        JsonNode fontWeight = root.get("fontWeight");
        if (fontWeight != null && (!fontWeight.isInt() || fontWeight.intValue() < 100 || fontWeight.intValue() > 900)) {
            return "fontWeight 必须为 100-900 的整数";
        }
        JsonNode glow = root.get("glow");
        if (glow != null) {
            if (!glow.isObject()) {
                return "glow 必须是 JSON 对象";
            }
            String glowColor = textOrNull(glow, "color");
            if (glowColor != null && !isHexColor(glowColor)) {
                return "glow.color 必须是十六进制颜色值";
            }
            JsonNode blur = glow.get("blur");
            if (blur != null && (!blur.isInt() || blur.intValue() < 0 || blur.intValue() > 32)) {
                return "glow.blur 必须为 0-32 的整数";
            }
        }
        return null;
    }

    private static String validateBadge(JsonNode root, int schemaVersion) {
        if (schemaVersion != BADGE_SCHEMA_VERSION) {
            return "schemaVersion 必须为 2";
        }
        String mode = textOrNull(root, "mode");
        if (mode == null || !BADGE_MODES.contains(mode)) {
            return "mode 必须为 icon 或 image";
        }
        if ("icon".equals(mode) && !StringUtils.hasText(textOrNull(root, "iconKey"))) {
            return "mode 为 icon 时 iconKey 必填";
        }
        if ("image".equals(mode) && !StringUtils.hasText(textOrNull(root, "imageUrl"))) {
            return "mode 为 image 时 imageUrl 必填";
        }
        String color = textOrNull(root, "color");
        if (color != null && !isHexColor(color)) {
            return "color 必须是十六进制颜色值";
        }
        String background = textOrNull(root, "background");
        if (background != null && !isHexColor(background)) {
            return "background 必须是十六进制颜色值";
        }
        JsonNode size = root.get("size");
        if (size != null && (!size.isNumber() || size.doubleValue() <= 0 || size.doubleValue() > 1)) {
            return "size 必须为大于 0 且不超过 1 的数字";
        }
        return null;
    }
    /**
     * 校验“纯色 hex 或 Gradient 对象”字段（v2 比例协议中 border.color 的取值形式）。
     */
    private static String validateColorOrGradientValue(JsonNode value, String prefix) {
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isTextual()) {
            return isHexColor(value.textValue()) ? null : prefix + " 必须是十六进制颜色值或渐变对象";
        }
        return validateGradient(value, prefix);
    }

    /**
     * 校验节点上可选的 color / gradient 字段（color 为十六进制，gradient 需 from/to 均为十六进制）。
     */
    private static String validateColorOrGradient(JsonNode node, String prefix) {
        String color = textOrNull(node, "color");
        if (color != null && !isHexColor(color)) {
            return prefix + ".color 必须是十六进制颜色值";
        }
        return validateGradient(node.get("gradient"), prefix + ".gradient");
    }

    private static String validateGradient(JsonNode gradient, String prefix) {
        if (gradient == null) {
            return null;
        }
        if (!gradient.isObject()) {
            return prefix + " 必须是 JSON 对象";
        }
        String from = textOrNull(gradient, "from");
        String to = textOrNull(gradient, "to");
        if (from == null || to == null || !isHexColor(from) || !isHexColor(to)) {
            return prefix + ".from 与 " + prefix + ".to 必须为十六进制颜色值";
        }
        return null;
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && value.isTextual() ? value.textValue() : null;
    }

    private static boolean isHexColor(String value) {
        return value != null && HEX_COLOR.matcher(value).matches();
    }
}
