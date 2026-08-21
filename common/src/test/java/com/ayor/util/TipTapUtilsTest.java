package com.ayor.util;

import com.ayor.entity.Base64Upload;
import com.ayor.image.ImageStorageService;
import com.ayor.image.StoredImage;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    // 测试转换Base64图片时保留GIF扩展名
    @Test
    void shouldPreserveGifExtensionWhenConvertingBase64Images() {
        ImageStorageService storageService = mock(ImageStorageService.class);
        ReflectionTestUtils.setField(tipTapUtils, "imageStorageService", storageService);
        StoredImage storedStaticImage = new StoredImage();
        storedStaticImage.setObjectName("posts/1/a.gif");
        storedStaticImage.setUrl("nineforum/posts/1/a.gif");
        when(storageService.storeImageBase64Image(any(Base64Upload.class), eq("posts/1/")))
                .thenReturn(storedStaticImage);

        String content = """
                {
                  "type": "doc",
                  "content": [
                    {
                      "type": "image",
                      "attrs": {
                        "src": "data:image/gif;base64,R0lGODlhAQABAIAAAAUEBA=="
                      }
                    }
                  ]
                }
                """;

        tipTapUtils.convertBase64ImagesToUrl(content, "posts/1/");

        verify(storageService).storeImageBase64Image(new Base64Upload("data:image/gif;base64,R0lGODlhAQABAIAAAAUEBA==", "image.gif"), "posts/1/");
    }

    // 测试图片节点计数的 0、7、8 张边界
    @Test
    void shouldCountImageNodesAtThreadLimitBoundaries() {
        assertEquals(0, tipTapUtils.countImageNodes(imageDocument(0)));
        assertEquals(List.of(), tipTapUtils.extractAllImageUrls(imageDocument(0)));
        assertEquals(7, tipTapUtils.countImageNodes(imageDocument(7)));
        assertEquals(8, tipTapUtils.countImageNodes(imageDocument(8)));
    }

    // 测试图片节点计数包含URL与Base64、保留重复节点且不统计sticker
    @Test
    void shouldCountOnlyImageNodesRegardlessOfSource() {
        String content = """
                {
                  "type": "doc",
                  "content": [
                    {"type": "image", "attrs": {"src": "https://example.com/repeated.png"}},
                    {"type": "sticker", "attrs": {"src": "https://example.com/sticker.png"}},
                    {"type": "paragraph", "content": [
                      {"type": "image", "attrs": {"src": "data:image/png;base64,AA=="}},
                      {"type": "image", "attrs": {"src": "https://example.com/repeated.png"}}
                    ]}
                  ]
                }
                """;

        assertEquals(3, tipTapUtils.countImageNodes(content));
        assertEquals(
                List.of(
                        "https://example.com/repeated.png",
                        "data:image/png;base64,AA==",
                        "https://example.com/repeated.png"
                ),
                tipTapUtils.extractAllImageUrls(content)
        );
    }

    // 测试丢弃图片节点时保留其他内容
    @Test
    void shouldDiscardImageNodesWithoutAddingImagePlaceholder() {
        String content = """
                {
                  "type": "doc",
                  "content": [
                    {"type": "image", "attrs": {"src": "https://example.com/top.png"}},
                    {"type": "paragraph", "content": [
                      {"type": "text", "text": "hello"},
                      {"type": "image", "attrs": {"src": "https://example.com/inline.png"}}
                    ]},
                    {"type": "sticker", "attrs": {"src": "https://example.com/sticker.png"}}
                  ]
                }
                """;

        String result = tipTapUtils.discardImageNodes(content);

        assertEquals(0, tipTapUtils.countImageNodes(result));
        assertEquals(List.of(), tipTapUtils.extractAllImageUrls(result));
        assertEquals("hello[表情]", tipTapUtils.extractText(tipTapUtils.filterNonImage(result)));
    }

    private String imageDocument(int imageCount) {
        StringBuilder builder = new StringBuilder("{\"type\":\"doc\",\"content\":[");
        for (int index = 0; index < imageCount; index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append("{\"type\":\"image\",\"attrs\":{\"src\":\"https://example.com/")
                    .append(index)
                    .append(".png\"}}");
        }
        return builder.append("]}").toString();
    }
}
