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
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    public Result<PageEntity<TopicVO>> getTopics(@RequestParam("page_num") Integer pageNum,
                                                 @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                 @RequestParam(value = "theme_id", required = false) Integer themeId) {
        if (themeId != null) {
            return Result.dataMessageHandler(() -> topicService.getTopicsByThemeId(themeId, pageNum, pageSize), "获取话题列表失败");
        }
        return Result.dataMessageHandler(() -> topicService.getTopics(pageNum, pageSize), "获取话题列表失败");
    }

    @GetMapping("/options")
    public Result<PageEntity<TopicVO>> getTopicsAsOptions(@RequestParam("page_num") Integer pageNum,
                                                 @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> topicService.getTopics(pageNum, pageSize), "获取话题列表失败");
    }

    @PostMapping
    public Result<Void> createTopic(@RequestBody TopicDTO topicDTO) {
        return Result.messageHandler(() -> topicService.createTopic(topicDTO));
    }

    @PutMapping("/{topicId}")
    public Result<Void> updateTopic(@PathVariable("topicId") Integer topicId, @RequestBody TopicDTO topicDTO) {
        topicDTO.setTopicId(topicId);
        return Result.messageHandler(() -> topicService.updateTopic(topicDTO));
    }

    @DeleteMapping("/{topicId}")
    public Result<Void> deleteTopic(@PathVariable("topicId") Integer topicId) {
        return Result.messageHandler(() -> topicService.deleteTopic(topicId));
    }


}
