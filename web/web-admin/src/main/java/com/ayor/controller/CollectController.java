package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Collect;
import com.ayor.result.Result;
import com.ayor.service.CollectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/collect")
@RequiredArgsConstructor
public class CollectController {

    private final CollectService collectService;

    @GetMapping("/list")
    public Result<PageEntity<Collect>> listCollects(@RequestParam("page_num") Integer pageNum,
                                                    @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                    @RequestParam(value = "thread_id", required = false) Integer threadId,
                                                    @RequestParam(value = "account_id", required = false) Integer accountId) {
        return Result.dataMessageHandler(() -> collectService.getCollects(pageNum, pageSize, threadId, accountId), "获取收藏记录失败");
    }

    @DeleteMapping("/{collect_id}")
    public Result<Void> deleteCollect(@PathVariable("collect_id") Integer collectId) {
        return Result.messageHandler(() -> collectService.deleteCollect(collectId));
    }
}
