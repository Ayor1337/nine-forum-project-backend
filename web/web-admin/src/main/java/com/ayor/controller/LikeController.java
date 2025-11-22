package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.LikeThread;
import com.ayor.result.Result;
import com.ayor.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @GetMapping("/list")
    public Result<PageEntity<LikeThread>> listLikes(@RequestParam("page_num") Integer pageNum,
                                                    @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                    @RequestParam(value = "thread_id", required = false) Integer threadId,
                                                    @RequestParam(value = "account_id", required = false) Integer accountId) {
        return Result.dataMessageHandler(() -> likeService.getLikes(pageNum, pageSize, threadId, accountId), "获取点赞记录失败");
    }

    @DeleteMapping("/{like_id}")
    public Result<Void> deleteLike(@PathVariable("like_id") Integer likeId) {
        return Result.messageHandler(() -> likeService.deleteLike(likeId));
    }
}
