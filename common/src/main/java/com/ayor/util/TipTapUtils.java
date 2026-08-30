package com.ayor.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class TipTapUtils {

    /**
     * 校验 TipTap 文档不包含图片节点。图片必须通过独立的 images 字段提交。
     *
     * @param content TipTap doc JSON 字符串
     * @throws IllegalArgumentException 当文档中存在图片节点时抛出
     */
    public void assertNoImageNodes(String content) {
        if (containsImageNode(parseDoc(content))) {
            throw new IllegalArgumentException("TipTap 内容不支持图片节点，请使用 images");
        }
    }

    /**
     * 将 TipTap JSON 中的贴纸节点转换为文本占位符。
     *
     * @param content TipTap doc JSON 字符串
     * @return 替换后的 TipTap JSON 字符串
     */
    public String filterStickerNodes(String content) {
        JSONObject root = parseDoc(content);
        convertStickerNodes(root);
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
     * 将贴纸节点转换为文本占位符节点。
     *
     * @param node 当前 JSON 节点
     */
    private void convertStickerNodes(JSONObject node) {
        JSONArray content = node.getJSONArray("content");
        if (content == null) {
            return;
        }

        for (int i = 0; i < content.size(); i++) {
            Object child = content.get(i);

            if (!(child instanceof JSONObject childNode)) {
                continue;
            }

            if (isStickerNode(childNode)) {
                JSONObject stickerNode = new JSONObject();
                stickerNode.put("type", "text");
                stickerNode.put("text", "[表情]");
                content.set(i, stickerNode);
                continue;
            }

            convertStickerNodes(childNode);
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

    private boolean containsImageNode(JSONObject node) {
        if (isImageNode(node)) {
            return true;
        }
        JSONArray content = node.getJSONArray("content");
        if (content == null) {
            return false;
        }
        for (Object child : content) {
            if (child instanceof JSONObject childNode && containsImageNode(childNode)) {
                return true;
            }
        }
        return false;
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
