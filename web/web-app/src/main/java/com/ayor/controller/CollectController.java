package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.vo.ThreadVO;
import com.ayor.result.Result;
import com.ayor.service.CollectService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class    CollectController {

    private final CollectService collectService;

    private final SecurityUtils security;
    /**
     * 收藏帖子。
     */

    @PostMapping("/threads/{thread_id}/collections")
    public Result<Void> collectThread(@PathVariable(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> collectService.insertCollect(userId, threadId));
    }
    /**
     * 取消收藏帖子。
     */

    @DeleteMapping("/threads/{thread_id}/collections")
    public Result<Void> uncollectThread(@PathVariable(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> collectService.removeCollect(userId, threadId));
    }
    /**
     * 判断当前用户是否收藏了指定帖子。
     */

    @GetMapping("/threads/{thread_id}/collections/me")
    public Result<Boolean> isCollected(@PathVariable(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> collectService.isCollectedByAccountId(userId, threadId), "获取失败");
    }
    /**
     * 获取帖子收藏数。
     */

    @GetMapping("/threads/{thread_id}/collections/count")
    public Result<Integer> getCollectCountByThreadId(@PathVariable(name = "thread_id") Integer threadId) {
        return Result.dataMessageHandler(() -> collectService.getCollectCountByThreadId(threadId), "获取失败");
    }
    /**
     * 获取当前用户的收藏列表。
     */

    @GetMapping("/users/{user_id}/collected-threads")
    public Result<PageEntity<ThreadVO>> getCollects(@PathVariable(name = "user_id") Integer userId,
                                                    @RequestParam(name = "page") Integer pageNum,
                                                    @RequestParam(name = "page_size") Integer pageSize) {
        return Result.dataMessageHandler(() -> collectService.getCollectsByAccountId(userId, pageNum, pageSize), "获取失败");
    }




}
