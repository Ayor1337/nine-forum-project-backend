package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.TopicStat;
import com.ayor.entity.vo.TopicStatVO;
import com.ayor.result.Result;
import com.ayor.service.TopicStatService;
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
@RequestMapping("/api/topic_stats")
@RequiredArgsConstructor
public class TopicStatController {

    private final TopicStatService topicStatService;

    /**
     * 分页查询话题统计记录，可按话题过滤。
     */
    @GetMapping
    public Result<PageEntity<TopicStatVO>> listTopicStats(@RequestParam("page_num") Integer pageNum,
                                                          @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                          @RequestParam(value = "topic_id", required = false) Integer topicId) {
        return Result.dataMessageHandler(() -> topicStatService.getTopicStats(pageNum, pageSize, topicId), "获取话题统计失败");
    }

    /**
     * 查询单条话题统计记录。
     */
    @GetMapping("/{statId}")
    public Result<TopicStatVO> getTopicStat(@PathVariable("statId") Integer statId) {
        return Result.dataMessageHandler(() -> topicStatService.getTopicStatById(statId), "获取话题统计失败");
    }

    /**
     * 创建话题统计记录。
     */
    @PostMapping
    public Result<Void> createTopicStat(@RequestBody TopicStat topicStat) {
        return Result.messageHandler(() -> topicStatService.createTopicStat(topicStat));
    }

    /**
     * 更新指定话题统计记录。
     */
    @PutMapping("/{statId}")
    public Result<Void> updateTopicStat(@PathVariable("statId") Integer statId,
                                        @RequestBody TopicStat topicStat) {
        return Result.messageHandler(() -> topicStatService.updateTopicStat(statId, topicStat));
    }

    /**
     * 删除指定话题统计记录。
     */
    @DeleteMapping("/{statId}")
    public Result<Void> deleteTopicStat(@PathVariable("statId") Integer statId) {
        return Result.messageHandler(() -> topicStatService.deleteTopicStat(statId));
    }
}
