package com.ayor.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ayor.entity.Base64Upload;
import com.ayor.minio.MinioService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Component
public class TipTapUtils {

    @Resource
    private MinioService minioService;

    /**
     * 将 TipTap JSON 中的 base64 图片上传到对象存储并替换为 URL。
     */
    public String convertBase64ImagesToUrl(String content, String path) {
        JSONObject root = parseDoc(content);
        replaceBase64Images(root, path);
        return JSON.toJSONString(root);
    }

    /**
     * 返回移除图片节点后的 TipTap JSON。
     */
    public String filterNonImage(String content) {
        JSONObject root = parseDoc(content);
        convertImageNodes(root);
        return JSON.toJSONString(root);
    }

    /**
     * 提取 TipTap JSON 中所有文本节点内容。
     */
    public String extractText(String content) {
        StringBuilder builder = new StringBuilder();
        appendText(parseDoc(content), builder);
        return builder.toString();
    }

    /**
     * 提取 TipTap JSON 中的 mention 节点。
     */
    public List<MentionTarget> extractMentions(String content) {
        List<MentionTarget> mentions = new ArrayList<>();
        collectMentions(parseDoc(content), mentions);
        return mentions;
    }

    /**
     * 提取 TipTap JSON 中的图片 URL，最多返回 3 个。
     */
    public List<String> extractImageUrls(String content) {
        List<String> imageUrls = new ArrayList<>();
        collectImageUrls(parseDoc(content), imageUrls);
        return imageUrls;
    }

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

    private void replaceBase64Images(JSONObject node, String path) {
        if (isImageNode(node)) {
            JSONObject attrs = node.getJSONObject("attrs");
            String src = attrs == null ? null : attrs.getString("src");
            if (isBase64Image(src)) {
                try {
                    attrs.put("src", minioService.uploadBase64(new Base64Upload(src, "image.png"), path));
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

            convertImageNodes(childNode);
        }
    }

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

    private void collectImageUrls(JSONObject node, List<String> imageUrls) {
        if (imageUrls.size() >= 3) {
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
            if (imageUrls.size() >= 3) {
                return;
            }
            if (child instanceof JSONObject childNode) {
                collectImageUrls(childNode, imageUrls);
            }
        }
    }

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

    private boolean isImageNode(JSONObject node) {
        return "image".equals(node.getString("type"));
    }

    private boolean isMentionNode(JSONObject node) {
        return "mention".equals(node.getString("type"));
    }

    private boolean isBase64Image(String src) {
        return src != null && src.startsWith("data:image/");
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

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

    public record MentionTarget(Integer accountId, String username) {
    }
}
