package com.ayor.controller;

import com.ayor.entity.PageEntity;
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
@RequestMapping("/api/chat")
public class ChatBoardController {

    private final ChatboardHistoryService chatboardHistoryService;

    private final SecurityUtils securityUtils;

    @PostMapping("/send")
    public Result<Void> chat(@RequestBody ChatBoardMessage message) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> chatboardHistoryService.insertChatboardHistory(userId, message.getTopicId(), message.getContent()));
    }

    @GetMapping("/info/history")
    public Result<PageEntity<ChatboardHistoryVO>> getHistory(@RequestParam("topic_id")Integer topicId,
                                                             @RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                                             @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> chatboardHistoryService.getChatboardHistory(topicId, pageNum, pageSize), "获取聊天记录失败");
    }



}
