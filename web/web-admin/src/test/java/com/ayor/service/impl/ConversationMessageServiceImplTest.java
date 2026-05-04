package com.ayor.service.impl;

import com.ayor.entity.pojo.ConversationMessage;
import com.ayor.entity.vo.ConversationMessageVO;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

class ConversationMessageServiceImplTest {

    @Test
    void shouldReturnMessageWhenMessageExists() {
        ConversationMessageServiceImpl service = new ConversationMessageServiceImpl() {
            @Override
            public ConversationMessage getById(Serializable id) {
                return ConversationMessage.builder()
                        .conversationMessageId(6)
                        .conversationId(2)
                        .content("hello")
                        .accountId(9)
                        .build();
            }
        };

        ConversationMessageVO result = service.getMessageById(6);

        assertNotNull(result);
        assertEquals("hello", result.getContent());
    }

    @Test
    void shouldMarkMessageAsEditedWhenUpdatingContent() {
        ConversationMessageServiceImpl service = new ConversationMessageServiceImpl() {
            @Override
            public ConversationMessage getById(Serializable id) {
                return ConversationMessage.builder()
                        .conversationMessageId(6)
                        .conversationId(2)
                        .content("old")
                        .accountId(9)
                        .isEdit(false)
                        .build();
            }

            @Override
            public boolean updateById(ConversationMessage entity) {
                assertEquals("new", entity.getContent());
                assertTrue(entity.getIsEdit());
                return true;
            }
        };

        ConversationMessage input = new ConversationMessage();
        input.setConversationMessageId(6);
        input.setContent("new");

        assertNull(service.updateMessage(input));
    }
}
