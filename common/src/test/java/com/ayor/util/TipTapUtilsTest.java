package com.ayor.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * TipTapUtils 的单元测试。
 */
class TipTapUtilsTest {

    private final TipTapUtils tipTapUtils = new TipTapUtils();

    // 测试提取提及目标从 TipTap JSON
    @Test
    void shouldExtractMentionTargetsFromTipTapJson() {
        String content = """
                {
                  "type": "doc",
                  "content": [
                    {
                      "type": "paragraph",
                      "content": [
                        {
                          "type": "text",
                          "text": "hello "
                        },
                        {
                          "type": "mention",
                          "attrs": {
                            "accountId": 12,
                            "username": "alice"
                          }
                        },
                        {
                          "type": "text",
                          "text": " world"
                        }
                      ]
                    }
                  ]
                }
                """;

        List<TipTapUtils.MentionTarget> mentions = tipTapUtils.extractMentions(content);

        assertEquals(1, mentions.size());
        assertEquals(12, mentions.get(0).accountId());
        assertEquals("alice", mentions.get(0).username());
    }

    // 测试提取纯文本时会包含提及内容
    @Test
    void shouldIncludeMentionsWhenExtractingPlainText() {
        String content = """
                {
                  "type": "doc",
                  "content": [
                    {
                      "type": "paragraph",
                      "content": [
                        {
                          "type": "text",
                          "text": "hi "
                        },
                        {
                          "type": "mention",
                          "attrs": {
                            "accountId": 7,
                            "username": "bob"
                          }
                        }
                      ]
                    }
                  ]
                }
                """;

        String text = tipTapUtils.extractText(content);

        assertEquals("hi @bob", text);
    }

    // 测试拒绝任意层级的图片节点
    @Test
    void shouldRejectImageNodesAtAnyDepth() {
        String imageContent = """
                {
                  "type": "doc",
                  "content": [
                    {
                      "type": "paragraph",
                      "content": [{"type": "image", "attrs": {"src": "https://example.com/a.png"}}]
                    }
                  ]
                }
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tipTapUtils.assertNoImageNodes(imageContent)
        );

        assertEquals("TipTap 内容不支持图片节点，请使用 images", exception.getMessage());
        assertDoesNotThrow(() -> tipTapUtils.assertNoImageNodes("{\"type\":\"doc\",\"content\":[]}"));
    }

    // 测试贴纸节点仍转换为文本占位符
    @Test
    void shouldConvertStickerNodesToTextPlaceholders() {
        String content = """
                {
                  "type": "doc",
                  "content": [
                    {"type": "sticker", "attrs": {"src": "https://example.com/sticker.png"}},
                    {"type": "paragraph", "content": [
                      {"type": "text", "text": "hello"}
                    ]}
                  ]
                }
                """;

        String result = tipTapUtils.filterStickerNodes(content);

        assertEquals("[表情]hello", tipTapUtils.extractText(result));
    }
}
