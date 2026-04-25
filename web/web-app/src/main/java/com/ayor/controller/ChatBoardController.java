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
@RequestMapping("/api/topics/{topic_id}/chat-messages")
public class ChatBoardController {

    private final ChatboardHistoryService chatboardHistoryService;

    private final SecurityUtils securityUtils;
    /**
     * chat 方法。
     */

    @PostMapping
    public Result<Void> chat(@PathVariable("topic_id") Integer topicId,
                             @RequestBody ChatBoardMessage message) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> chatboardHistoryService.insertChatboardHistory(userId, topicId, message.getContent()));
    }
    /**
     * getHistory 方法。
     */

    @GetMapping
    public Result<PageEntity<ChatboardHistoryVO>> getHistory(@PathVariable("topic_id")Integer topicId,
                                                             @RequestParam(value = "page_num", defaultValue = "1") Integer pageNum,
                                                             @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> chatboardHistoryService.getChatboardHistory(topicId, pageNum, pageSize), "获取聊天记录失败");
    }



}
