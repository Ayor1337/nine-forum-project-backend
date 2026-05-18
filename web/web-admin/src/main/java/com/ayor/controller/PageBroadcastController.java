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

@RestController
@RequestMapping("/api/page-broadcasts")
@RequiredArgsConstructor
public class PageBroadcastController {

    private final PageBroadcastService pageBroadcastService;

    @PostMapping
    public Result<Void> createPageBroadcast(@RequestBody PageBroadcastDTO dto) {
        return Result.messageHandler(() -> pageBroadcastService.createPageBroadcast(dto));
    }

    @GetMapping
    public Result<List<PageBroadcastVO>> listPageBroadcasts() {
        return Result.dataMessageHandler(pageBroadcastService::listPageBroadcasts, "获取页面广播失败");
    }

    @DeleteMapping("/{broadcastId}")
    public Result<Void> deletePageBroadcast(@PathVariable("broadcastId") String broadcastId) {
        return Result.messageHandler(() -> pageBroadcastService.deletePageBroadcast(broadcastId));
    }
}
