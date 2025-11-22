package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.ChatboardHistory;
import com.ayor.result.Result;
import com.ayor.service.ChatboardHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatboard/history")
@RequiredArgsConstructor
public class ChatboardHistoryController {

    private final ChatboardHistoryService chatboardHistoryService;

    @GetMapping
    public Result<PageEntity<ChatboardHistory>> listHistories(@RequestParam("page_num") Integer pageNum,
                                                              @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                              @RequestParam(value = "topic_id", required = false) Integer topicId) {
        return Result.dataMessageHandler(() -> chatboardHistoryService.getHistories(topicId, pageNum, pageSize), "获取聊天记录失败");
    }

    @DeleteMapping("/{history_id}")
    public Result<Void> deleteHistory(@PathVariable("history_id") Integer historyId) {
        return Result.messageHandler(() -> chatboardHistoryService.deleteHistory(historyId));
    }
}
