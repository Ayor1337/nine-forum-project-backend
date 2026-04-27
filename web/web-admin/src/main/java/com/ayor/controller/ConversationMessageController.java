package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.ConversationMessage;
import com.ayor.result.Result;
import com.ayor.service.ConversationMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ConversationMessageController {

    private final ConversationMessageService conversationMessageService;

    @GetMapping("/api/conversations/{conversationId}/messages")
    public Result<PageEntity<ConversationMessage>> listMessages(@PathVariable("conversationId") Integer conversationId,
                                                                @RequestParam("page_num") Integer pageNum,
                                                                @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> conversationMessageService.getMessages(conversationId, pageNum, pageSize), "获取会话消息失败");
    }

    @DeleteMapping("/api/conversation_messages/{messageId}")
    public Result<Void> deleteMessage(@PathVariable("messageId") Integer messageId) {
        return Result.messageHandler(() -> conversationMessageService.deleteMessage(messageId));
    }
}
