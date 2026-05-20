package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.ConversationMessage;
import com.ayor.entity.vo.ConversationMessageVO;
import com.ayor.result.Result;
import com.ayor.service.ConversationMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ConversationMessageController {

    private final ConversationMessageService conversationMessageService;

    /**
     * 分页查询某个会话下的消息。
     */
    @GetMapping("/api/conversations/{conversationId}/messages")
    public Result<PageEntity<ConversationMessageVO>> listMessages(@PathVariable("conversationId") Integer conversationId,
                                                                  @RequestParam("page_num") Integer pageNum,
                                                                  @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> conversationMessageService.getMessages(conversationId, pageNum, pageSize), "获取会话消息失败");
    }

    /**
     * 创建会话消息。
     */
    @PostMapping("/api/conversations/{conversationId}/messages")
    public Result<Void> createMessage(@PathVariable("conversationId") Integer conversationId,
                                      @RequestBody ConversationMessage message) {
        message.setConversationId(conversationId);
        return Result.messageHandler(() -> conversationMessageService.createMessage(message));
    }

    /**
     * 查询单条会话消息。
     */
    @GetMapping("/api/conversation_messages/{messageId}")
    public Result<ConversationMessageVO> getMessage(@PathVariable("messageId") Integer messageId) {
        return Result.dataMessageHandler(() -> conversationMessageService.getMessageById(messageId), "获取会话消息失败");
    }

    /**
     * 更新单条会话消息。
     */
    @PutMapping("/api/conversation_messages/{messageId}")
    public Result<Void> updateMessage(@PathVariable("messageId") Integer messageId,
                                      @RequestBody ConversationMessage message) {
        message.setConversationMessageId(messageId);
        return Result.messageHandler(() -> conversationMessageService.updateMessage(message));
    }

    /**
     * 删除指定消息。
     */
    @DeleteMapping("/api/conversation_messages/{messageId}")
    public Result<Void> deleteMessage(@PathVariable("messageId") Integer messageId) {
        return Result.messageHandler(() -> conversationMessageService.deleteMessage(messageId));
    }
}
