package com.ayor.controlller;

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
@RequestMapping("/api/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    private final ConversationMessageService conversationMessageService;

    private final SecurityUtils securityUtils;

    @PostMapping("/new")
    public Result<Void> newConversation(@RequestParam("username") String toUsername) {
        String username = securityUtils.getSecurityUsername();
        return Result.messageHandler(() -> conversationService.createNewConversation(username, toUsername));
    }

    @GetMapping("/talk")
    public Result<ConversationVO> startConversation(@RequestParam("accountId") Integer toAccountId) {
        String username = securityUtils.getSecurityUsername();
        return Result.dataMessageHandler(() -> conversationService.getConversationByAccountId(username, toAccountId), "获取聊天列表失败");
    }

    @PostMapping("/hide")
    public Result<Void> hideConversation(@RequestParam("conversationId") Integer conversationId) {
        String username = securityUtils.getSecurityUsername();
        return Result.messageHandler(() -> conversationService.hiddenConversation(conversationId, username));
    }

    @GetMapping("/list")
    public Result<List<ConversationVO>> listConversation() {
        String username = securityUtils.getSecurityUsername();
        return Result.dataMessageHandler(() -> conversationService.getConversationList(username), "获取聊天列表失败");
    }

    @PostMapping("/send")
    public Result<Void> sendMessage(@RequestBody ConversationMessageDTO conversationMessage) {
        String username = securityUtils.getSecurityUsername();
        return Result.messageHandler(() -> conversationMessageService.sendMessage(conversationMessage, username));
    }

    @GetMapping("/message/list")
    public Result<List<ConversationMessageVO>> listMessage(@RequestParam("conversationId") Integer conversationId) {
        String username = securityUtils.getSecurityUsername();
        return Result.dataMessageHandler(() -> conversationMessageService.getConversationMessageList(conversationId,  username), "获取聊天列表失败");
    }

    @GetMapping("/message/unread")
    public Result<List<ChatUnread>> getUnreadMessageCount() {
        String username = securityUtils.getSecurityUsername();
        return Result.dataMessageHandler(() -> conversationService.getUnreadList(username), "获取未读消息数量失败");
    }

    @GetMapping("/message/read")
    public Result<Void> clearUnreadMessageCount(@RequestParam("conversationId") Integer conversationId,
                                                @RequestParam("fromUserId") Integer fromUserId) {
        return Result.messageHandler(() -> conversationService.clearUnread(conversationId, fromUserId));
    }


}
