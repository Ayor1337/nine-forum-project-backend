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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "浏览历史管理", description = "后台浏览历史记录管理接口")
@RestController
@RequestMapping("/api/histories")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @Operation(summary = "查询后台资源")
    @GetMapping
    public Result<PageEntity<HistoryVO>> listHistories(@Parameter(description = "页码") @RequestParam("page_num") Integer pageNum,
                                                       @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                       @Parameter(description = "帖子ID") @RequestParam(value = "thread_id", required = false) Integer threadId,
                                                       @Parameter(description = "用户ID") @RequestParam(value = "account_id", required = false) Integer accountId) {
        return Result.dataMessageHandler(() -> historyService.getHistories(pageNum, pageSize, threadId, accountId), "获取浏览记录失败");
    }

    @Operation(summary = "执行后台管理操作")
    @GetMapping("/{historyId}")
    public Result<HistoryVO> getHistory(@Parameter(description = "历史记录ID") @PathVariable("historyId") Integer historyId) {
        return Result.dataMessageHandler(() -> historyService.getHistoryById(historyId), "获取浏览记录失败");
    }

    @Operation(summary = "创建后台资源")
    @PostMapping
    public Result<Void> createHistory(@Parameter(description = "历史记录") @RequestBody History history) {
        return Result.messageHandler(() -> historyService.createHistory(history));
    }

    @Operation(summary = "执行后台管理操作")
    @PutMapping("/{historyId}")
    public Result<Void> updateHistory(@Parameter(description = "历史记录ID") @PathVariable("historyId") Integer historyId,
                                      @Parameter(description = "历史记录") @RequestBody History history) {
        history.setHistoryId(historyId);
        return Result.messageHandler(() -> historyService.updateHistory(history));
    }

    @Operation(summary = "执行后台管理操作")
    @DeleteMapping("/{historyId}")
    public Result<Void> deleteHistory(@Parameter(description = "历史记录ID") @PathVariable("historyId") Integer historyId) {
        return Result.messageHandler(() -> historyService.deleteHistory(historyId));
    }
}
