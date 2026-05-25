package com.ayor.controller.permission;

import com.ayor.entity.dto.TopicDTO;
import com.ayor.result.Result;
import com.ayor.service.AuthorizationService;
import com.ayor.service.TopicService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/perm/topic")
public class PermTopicController {

    private final TopicService topicService;

    private final AuthorizationService authorizationService;

    private final SecurityUtils securityUtils;

    @PostMapping
    public Result<Void> insertTopic(@RequestBody TopicDTO topicDTO) {
        authorizationService.assertCanCreateTopic(securityUtils.getSecurityUserId());
        return Result.messageHandler(() -> topicService.insertTopic(topicDTO));
    }

    @PutMapping("/{topic_id}")
    public Result<Void> updateTopic(@PathVariable(name = "topic_id") Integer topicId,
                                    @RequestBody TopicDTO topicDTO) {
        authorizationService.assertCanUpdateTopic(securityUtils.getSecurityUserId(), topicId);
        topicDTO.setTopicId(topicId);
        return Result.messageHandler(() -> topicService.updateTopic(topicDTO));
    }

    @DeleteMapping("/{topic_id}")
    public Result<Void> deleteTopic(@PathVariable(name = "topic_id") Integer topicId) {
        authorizationService.assertCanDeleteTopic(securityUtils.getSecurityUserId(), topicId);
        return Result.messageHandler(() -> topicService.deleteTopic(topicId));
    }
}
