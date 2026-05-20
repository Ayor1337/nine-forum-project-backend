package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.ChatboardHistory;
import com.ayor.entity.vo.ChatboardHistoryVO;
import com.ayor.result.Result;
import com.ayor.service.ChatboardHistoryService;
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
@RequestMapping("/api/chatboard_histories")
@RequiredArgsConstructor
public class ChatboardHistoryController {

    private final ChatboardHistoryService chatboardHistoryService;

    /**
     * 分页查询聊天板历史记录，可按话题过滤。
     */
    @GetMapping
    public Result<PageEntity<ChatboardHistoryVO>> listHistories(@RequestParam("page_num") Integer pageNum,
                                                                @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                                @RequestParam(value = "topic_id", required = false) Integer topicId) {
        return Result.dataMessageHandler(() -> chatboardHistoryService.getHistories(topicId, pageNum, pageSize), "获取聊天记录失败");
    }

    /**
     * 查询单条聊天板历史记录。
     */
    @GetMapping("/{historyId}")
    public Result<ChatboardHistoryVO> getHistory(@PathVariable("historyId") Integer historyId) {
        return Result.dataMessageHandler(() -> chatboardHistoryService.getHistoryById(historyId), "获取聊天记录失败");
    }

    /**
     * 创建聊天板历史记录。
     */
    @PostMapping
    public Result<Void> createHistory(@RequestBody ChatboardHistory history) {
        return Result.messageHandler(() -> chatboardHistoryService.createHistory(history));
    }

    /**
     * 更新指定聊天板历史记录。
     */
    @PutMapping("/{historyId}")
    public Result<Void> updateHistory(@PathVariable("historyId") Integer historyId,
                                      @RequestBody ChatboardHistory history) {
        history.setChatboardHistoryId(historyId);
        return Result.messageHandler(() -> chatboardHistoryService.updateHistory(history));
    }

    /**
     * 删除指定聊天板历史记录。
     */
    @DeleteMapping("/{historyId}")
    public Result<Void> deleteHistory(@PathVariable("historyId") Integer historyId) {
        return Result.messageHandler(() -> chatboardHistoryService.deleteHistory(historyId));
    }
}
