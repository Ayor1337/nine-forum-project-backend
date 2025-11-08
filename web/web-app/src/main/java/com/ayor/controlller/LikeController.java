package com.ayor.controlller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.vo.ThreadVO;
import com.ayor.result.Result;
import com.ayor.service.LikeThreadService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/like")
public class LikeController {

    private final LikeThreadService likeThreadService;

    private final SecurityUtils security;

    @PostMapping("/like_thread")
    public Result<Void> likeThread(@RequestParam(name = "thread_id") Integer threadId) {
        String username = security.getSecurityUsername();
        return Result.messageHandler(() -> likeThreadService.insertLikeThreadId(username, threadId));
    }

    @PostMapping("/unlike_thread")
    public Result<Void> unlikeThread(@RequestParam(name = "thread_id") Integer threadId) {
        String username = security.getSecurityUsername();
        return Result.messageHandler(() -> likeThreadService.removeLikeThreadId(username, threadId));
    }

    @GetMapping("/is_like")
    public Result<Boolean> isLiked(@RequestParam(name = "thread_id") Integer threadId) {
        String username = security.getSecurityUsername();
        return Result.dataMessageHandler(() -> likeThreadService.isLikedByUsername(username, threadId), "获取失败");
    }

    @GetMapping("/get_like_count")
    public Result<Integer> getLikeCountByThreadId(@RequestParam(name = "thread_id") Integer threadId) {
        return Result.dataMessageHandler(() -> likeThreadService.getLikeCountByThreadId(threadId), "获取失败");
    }

    @GetMapping("/get_likes")
    public Result<PageEntity<ThreadVO>> getLikes(@RequestParam(name = "user_id") Integer userId,
                                                 @RequestParam(name = "page") Integer pageNum,
                                                 @RequestParam(name = "page_size") Integer pageSize) {
        return Result.dataMessageHandler(() -> likeThreadService.getLikesByAccountId(userId, pageNum, pageSize), "获取失败");
    }


}