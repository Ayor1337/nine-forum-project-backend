package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.ThreadVO;
import com.ayor.result.Result;
import com.ayor.service.LikeThreadService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(name = "点赞")
public class LikeController {

    private final LikeThreadService likeThreadService;

    private final SecurityUtils security;
    /**
     * 点赞帖子。
     */
    @Operation(summary = "点赞帖子")
    @PostMapping("/threads/{thread_id}/likes")
    public Result<Void> likeThread(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> likeThreadService.insertLikeThreadId(userId, threadId));
    }
    /**
     * 取消点赞帖子。
     */
    @Operation(summary = "取消点赞帖子")
    @DeleteMapping("/threads/{thread_id}/likes")
    public Result<Void> unlikeThread(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> likeThreadService.removeLikeThreadId(userId, threadId));
    }
    /**
     * 判断当前用户是否点赞了指定帖子。
     */
    @Operation(summary = "判断当前用户是否点赞指定帖子")
    @GetMapping("/threads/{thread_id}/likes/me")
    public Result<Boolean> isLiked(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> likeThreadService.isLikedByAccountId(userId, threadId), "获取失败");
    }
    /**
     * 获取帖子点赞数。
     */
    @Operation(summary = "获取帖子点赞数")
    @GetMapping("/threads/{thread_id}/likes/count")
    public Result<Integer> getLikeCountByThreadId(@Parameter(description = "帖子 ID") @PathVariable(name = "thread_id") Integer threadId) {
        return Result.dataMessageHandler(() -> likeThreadService.getLikeCountByThreadId(threadId), "获取失败");
    }
    /**
     * 获取指定用户的点赞列表，受隐私设置约束。
     */
    @Operation(summary = "获取指定用户的点赞列表")
    @GetMapping("/users/{user_id}/liked-threads")
    public Result<PageEntity<ThreadVO>> getLikes(@Parameter(description = "用户 ID") @PathVariable(name = "user_id") Integer userId,
                                                 @Parameter(description = "页码") @RequestParam(name = "page") Integer pageNum,
                                                 @Parameter(description = "每页数量") @RequestParam(name = "page_size") Integer pageSize) {
        Integer viewerId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> likeThreadService.getLikesByAccountId(viewerId, userId, pageNum, pageSize), "获取失败");
    }


}
