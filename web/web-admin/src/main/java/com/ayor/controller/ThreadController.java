package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.admin.dto.ThreadDTO;
import com.ayor.entity.admin.vo.ThreadTableVO;
import com.ayor.result.Result;
import com.ayor.service.ThreaddService;
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
@RequestMapping("/api/thread")
@RequiredArgsConstructor
public class ThreadController {

    private final ThreaddService threaddService;

    @GetMapping("get_threads")
    public Result<PageEntity<ThreadTableVO>> getThreads(@RequestParam("page_num") Integer pageNum,
                                                        @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> threaddService.getThreads(pageNum, pageSize), "获取帖子列表失败");
    }

    @PostMapping
    public Result<Void> createThread(@RequestBody ThreadDTO threadDTO) {
        return Result.messageHandler(() -> threaddService.createThread(threadDTO));
    }

    @PutMapping
    public Result<Void> updateThread(@RequestBody ThreadDTO threadDTO) {
        return Result.messageHandler(() -> threaddService.updateThread(threadDTO));
    }

    @DeleteMapping("/{thread_id}")
    public Result<Void> deleteThread(@PathVariable("thread_id") Integer threadId) {
        return Result.messageHandler(() -> threaddService.deleteThread(threadId));
    }

}
