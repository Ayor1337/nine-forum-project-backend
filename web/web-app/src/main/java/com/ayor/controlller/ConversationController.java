package com.ayor.controlller;

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
@RequestMapping("/api/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    private final ConversationMessageService conversationMessageService;

    private final SecurityUtils securityUtils;

    @PostMapping("/new")
    public Result<Void> newConversation(@RequestParam("username") String toUsername) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> conversationService.createNewConversation(userId, toUsername));
    }

    @GetMapping("/talk")
    public Result<ConversationVO> startConversation(@RequestParam("accountId") Integer toAccountId) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> conversationService.getConversationByAccountId(userId, toAccountId), "获取聊天列表失败");
    }

    @PostMapping("/hide")
    public Result<Void> hideConversation(@RequestParam("conversationId") Integer conversationId) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> conversationService.hiddenConversation(conversationId, userId));
    }

    @GetMapping("/list")
    public Result<List<ConversationVO>> listConversation() {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> conversationService.getConversationList(userId), "获取聊天列表失败");
    }

    @PostMapping("/send")
    public Result<Void> sendMessage(@RequestBody ConversationMessageDTO conversationMessage) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> conversationMessageService.sendMessage(conversationMessage, userId));
    }

    @GetMapping("/message/list")
    public Result<PageEntity<ConversationMessageVO>> listMessage(@RequestParam("conversationId") Integer conversationId,
                                                                 @RequestParam("page_num") Integer pageNum) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> conversationMessageService.getConversationMessageList(conversationId,  userId, pageNum), "获取聊天列表失败");
    }

    @GetMapping("/message/unread")
    public Result<List<ChatUnread>> getUnreadMessageCount() {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> conversationService.getUnreadList(userId), "获取未读消息数量失败");
    }

    @GetMapping("/message/read")
    public Result<Void> clearUnreadMessageCount(@RequestParam("conversationId") Integer conversationId,
                                                @RequestParam("fromUserId") Integer fromUserId) {
        return Result.messageHandler(() -> conversationService.clearUnread(conversationId, fromUserId));
    }


}
