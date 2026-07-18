package com.ayor.controller;

import com.ayor.result.Result;
import com.ayor.service.DataRepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "数据修复", description = "后台数据修复维护接口")
@RestController
@RequestMapping("/api/data_repairs")
@RequiredArgsConstructor
public class DataRepairController {

    private final DataRepairService dataRepairService;

    /**
     * 补齐账号和话题缺失的附属记录。
     */
    @Operation(summary = "补齐账号和话题缺失的附属记录")
    @PostMapping("/missing_related_records")
    public Result<Void> initializeMissingRelatedRecords() {
        return Result.messageHandler(dataRepairService::initializeMissingRelatedRecords);
    }

    /**
     * 触发搜索索引全量重建（异步执行）。
     */
    @Operation(summary = "触发搜索索引全量重建", description = "发送重建命令, 由 web-app 异步执行全量灌入与失效文档清理")
    @PostMapping("/rebuild_search_index")
    public Result<Void> rebuildSearchIndex() {
        return Result.messageHandler(dataRepairService::rebuildSearchIndex);
    }
}
