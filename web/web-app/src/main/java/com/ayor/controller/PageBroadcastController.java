package com.ayor.controller;

import com.ayor.entity.vo.PageBroadcastVO;
import com.ayor.result.Result;
import com.ayor.service.PageBroadcastQueryService;
import com.ayor.type.PageBroadcastScopeType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/page-broadcasts")
@RequiredArgsConstructor
public class PageBroadcastController {

    private final PageBroadcastQueryService pageBroadcastQueryService;

    @GetMapping("/active")
    public Result<List<PageBroadcastVO>> listActiveBroadcasts(@RequestParam("scope_type") PageBroadcastScopeType scopeType,
                                                              @RequestParam(value = "scope_id", required = false) Integer scopeId) {
        return Result.dataMessageHandler(
                () -> pageBroadcastQueryService.listActiveBroadcasts(scopeType, scopeId),
                "获取页面广播失败");
    }
}
