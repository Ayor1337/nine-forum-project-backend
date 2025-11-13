package com.ayor.controlller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.vo.ThreadVO;
import com.ayor.result.Result;
import com.ayor.service.CollectService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/collect")
public class    CollectController {

    private final CollectService collectService;

    private final SecurityUtils security;

    @PostMapping("/collect_thread")
    public Result<Void> collectThread(@RequestParam(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> collectService.insertCollect(userId, threadId));
    }

    @PostMapping("/uncollect_thread")
    public Result<Void> uncollectThread(@RequestParam(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> collectService.removeCollect(userId, threadId));
    }

    @GetMapping("/info/is_collect")
    public Result<Boolean> isCollected(@RequestParam(name = "thread_id") Integer threadId) {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> collectService.isCollectedByAccountId(userId, threadId), "获取失败");
    }

    @GetMapping("/info/get_collect_count")
    public Result<Integer> getCollectCountByThreadId(@RequestParam(name = "thread_id") Integer threadId) {
        return Result.dataMessageHandler(() -> collectService.getCollectCountByThreadId(threadId), "获取失败");
    }

    @GetMapping("/get_collects")
    public Result<PageEntity<ThreadVO>> getCollects(@RequestParam(name = "user_id") Integer userId,
                                                    @RequestParam(name = "page") Integer pageNum,
                                                    @RequestParam(name = "page_size") Integer pageSize) {
        return Result.dataMessageHandler(() -> collectService.getCollectsByAccountId(userId, pageNum, pageSize), "获取失败");
    }




}
