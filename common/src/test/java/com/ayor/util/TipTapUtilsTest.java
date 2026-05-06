package com.ayor.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
