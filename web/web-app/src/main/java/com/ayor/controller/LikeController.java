package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.vo.ThreadVO;
import com.ayor.result.Result;
import com.ayor.service.LikeThreadService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class LikeController {

    private final LikeThreadService likeThreadService;

    private final SecurityUtils security;
    /**
     * likeThread 方法。
     */

    @PostMapping("/threads/{thread_id}/likes")
    public Result<Void> likeThread(@PathVariable(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> likeThreadService.insertLikeThreadId(userId, threadId));
    }
    /**
     * unlikeThread 方法。
     */

    @DeleteMapping("/threads/{thread_id}/likes")
    public Result<Void> unlikeThread(@PathVariable(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> likeThreadService.removeLikeThreadId(userId, threadId));
    }
    /**
     * isLiked 方法。
     */

    @GetMapping("/threads/{thread_id}/likes/me")
    public Result<Boolean> isLiked(@PathVariable(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> likeThreadService.isLikedByAccountId(userId, threadId), "获取失败");
    }
    /**
     * getLikeCountByThreadId 方法。
     */

    @GetMapping("/threads/{thread_id}/likes/count")
    public Result<Integer> getLikeCountByThreadId(@PathVariable(name = "thread_id") Integer threadId) {
        return Result.dataMessageHandler(() -> likeThreadService.getLikeCountByThreadId(threadId), "获取失败");
    }
    /**
     * getLikes 方法。
     */

    @GetMapping("/users/{user_id}/liked-threads")
    public Result<PageEntity<ThreadVO>> getLikes(@PathVariable(name = "user_id") Integer userId,
                                                 @RequestParam(name = "page") Integer pageNum,
                                                 @RequestParam(name = "page_size") Integer pageSize) {
        return Result.dataMessageHandler(() -> likeThreadService.getLikesByAccountId(userId, pageNum, pageSize), "获取失败");
    }


}
