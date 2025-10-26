package com.ayor.controlller;

import com.ayor.entity.app.vo.ThreadPageVO;
import com.ayor.entity.app.vo.ThreadVO;
import com.ayor.result.Result;
import com.ayor.service.LikeService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/like")
public class LikeController {

    private final LikeService likeService;

    private final SecurityUtils security;

    @PostMapping("/like_thread")
    public Result<Void> likeThread(@RequestParam(name = "thread_id") Integer threadId) {
        String username = security.getSecurityUsername();
        return Result.messageHandler(() -> likeService.insertLikeThreadId(username, threadId));
    }

    @PostMapping("/unlike_thread")
    public Result<Void> unlikeThread(@RequestParam(name = "thread_id") Integer threadId) {
        String username = security.getSecurityUsername();
        return Result.messageHandler(() -> likeService.removeLikeThreadId(username, threadId));
    }

    @GetMapping("/is_like")
    public Result<Boolean> isLiked(@RequestParam(name = "thread_id") Integer threadId) {
        String username = security.getSecurityUsername();
        return Result.dataMessageHandler(() -> likeService.isLikedByUsername(username, threadId), "获取失败");
    }

    @GetMapping("/get_like_count")
    public Result<Integer> getLikeCountByThreadId(@RequestParam(name = "thread_id") Integer threadId) {
        return Result.dataMessageHandler(() -> likeService.getLikeCountByThreadId(threadId), "获取失败");
    }

    @GetMapping("/get_likes")
    public Result<ThreadPageVO> getLikes(@RequestParam(name = "user_id") Integer userId,
                                         @RequestParam(name = "page") Integer pageNum,
                                         @RequestParam(name = "page_size") Integer pageSize) {
        return Result.dataMessageHandler(() -> likeService.getLikesByAccountId(userId, pageNum, pageSize), "获取失败");
    }


}