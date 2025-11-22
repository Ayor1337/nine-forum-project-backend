package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Conversation;
import com.ayor.result.Result;
import com.ayor.service.ConversationService;
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
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping("/list")
    public Result<PageEntity<Conversation>> listConversations(@RequestParam("page_num") Integer pageNum,
                                                              @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                              @RequestParam(value = "alpha_account_id", required = false) Integer alphaAccountId,
                                                              @RequestParam(value = "beta_account_id", required = false) Integer betaAccountId) {
        return Result.dataMessageHandler(() -> conversationService.getConversations(pageNum, pageSize, alphaAccountId, betaAccountId), "获取会话失败");
    }

    @DeleteMapping("/{conversation_id}")
    public Result<Void> deleteConversation(@PathVariable("conversation_id") Integer conversationId) {
        return Result.messageHandler(() -> conversationService.deleteConversation(conversationId));
    }
}
