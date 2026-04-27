package com.ayor.util;

import com.ayor.entity.Base64Upload;
import com.ayor.minio.MinioService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TipTapUtilsTest {

    private final TipTapUtils tipTapUtils = new TipTapUtils();

    @Test
    void extractTextRecursivelyConcatenatesTipTapTextNodes() {
        String content = """
                {"type":"doc","content":[
                  {"type":"paragraph","content":[{"type":"text","text":"hello "},{"type":"text","text":"world"}]},
                  {"type":"blockquote","content":[{"type":"paragraph","content":[{"type":"text","text":"!"}]}]}
                ]}
                """;

        assertEquals("hello world!", tipTapUtils.extractText(content));
    }

    @Test
    void extractImageUrlsReturnsAtMostThreeImageSrcValues() {
        String content = """
                {"type":"doc","content":[
                  {"type":"image","attrs":{"src":"https://example.com/1.png"}},
                  {"type":"paragraph","content":[{"type":"image","attrs":{"src":"https://example.com/2.png"}}]},
                  {"type":"image","attrs":{"src":"https://example.com/3.png"}},
                  {"type":"image","attrs":{"src":"https://example.com/4.png"}}
                ]}
                """;

        assertEquals(List.of(
                "https://example.com/1.png",
                "https://example.com/2.png",
                "https://example.com/3.png"
        ), tipTapUtils.extractImageUrls(content));
    }

    @Test
    void filterNonImageRemovesImageNodesAndKeepsTextStructure() {
        String content = """
                {"type":"doc","content":[
                  {"type":"paragraph","content":[{"type":"text","text":"before"},{"type":"image","attrs":{"src":"https://example.com/a.png"}}]},
                  {"type":"image","attrs":{"src":"https://example.com/b.png"}},
                  {"type":"paragraph","content":[{"type":"text","text":"after"}]}
                ]}
                """;

        String filtered = tipTapUtils.filterNonImage(content);

        assertTrue(filtered.contains("\"type\":\"doc\""));
        assertTrue(filtered.contains("\"text\":\"before\""));
        assertTrue(filtered.contains("\"text\":\"after\""));
        assertTrue(!filtered.contains("\"type\":\"image\""));
    }

    @Test
    void convertBase64ImagesToUrlUploadsBase64ImageSrcAndReplacesIt() throws Exception {
        MinioService minioService = mock(MinioService.class);
        ReflectionTestUtils.setField(tipTapUtils, "minioService", minioService);
        when(minioService.uploadBase64(any(Base64Upload.class), eq("threads/1/")))
                .thenReturn("https://cdn.example.com/image.png");
        String content = """
                {"type":"doc","content":[
                  {"type":"image","attrs":{"src":"data:image/png;base64,AAAA"}},
                  {"type":"image","attrs":{"src":"https://example.com/already.png"}}
                ]}
                """;

        String converted = tipTapUtils.convertBase64ImagesToUrl(content, "threads/1/");

        assertTrue(converted.contains("\"src\":\"https://cdn.example.com/image.png\""));
        assertTrue(converted.contains("\"src\":\"https://example.com/already.png\""));
        assertTrue(!converted.contains("data:image/png;base64,AAAA"));
        verify(minioService).uploadBase64(any(Base64Upload.class), eq("threads/1/"));
    }

    @Test
    void parseRejectsInvalidJsonAndNonDocRoot() {
        assertThrows(IllegalArgumentException.class, () -> tipTapUtils.extractText("{"));
        assertThrows(IllegalArgumentException.class, () -> tipTapUtils.extractText("{\"type\":\"paragraph\"}"));
    }
}
