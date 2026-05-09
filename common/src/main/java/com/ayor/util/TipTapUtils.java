package com.ayor.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ayor.entity.Base64Upload;
import com.ayor.image.ImageStorageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class TipTapUtils {

    @Resource
    private ImageStorageService imageStorageService;

    /**
     * 将 TipTap JSON 中的 Base64 图片上传到对象存储，并把图片地址替换为可访问 URL。
     *
     * @param content TipTap doc JSON 字符串
     * @param path 图片存储路径
     * @return 替换后的 TipTap JSON 字符串
     */
    public String convertBase64ImagesToUrl(String content, String path) {
        JSONObject root = parseDoc(content);
        replaceBase64Images(root, path);
        return JSON.toJSONString(root);
    }

    /**
     * 将 TipTap JSON 中的图片节点替换为文本占位符。
     *
     * @param content TipTap doc JSON 字符串
     * @return 替换后的 TipTap JSON 字符串
     */
    public String filterNonImage(String content) {
        JSONObject root = parseDoc(content);
        convertImageNodes(root);
        return JSON.toJSONString(root);
    }

    /**
     * 提取 TipTap JSON 中所有文本节点内容并按文档顺序拼接。
     *
     * @param content TipTap doc JSON 字符串
     * @return 拼接后的纯文本
     */
    public String extractText(String content) {
        StringBuilder builder = new StringBuilder();
        appendText(parseDoc(content), builder);
        return builder.toString();
    }

    /**
     * 提取 TipTap JSON 中的 mention 节点信息。
     *
     * @param content TipTap doc JSON 字符串
     * @return mention 目标列表
     */
    public List<MentionTarget> extractMentions(String content) {
        List<MentionTarget> mentions = new ArrayList<>();
        collectMentions(parseDoc(content), mentions);
        return mentions;
    }

    /**
     * 提取 TipTap JSON 中的图片 URL，最多返回 3 个。
     *
     * @param content TipTap doc JSON 字符串
     * @return 图片 URL 列表
     */
    public List<String> extractImageUrls(String content) {
        List<String> imageUrls = new ArrayList<>();
        collectImageUrls(parseDoc(content), imageUrls, 3);
        return imageUrls;
    }

    /**
     * 提取 TipTap JSON 中的全部图片 URL。
     *
     * @param content TipTap doc JSON 字符串
     * @return 图片 URL 列表
     */
    public List<String> extractAllImageUrls(String content) {
        List<String> imageUrls = new ArrayList<>();
        collectImageUrls(parseDoc(content), imageUrls, -1);
        return imageUrls;
    }

    /**
     * 解析并校验 TipTap 文档 JSON。
     *
     * @param content TipTap doc JSON 字符串
     * @return 解析后的根对象
     * @throws IllegalArgumentException 当内容不是合法的 TipTap 文档 JSON 时抛出
     */
    private JSONObject parseDoc(String content) {
        try {
            JSONObject root = JSON.parseObject(content);
            if (root == null
                    || !"doc".equals(root.getString("type"))
                    || !(root.get("content") instanceof JSONArray)) {
                throw new IllegalArgumentException("content 必须是 TipTap doc JSON 字符串");
            }
            return root;
        } catch (JSONException e) {
            throw new IllegalArgumentException("content 必须是合法的 TipTap JSON 字符串", e);
        }
    }

    /**
     * 递归替换 TipTap 图片节点中的 Base64 图片。
     *
     * @param node 当前 JSON 节点
     * @param path 图片存储路径
     */
    private void replaceBase64Images(JSONObject node, String path) {
        if (isImageNode(node)) {
            JSONObject attrs = node.getJSONObject("attrs");
            String src = attrs == null ? null : attrs.getString("src");
            if (isBase64Image(src)) {
                try {
                    attrs.put("src", imageStorageService.storeImageBase64Image(new Base64Upload(src, "image." + extractImageExtension(src)), path).getUrl());
                } catch (IllegalArgumentException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        JSONArray content = node.getJSONArray("content");
        if (content == null) {
            return;
        }
        for (Object child : content) {
            if (child instanceof JSONObject childNode) {
                replaceBase64Images(childNode, path);
            }
        }
    }

    /**
     * 将图片节点转换为文本占位符节点。
     *
     * @param node 当前 JSON 节点
     */
    private void convertImageNodes(JSONObject node) {
        JSONArray content = node.getJSONArray("content");
        if (content == null) {
            return;
        }

        for (int i = 0; i < content.size(); i++) {
            Object child = content.get(i);

            if (!(child instanceof JSONObject childNode)) {
                continue;
            }

            if (isImageNode(childNode)) {
                JSONObject textNode = new JSONObject();
                textNode.put("type", "text");
                textNode.put("text", "[图片]");
                content.set(i, textNode);
                continue;
            }

            if (isStickerNode(childNode)) {
                JSONObject stickerNode = new JSONObject();
                stickerNode.put("type", "text");
                stickerNode.put("text", "[表情]");
                content.set(i, stickerNode);
                continue;
            }

            convertImageNodes(childNode);
        }
    }

    /**
     * 递归收集节点中的文本内容。
     *
     * @param node 当前 JSON 节点
     * @param builder 文本拼接器
     */
    private void appendText(JSONObject node, StringBuilder builder) {
        if ("text".equals(node.getString("type"))) {
            String text = node.getString("text");
            if (text != null) {
                builder.append(text);
            }
        }
        if (isMentionNode(node)) {
            JSONObject attrs = node.getJSONObject("attrs");
            String username = attrs == null ? null : firstNonBlank(
                    attrs.getString("username"),
                    attrs.getString("label")
            );
            if (username != null) {
                builder.append("@").append(username);
            }
        }
        JSONArray content = node.getJSONArray("content");
        if (content == null) {
            return;
        }
        for (Object child : content) {
            if (child instanceof JSONObject childNode) {
                appendText(childNode, builder);
            }
        }
    }

    /**
     * 递归收集图片 URL。
     *
     * @param node 当前 JSON 节点
     * @param imageUrls 图片 URL 结果列表
     * @param limit 最大收集数量，-1 表示不限制
     */
    private void collectImageUrls(JSONObject node, List<String> imageUrls, int limit) {
        if (limit > 0 && imageUrls.size() >= limit) {
            return;
        }
        if (isImageNode(node)) {
            JSONObject attrs = node.getJSONObject("attrs");
            String src = attrs == null ? null : attrs.getString("src");
            if (src != null && !src.isBlank()) {
                imageUrls.add(src);
            }
        }
        JSONArray content = node.getJSONArray("content");
        if (content == null) {
            return;
        }
        for (Object child : content) {
            if (limit > 0 && imageUrls.size() >= limit) {
                return;
            }
            if (child instanceof JSONObject childNode) {
                collectImageUrls(childNode, imageUrls, limit);
            }
        }
    }

    /**
     * 递归收集 mention 节点信息。
     *
     * @param node 当前 JSON 节点
     * @param mentions mention 结果列表
     */
    private void collectMentions(JSONObject node, List<MentionTarget> mentions) {
        if (isMentionNode(node)) {
            JSONObject attrs = node.getJSONObject("attrs");
            Integer accountId = attrs == null ? null : toInteger(
                    firstNonBlankObject(attrs.get("accountId"), attrs.get("id"), attrs.get("userId"))
            );
            String username = attrs == null ? null : firstNonBlank(
                    attrs.getString("username"),
                    attrs.getString("label")
            );
            if (accountId != null && username != null) {
                mentions.add(new MentionTarget(accountId, username));
            }
        }
        JSONArray content = node.getJSONArray("content");
        if (content == null) {
            return;
        }
        for (Object child : content) {
            if (child instanceof JSONObject childNode) {
                collectMentions(childNode, mentions);
            }
        }
    }

    /**
     * 判断节点是否为图片节点。
     *
     * @param node JSON 节点
     * @return 是否为图片节点
     */
    private boolean isImageNode(JSONObject node) {
        return "image".equals(node.getString("type"));
    }

    /**
     * 判断节点是否是 Sticker 节点
     *
     * @param node JSON 节点
     * @return 是否为 Sticker 节点
     */
    private boolean isStickerNode(JSONObject node) {
        return "sticker".equals(node.getString("type"));
    }

    /**
     * 判断节点是否为 mention 节点。
     *
     * @param node JSON 节点
     * @return 是否为 mention 节点
     */
    private boolean isMentionNode(JSONObject node) {
        return "mention".equals(node.getString("type"));
    }

    /**
     * 判断是否为 Base64 图片地址。
     *
     * @param src 图片地址
     * @return 是否为 Base64 图片
     */
    private boolean isBase64Image(String src) {
        return src != null && src.startsWith("data:image/");
    }

    /**
     * 从 data URI 中提取图片扩展名。
     *
     * @param src Base64 图片地址
     * @return 图片扩展名，解析失败时返回 png
     */
    private String extractImageExtension(String src) {
        int slashIndex = src.indexOf('/') + 1;
        int semicolonIndex = src.indexOf(';');
        if (slashIndex <= 0 || semicolonIndex <= slashIndex) {
            return "png";
        }
        return src.substring(slashIndex, semicolonIndex).toLowerCase();
    }

    /**
     * 返回第一个非空白字符串。
     *
     * @param values 待检查的字符串列表
     * @return 第一个非空白字符串；若都为空则返回 null
     */
    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    /**
     * 返回第一个非空白对象。
     *
     * @param values 待检查的对象列表
     * @return 第一个非空白对象；若都为空则返回 null
     */
    private Object firstNonBlankObject(Object... values) {
        for (Object value : values) {
            if (value == null) {
                continue;
            }
            if (value instanceof String text && text.isBlank()) {
                continue;
            }
            return value;
        }
        return null;
    }

    /**
     * 尝试将对象转换为整数。
     *
     * @param value 待转换对象
     * @return 转换后的整数；转换失败时返回 null
     */
    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(Objects.toString(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * mention 目标信息。
     *
     * @param accountId 账号 ID
     * @param username 用户名
     */
    public record MentionTarget(Integer accountId, String username) {
    }
}
