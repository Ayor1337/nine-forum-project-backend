package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.dto.ConversationMessageDTO;
import com.ayor.entity.app.vo.ConversationMessageVO;
import com.ayor.entity.app.vo.ConversationVO;
import com.ayor.entity.stomp.ChatUnread;
import com.ayor.result.Result;
import com.ayor.service.ConversationMessageService;
import com.ayor.service.ConversationService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    private final ConversationMessageService conversationMessageService;

    private final SecurityUtils securityUtils;
    /**
     * newConversation 方法。
     */

    @PostMapping
    public Result<Void> newConversation(@RequestParam("username") String toUsername) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> conversationService.createNewConversation(userId, toUsername));
    }
    /**
     * startConversation 方法。
     */

    @GetMapping("/with-user/{account_id}")
    public Result<ConversationVO> startConversation(@PathVariable("account_id") Integer toAccountId) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> conversationService.getConversationByAccountId(userId, toAccountId), "获取聊天列表失败");
    }
    /**
     * hideConversation 方法。
     */

    @DeleteMapping("/{conversation_id}")
    public Result<Void> hideConversation(@PathVariable("conversation_id") Integer conversationId) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> conversationService.hiddenConversation(conversationId, userId));
    }
    /**
     * listConversation 方法。
     */

    @GetMapping
    public Result<List<ConversationVO>> listConversation() {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> conversationService.getConversationList(userId), "获取聊天列表失败");
    }
    /**
     * sendMessage 方法。
     */

    @PostMapping("/{conversation_id}/messages")
    public Result<Void> sendMessage(@PathVariable("conversation_id") Integer conversationId,
                                    @RequestBody ConversationMessageDTO conversationMessage) {
        conversationMessage.setConversationId(conversationId);
        Integer userId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> conversationMessageService.sendMessage(conversationMessage, userId));
    }
    /**
     * listMessage 方法。
     */

    @GetMapping("/{conversation_id}/messages")
    public Result<PageEntity<ConversationMessageVO>> listMessage(@PathVariable("conversation_id") Integer conversationId,
                                                                 @RequestParam("page_num") Integer pageNum) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> conversationMessageService.getConversationMessageList(conversationId,  userId, pageNum), "获取聊天列表失败");
    }
    /**
     * getUnreadMessageCount 方法。
     */

    @GetMapping("/unread-messages")
    public Result<List<ChatUnread>> getUnreadMessageCount() {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> conversationService.getUnreadList(userId), "获取未读消息数量失败");
    }
    /**
     * clearUnreadMessageCount 方法。
     */

    @DeleteMapping("/{conversation_id}/unread-messages")
    public Result<Void> clearUnreadMessageCount(@PathVariable("conversation_id") Integer conversationId,
                                                @RequestParam("from_user_id") Integer fromUserId) {
        return Result.messageHandler(() -> conversationService.clearUnread(conversationId, fromUserId));
    }


}
