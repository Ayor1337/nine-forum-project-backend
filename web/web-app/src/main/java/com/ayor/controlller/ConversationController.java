package com.ayor.controlller;

import com.ayor.entity.app.dto.ConversationMessageDTO;
import com.ayor.entity.app.vo.ConversationMessageVO;
import com.ayor.entity.app.vo.ConversationVO;
import com.ayor.entity.pojo.ConversationMessage;
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

    @PostMapping("/talk")
    public Result<Void> newConversation(@RequestParam("username") String toUsername) {
        String username = securityUtils.getSecurityUsername();
        return Result.messageHandler(() -> conversationService.createNewConversation(username, toUsername));
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
        return Result.dataMessageHandler(() -> conversationMessageService.getConversationMessageList(conversationId), "获取聊天列表失败");
    }


}
