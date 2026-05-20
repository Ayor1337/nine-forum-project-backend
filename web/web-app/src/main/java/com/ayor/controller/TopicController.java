package com.ayor.controller;

import com.ayor.entity.dto.TopicDTO;
import com.ayor.entity.vo.TopicVO;
import com.ayor.result.Result;
import com.ayor.service.AuthorizationService;
import com.ayor.service.TopicService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class TopicController {

    private final TopicService topicService;

    private final AuthorizationService authorizationService;

    private final SecurityUtils securityUtils;
    /**
     * 获取指定主题下的话题列表。
     */

    @GetMapping("/themes/{theme_id}/topics")
    public Result<List<TopicVO>> getTopicList(@PathVariable(name = "theme_id") Integer themeId) {
        return Result.dataMessageHandler(() -> topicService.getTopicListByThemeId(themeId), "获取主题下的帖子列表失败");
    }
    /**
     * 新建话题。
     */

    @PostMapping("/topics")
    public Result<Void> insertTopic(@RequestBody TopicDTO topicDTO) {
        authorizationService.assertCanManageTopic(securityUtils.getSecurityUserId());
        return Result.messageHandler(() -> topicService.insertTopic(topicDTO));
    }
    /**
     * 更新话题信息。
     */

    @PutMapping("/topics/{topic_id}")
    public Result<Void> updateTopic(@PathVariable(name = "topic_id") Integer topicId,
                                    @RequestBody TopicDTO topicDTO) {
        authorizationService.assertCanManageTopic(securityUtils.getSecurityUserId());
        topicDTO.setTopicId(topicId);
        return Result.messageHandler(() -> topicService.updateTopic(topicDTO));
    }
    /**
     * 删除话题。
     */

    @DeleteMapping("/topics/{topic_id}")
    public Result<Void> deleteTopic(@PathVariable(name = "topic_id") Integer topicId) {
        authorizationService.assertCanManageTopic(securityUtils.getSecurityUserId());
        return Result.messageHandler(() -> topicService.deleteTopic(topicId));
    }

}
