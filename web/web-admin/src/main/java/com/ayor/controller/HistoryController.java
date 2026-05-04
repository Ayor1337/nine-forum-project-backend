package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.History;
import com.ayor.entity.vo.HistoryVO;
import com.ayor.result.Result;
import com.ayor.service.HistoryService;
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
@RequestMapping("/api/histories")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public Result<PageEntity<HistoryVO>> listHistories(@RequestParam("page_num") Integer pageNum,
                                                       @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                       @RequestParam(value = "thread_id", required = false) Integer threadId,
                                                       @RequestParam(value = "account_id", required = false) Integer accountId) {
        return Result.dataMessageHandler(() -> historyService.getHistories(pageNum, pageSize, threadId, accountId), "获取浏览记录失败");
    }

    @GetMapping("/{historyId}")
    public Result<HistoryVO> getHistory(@PathVariable("historyId") Integer historyId) {
        return Result.dataMessageHandler(() -> historyService.getHistoryById(historyId), "获取浏览记录失败");
    }

    @PostMapping
    public Result<Void> createHistory(@RequestBody History history) {
        return Result.messageHandler(() -> historyService.createHistory(history));
    }

    @PutMapping("/{historyId}")
    public Result<Void> updateHistory(@PathVariable("historyId") Integer historyId,
                                      @RequestBody History history) {
        history.setHistoryId(historyId);
        return Result.messageHandler(() -> historyService.updateHistory(history));
    }

    @DeleteMapping("/{historyId}")
    public Result<Void> deleteHistory(@PathVariable("historyId") Integer historyId) {
        return Result.messageHandler(() -> historyService.deleteHistory(historyId));
    }
}
