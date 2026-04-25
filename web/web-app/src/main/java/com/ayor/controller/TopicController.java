package com.ayor.controller;

import com.ayor.entity.app.dto.TopicDTO;
import com.ayor.entity.app.vo.TopicVO;
import com.ayor.result.Result;
import com.ayor.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class TopicController {

    private final TopicService topicService;
    /**
     * getTopicList 方法。
     */

    @GetMapping("/themes/{theme_id}/topics")
    public Result<List<TopicVO>> getTopicList(@PathVariable(name = "theme_id") Integer themeId) {
        return Result.dataMessageHandler(() -> topicService.getTopicListByThemeId(themeId), "获取主题下的帖子列表失败");
    }
    /**
     * insertTopic 方法。
     */

    @PreAuthorize("hasAnyRole('ROLE_OWNER')")
    @PostMapping("/topics")
    public Result<Void> insertTopic(@RequestBody TopicDTO topicDTO) {
        return Result.messageHandler(() -> topicService.insertTopic(topicDTO));
    }
    /**
     * updateTopic 方法。
     */

    @PreAuthorize("hasAnyRole('ROLE_OWNER')")
    @PutMapping("/topics/{topic_id}")
    public Result<Void> updateTopic(@PathVariable(name = "topic_id") Integer topicId,
                                    @RequestBody TopicDTO topicDTO) {
        topicDTO.setTopicId(topicId);
        return Result.messageHandler(() -> topicService.updateTopic(topicDTO));
    }
    /**
     * deleteTopic 方法。
     */

    @PreAuthorize("hasAnyRole('ROLE_OWNER')")
    @DeleteMapping("/topics/{topic_id}")
    public Result<Void> deleteTopic(@PathVariable(name = "topic_id") Integer topicId) {
        return Result.messageHandler(() -> topicService.deleteTopic(topicId));
    }

}
