package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.ThreadDTO;
import com.ayor.entity.vo.ThreadTableVO;
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
@RequestMapping("/api/threads")
@RequiredArgsConstructor
public class ThreadController {

    private final ThreaddService threaddService;

    /**
     * 分页查询帖子列表。
     */
    @GetMapping
    public Result<PageEntity<ThreadTableVO>> getThreads(@RequestParam("page_num") Integer pageNum,
                                                        @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> threaddService.getThreads(pageNum, pageSize), "获取帖子列表失败");
    }

    /**
     * 创建一条新帖子。
     */
    @PostMapping
    public Result<Void> createThread(@RequestBody ThreadDTO threadDTO) {
        return Result.messageHandler(() -> threaddService.createThread(threadDTO));
    }

    /**
     * 更新指定帖子的内容。
     */
    @PutMapping("/{threadId}")
    public Result<Void> updateThread(@PathVariable("threadId") Integer threadId, @RequestBody ThreadDTO threadDTO) {
        threadDTO.setThreadId(threadId);
        return Result.messageHandler(() -> threaddService.updateThread(threadDTO));
    }

    /**
     * 删除指定帖子。
     */
    @DeleteMapping("/{threadId}")
    public Result<Void> deleteThread(@PathVariable("threadId") Integer threadId) {
        return Result.messageHandler(() -> threaddService.deleteThread(threadId));
    }

}
