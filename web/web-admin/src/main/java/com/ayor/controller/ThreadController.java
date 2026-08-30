package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.ThreadDTO;
import com.ayor.entity.pojo.Threadd;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "帖子管理", description = "后台帖子管理接口")
@RestController
@RequestMapping("/api/threads")
@RequiredArgsConstructor
public class ThreadController {

    private final ThreaddService threaddService;

    /**
     * 分页查询帖子列表。
     */
    @Operation(summary = "分页查询帖子列表")
    @GetMapping
    public Result<PageEntity<ThreadTableVO>> getThreads(@Parameter(description = "页码") @RequestParam("page_num") Integer pageNum,
                                                        @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                        @Parameter(description = "话题ID") @RequestParam(value = "topic_id", required = false) Integer topicId) {
        return Result.dataMessageHandler(() -> threaddService.getThreads(topicId, pageNum, pageSize), "获取帖子列表失败");
    }

    /**
     * 查询单个帖子详情。
     */
    @Operation(summary = "查询单个帖子详情")
    @GetMapping("/{threadId}")
    public Result<Threadd> getThread(@Parameter(description = "帖子ID") @PathVariable("threadId") Integer threadId) {
        return Result.dataMessageHandler(() -> threaddService.getThreadById(threadId), "获取帖子失败");
    }

    /**
     * 创建一条新帖子。
     */
    @Operation(summary = "创建一条新帖子")
    @PostMapping
    public Result<Void> createThread(@Parameter(description = "帖子信息") @RequestBody ThreadDTO threadDTO) {
        return Result.messageHandler(() -> threaddService.createThread(threadDTO));
    }

    /**
     * 更新指定帖子的内容。
     */
    @Operation(summary = "更新指定帖子的内容")
    @PutMapping("/{threadId}")
    public Result<Void> updateThread(@Parameter(description = "帖子ID") @PathVariable("threadId") Integer threadId, @RequestBody ThreadDTO threadDTO) {
        threadDTO.setThreadId(threadId);
        return Result.messageHandler(() -> threaddService.updateThread(threadDTO));
    }

    /**
     * 删除指定帖子。
     */
    @Operation(summary = "删除指定帖子")
    @DeleteMapping("/{threadId}")
    public Result<Void> deleteThread(@Parameter(description = "帖子ID") @PathVariable("threadId") Integer threadId) {
        return Result.messageHandler(() -> threaddService.deleteThread(threadId));
    }

}
