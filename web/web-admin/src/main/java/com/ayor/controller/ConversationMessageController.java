package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.ConversationMessage;
import com.ayor.result.Result;
import com.ayor.service.ConversationMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversation")
@RequiredArgsConstructor
public class ConversationMessageController {

    private final ConversationMessageService conversationMessageService;

    @GetMapping("/{conversation_id}/messages")
    public Result<PageEntity<ConversationMessage>> listMessages(@PathVariable("conversation_id") Integer conversationId,
                                                                @RequestParam("page_num") Integer pageNum,
                                                                @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> conversationMessageService.getMessages(conversationId, pageNum, pageSize), "获取会话消息失败");
    }

    @DeleteMapping("/message/{message_id}")
    public Result<Void> deleteMessage(@PathVariable("message_id") Integer messageId) {
        return Result.messageHandler(() -> conversationMessageService.deleteMessage(messageId));
    }
}
