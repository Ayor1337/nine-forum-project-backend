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

    @Test
    void shouldPreserveGifExtensionWhenConvertingBase64Images() {
        ImageStorageService storageService = mock(ImageStorageService.class);
        ReflectionTestUtils.setField(tipTapUtils, "staticImageStorageService", storageService);
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
}
