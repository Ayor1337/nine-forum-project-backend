package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.TopicDTO;
import com.ayor.entity.vo.TopicVO;
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

    /**
     * 分页查询话题列表。
     */
    @GetMapping
    public Result<PageEntity<TopicVO>> getTopics(@RequestParam("page_num") Integer pageNum,
                                                 @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                 @RequestParam(value = "theme_id", required = false) Integer themeId) {
        if (themeId != null) {
            return Result.dataMessageHandler(() -> topicService.getTopicsByThemeId(themeId, pageNum, pageSize), "获取话题列表失败");
        }
        return Result.dataMessageHandler(() -> topicService.getTopics(pageNum, pageSize), "获取话题列表失败");
    }

    /**
     * 获取话题下拉选项。
     */
    @GetMapping("/options")
    public Result<PageEntity<TopicVO>> getTopicsAsOptions(@RequestParam("page_num") Integer pageNum,
                                                 @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> topicService.getTopics(pageNum, pageSize), "获取话题列表失败");
    }

    /**
     * 创建话题。
     */
    @PostMapping
    public Result<Void> createTopic(@RequestBody TopicDTO topicDTO) {
        return Result.messageHandler(() -> topicService.createTopic(topicDTO));
    }

    /**
     * 更新指定话题。
     */
    @PutMapping("/{topicId}")
    public Result<Void> updateTopic(@PathVariable("topicId") Integer topicId, @RequestBody TopicDTO topicDTO) {
        topicDTO.setTopicId(topicId);
        return Result.messageHandler(() -> topicService.updateTopic(topicDTO));
    }

    /**
     * 删除指定话题。
     */
    @DeleteMapping("/{topicId}")
    public Result<Void> deleteTopic(@PathVariable("topicId") Integer topicId) {
        return Result.messageHandler(() -> topicService.deleteTopic(topicId));
    }


}
