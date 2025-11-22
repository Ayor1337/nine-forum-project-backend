package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.TopicStat;
import com.ayor.result.Result;
import com.ayor.service.TopicStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/topic_stat")
@RequiredArgsConstructor
public class TopicStatController {

    private final TopicStatService topicStatService;

    @GetMapping("/list")
    public Result<PageEntity<TopicStat>> listTopicStats(@RequestParam("page_num") Integer pageNum,
                                                        @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                        @RequestParam(value = "topic_id", required = false) Integer topicId) {
        return Result.dataMessageHandler(() -> topicStatService.getTopicStats(pageNum, pageSize, topicId), "获取话题统计失败");
    }

    @PutMapping("/{stat_id}")
    public Result<Void> updateTopicStat(@PathVariable("stat_id") Integer statId,
                                        @RequestBody TopicStat topicStat) {
        return Result.messageHandler(() -> topicStatService.updateTopicStat(statId, topicStat));
    }
}
