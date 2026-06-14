package com.ayor.controller;

import com.ayor.entity.dto.PageBroadcastDTO;
import com.ayor.entity.vo.PageBroadcastVO;
import com.ayor.result.Result;
import com.ayor.service.PageBroadcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "页面广播管理", description = "后台页面广播管理接口")
@RestController
@RequestMapping("/api/page-broadcasts")
@RequiredArgsConstructor
public class PageBroadcastController {

    private final PageBroadcastService pageBroadcastService;

    @Operation(summary = "创建后台资源")
    @PostMapping
    public Result<Void> createPageBroadcast(@Parameter(description = "请求体") @RequestBody PageBroadcastDTO dto) {
        return Result.messageHandler(() -> pageBroadcastService.createPageBroadcast(dto));
    }

    @Operation(summary = "查询后台资源")
    @GetMapping
    public Result<List<PageBroadcastVO>> listPageBroadcasts() {
        return Result.dataMessageHandler(pageBroadcastService::listPageBroadcasts, "获取页面广播失败");
    }

    @Operation(summary = "执行后台管理操作")
    @DeleteMapping("/{broadcastId}")
    public Result<Void> deletePageBroadcast(@Parameter(description = "广播ID") @PathVariable("broadcastId") String broadcastId) {
        return Result.messageHandler(() -> pageBroadcastService.deletePageBroadcast(broadcastId));
    }
}
