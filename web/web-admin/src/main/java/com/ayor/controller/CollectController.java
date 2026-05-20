package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Collect;
import com.ayor.entity.vo.CollectVO;
import com.ayor.result.Result;
import com.ayor.service.CollectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/collects")
@RequiredArgsConstructor
public class CollectController {

    private final CollectService collectService;

    /**
     * 分页查询收藏记录，可按帖子或用户过滤。
     */
    @GetMapping
    public Result<PageEntity<CollectVO>> listCollects(@RequestParam("page_num") Integer pageNum,
                                                      @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                      @RequestParam(value = "thread_id", required = false) Integer threadId,
                                                      @RequestParam(value = "account_id", required = false) Integer accountId) {
        return Result.dataMessageHandler(() -> collectService.getCollects(pageNum, pageSize, threadId, accountId), "获取收藏记录失败");
    }

    /**
     * 查询单条收藏记录。
     */
    @GetMapping("/{collectId}")
    public Result<CollectVO> getCollect(@PathVariable("collectId") Integer collectId) {
        return Result.dataMessageHandler(() -> collectService.getCollectById(collectId), "获取收藏记录失败");
    }

    /**
     * 创建收藏记录。
     */
    @PostMapping
    public Result<Void> createCollect(@RequestBody Collect collect) {
        return Result.messageHandler(() -> collectService.createCollect(collect));
    }

    /**
     * 更新收藏记录。
     */
    @PutMapping("/{collectId}")
    public Result<Void> updateCollect(@PathVariable("collectId") Integer collectId,
                                      @RequestBody Collect collect) {
        collect.setCollectId(collectId);
        return Result.messageHandler(() -> collectService.updateCollect(collect));
    }

    /**
     * 删除指定收藏记录。
     */
    @DeleteMapping("/{collectId}")
    public Result<Void> deleteCollect(@PathVariable("collectId") Integer collectId) {
        return Result.messageHandler(() -> collectService.deleteCollect(collectId));
    }
}
