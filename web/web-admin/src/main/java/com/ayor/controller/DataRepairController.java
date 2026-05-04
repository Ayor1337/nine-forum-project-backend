package com.ayor.controller;

import com.ayor.result.Result;
import com.ayor.service.DataRepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data_repairs")
@RequiredArgsConstructor
public class DataRepairController {

    private final DataRepairService dataRepairService;

    /**
     * 补齐账号和话题缺失的附属记录。
     */
    @PostMapping("/missing_related_records")
    public Result<Void> initializeMissingRelatedRecords() {
        return Result.messageHandler(dataRepairService::initializeMissingRelatedRecords);
    }
}
