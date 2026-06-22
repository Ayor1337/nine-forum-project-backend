package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.ThreadVO;
import com.ayor.result.Result;
import com.ayor.service.CollectService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(name = "收藏")
public class    CollectController {

    private final CollectService collectService;

    private final SecurityUtils security;
    /**
     * 收藏帖子。
     */
    @Operation(summary = "收藏帖子")
    @PostMapping("/threads/{thread_id}/collections")
    public Result<Void> collectThread(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> collectService.insertCollect(userId, threadId));
    }
    /**
     * 取消收藏帖子。
     */
    @Operation(summary = "取消收藏帖子")
    @DeleteMapping("/threads/{thread_id}/collections")
    public Result<Void> uncollectThread(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> collectService.removeCollect(userId, threadId));
    }
    /**
     * 判断当前用户是否收藏了指定帖子。
     */
    @Operation(summary = "判断当前用户是否收藏指定帖子")
    @GetMapping("/threads/{thread_id}/collections/me")
    public Result<Boolean> isCollected(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> collectService.isCollectedByAccountId(userId, threadId), "获取失败");
    }
    /**
     * 获取帖子收藏数。
     */
    @Operation(summary = "获取帖子收藏数")
    @GetMapping("/threads/{thread_id}/collections/count")
    public Result<Integer> getCollectCountByThreadId(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId) {
        return Result.dataMessageHandler(() -> collectService.getCollectCountByThreadId(threadId), "获取失败");
    }
    /**
     * 获取指定用户的收藏列表，受隐私设置约束。
     */
    @Operation(summary = "获取指定用户的收藏列表")
    @GetMapping("/users/{user_id}/collected-threads")
    public Result<PageEntity<ThreadVO>> getCollects(@Parameter(description = "用户 ID") @PathVariable(name = "user_id") Integer userId,
                                                    @Parameter(description = "页码") @RequestParam(name = "page") Integer pageNum,
                                                    @Parameter(description = "每页数量") @RequestParam(name = "page_size") Integer pageSize) {
        Integer viewerId = security.getOptionalSecurityUserId();
        return Result.dataMessageHandler(() -> collectService.getCollectsByAccountId(viewerId, userId, pageNum, pageSize), "获取失败");
    }




}
