package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.admin.dto.TopicDTO;
import com.ayor.entity.admin.vo.TopicVO;
import com.ayor.result.Result;
import com.ayor.service.TopicService;
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
@RequestMapping("/api/topic")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping("/get_topics_by_theme_id")
    public Result<PageEntity<TopicVO>> getTopicsByTheme(@RequestParam("theme_id") Integer themeId,
                                                @RequestParam("page_num") Integer pageNum,
                                                @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> topicService.getTopicsByThemeId(themeId, pageNum, pageSize), "获取话题列表失败");
    }

    @GetMapping("/list")
    public Result<PageEntity<TopicVO>> getTopics(@RequestParam("page_num") Integer pageNum,
                                                 @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> topicService.getTopics(pageNum, pageSize), "获取话题列表失败");
    }

    @GetMapping("/list_options")
    public Result<PageEntity<TopicVO>> getTopicsAsOptions(@RequestParam("page_num") Integer pageNum,
                                                 @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> topicService.getTopics(pageNum, pageSize), "获取话题列表失败");
    }

    @PostMapping
    public Result<Void> createTopic(@RequestBody TopicDTO topicDTO) {
        return Result.messageHandler(() -> topicService.createTopic(topicDTO));
    }

    @PutMapping
    public Result<Void> updateTopic(@RequestBody TopicDTO topicDTO) {
        return Result.messageHandler(() -> topicService.updateTopic(topicDTO));
    }

    @DeleteMapping("/{topic_id}")
    public Result<Void> deleteTopic(@PathVariable("topic_id") Integer topicId) {
        return Result.messageHandler(() -> topicService.deleteTopic(topicId));
    }


}
