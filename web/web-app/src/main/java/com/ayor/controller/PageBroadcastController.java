package com.ayor.controller;

import com.ayor.entity.vo.PageBroadcastVO;
import com.ayor.result.Result;
import com.ayor.service.PageBroadcastQueryService;
import com.ayor.type.PageBroadcastScopeType;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/page-broadcasts")
@RequiredArgsConstructor
@Tag(name = "页面广播")
public class PageBroadcastController {

    private final PageBroadcastQueryService pageBroadcastQueryService;

    @Operation(summary = "获取当前生效的页面广播")
    @GetMapping("/active")
    public Result<List<PageBroadcastVO>> listActiveBroadcasts(@Parameter(description = "广播作用域类型") @RequestParam("scope_type") PageBroadcastScopeType scopeType,
                                                              @Parameter(description = "广播作用域 ID") @RequestParam(value = "scope_id", required = false) Integer scopeId) {
        return Result.dataMessageHandler(
                () -> pageBroadcastQueryService.listActiveBroadcasts(scopeType, scopeId),
                "获取页面广播失败");
    }
}
