package com.ayor.controlller;

import com.ayor.entity.app.vo.ChatboardHistoryVO;
import com.ayor.entity.stomp.ChatBoardMessage;
import com.ayor.result.Result;
import com.ayor.service.ChatboardHistoryService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatBoardController {

    private final ChatboardHistoryService chatboardHistoryService;

    private final SecurityUtils securityUtils;

    @PostMapping("/chat")
    public Result<Void> chat(@RequestBody ChatBoardMessage message) {
        String username = securityUtils.getSecurityUsername();
        return Result.messageHandler(() -> chatboardHistoryService.insertChatboardHistory(username, message.getTopicId(), message.getContent()));
    }

    @GetMapping("/chat/history")
    public Result<List<ChatboardHistoryVO>> getHistory(@RequestParam("topic_id")Integer topicId) {
        return Result.dataMessageHandler(() -> chatboardHistoryService.getChatboardHistory(topicId), "获取聊天记录失败");
    }



}
