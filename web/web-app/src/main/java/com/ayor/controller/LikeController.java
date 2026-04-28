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
     * 点赞帖子。
     */

    @PostMapping("/threads/{thread_id}/likes")
    public Result<Void> likeThread(@PathVariable(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> likeThreadService.insertLikeThreadId(userId, threadId));
    }
    /**
     * 取消点赞帖子。
     */

    @DeleteMapping("/threads/{thread_id}/likes")
    public Result<Void> unlikeThread(@PathVariable(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> likeThreadService.removeLikeThreadId(userId, threadId));
    }
    /**
     * 判断当前用户是否点赞了指定帖子。
     */

    @GetMapping("/threads/{thread_id}/likes/me")
    public Result<Boolean> isLiked(@PathVariable(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> likeThreadService.isLikedByAccountId(userId, threadId), "获取失败");
    }
    /**
     * 获取帖子点赞数。
     */

    @GetMapping("/threads/{thread_id}/likes/count")
    public Result<Integer> getLikeCountByThreadId(@PathVariable(name = "thread_id") Integer threadId) {
        return Result.dataMessageHandler(() -> likeThreadService.getLikeCountByThreadId(threadId), "获取失败");
    }
    /**
     * 获取指定用户的点赞列表，受隐私设置约束。
     *
     * @param userId 目标用户ID
     * @param pageNum 当前页码
     * @param pageSize 每页记录数
     * @return 点赞列表分页结果
     */
    @GetMapping("/users/{user_id}/liked-threads")
    public Result<PageEntity<ThreadVO>> getLikes(@PathVariable(name = "user_id") Integer userId,
                                                 @RequestParam(name = "page") Integer pageNum,
                                                 @RequestParam(name = "page_size") Integer pageSize) {
        Integer viewerId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> likeThreadService.getLikesByAccountId(viewerId, userId, pageNum, pageSize), "获取失败");
    }


}
