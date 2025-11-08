package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.admin.vo.TopicVO;
import com.ayor.result.Result;
import com.ayor.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/topic")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping("/get_topics_by_theme_id")
    public Result<PageEntity<TopicVO>> getTopics(@RequestParam("theme_id") Integer themeId,
                                                @RequestParam("page_num") Integer pageNum,
                                                @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> topicService.getTopicsByThemeId(themeId, pageNum, pageSize), "获取话题列表失败");
    }


}
