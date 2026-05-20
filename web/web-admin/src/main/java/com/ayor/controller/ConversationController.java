package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Conversation;
import com.ayor.entity.vo.ConversationVO;
import com.ayor.result.Result;
import com.ayor.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * 分页查询会话列表，可按双方账号过滤。
     */
    @GetMapping
    public Result<PageEntity<ConversationVO>> listConversations(@RequestParam("page_num") Integer pageNum,
                                                                @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                                @RequestParam(value = "alpha_account_id", required = false) Integer alphaAccountId,
                                                                @RequestParam(value = "beta_account_id", required = false) Integer betaAccountId) {
        return Result.dataMessageHandler(() -> conversationService.getConversations(pageNum, pageSize, alphaAccountId, betaAccountId), "获取会话失败");
    }

    /**
     * 查询单个会话。
     */
    @GetMapping("/{conversationId}")
    public Result<ConversationVO> getConversation(@PathVariable("conversationId") Integer conversationId) {
        return Result.dataMessageHandler(() -> conversationService.getConversationById(conversationId), "获取会话失败");
    }

    /**
     * 创建会话。
     */
    @PostMapping
    public Result<Void> createConversation(@RequestBody Conversation conversation) {
        return Result.messageHandler(() -> conversationService.createConversation(conversation));
    }

    /**
     * 更新指定会话。
     */
    @PutMapping("/{conversationId}")
    public Result<Void> updateConversation(@PathVariable("conversationId") Integer conversationId,
                                           @RequestBody Conversation conversation) {
        conversation.setConversationId(conversationId);
        return Result.messageHandler(() -> conversationService.updateConversation(conversation));
    }

    /**
     * 删除指定会话。
     */
    @DeleteMapping("/{conversationId}")
    public Result<Void> deleteConversation(@PathVariable("conversationId") Integer conversationId) {
        return Result.messageHandler(() -> conversationService.deleteConversation(conversationId));
    }
}
