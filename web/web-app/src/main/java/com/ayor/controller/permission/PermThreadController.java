package com.ayor.controller.permission;

import com.ayor.entity.dto.TagUpdateDTO;
import com.ayor.result.Result;
import com.ayor.service.AuthorizationService;
import com.ayor.service.ThreaddService;
import com.ayor.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/moderation")
public class PermThreadController {

    private final ThreaddService threaddService;

    private final SecurityUtils security;

    private final AuthorizationService authorizationService;

    /**
     * 修改帖子标签。
     */
    @PutMapping("/threads/{thread_id}/tag")
    public Result<Void> updateTag(@PathVariable(name = "thread_id") Integer threadId,
                                  @RequestParam(name = "topic_id") Integer topicId,
                                  @Valid @RequestBody TagUpdateDTO tagUpdateDTO) {
        authorizationService.assertCanUpdateThreadTag(security.getSecurityUserId(), threadId, topicId);
        return Result.messageHandler(() -> threaddService.updateThreadTag(threadId, topicId, tagUpdateDTO.getTagId()));
    }

    /**
     * 删除帖子标签。
     */
    @DeleteMapping("/threads/{thread_id}/tag")
    public Result<Void> deleteThreadTag(@PathVariable(name = "thread_id") Integer threadId,
                                        @RequestParam(name = "topic_id") Integer topicId) {
        authorizationService.assertCanUpdateThreadTag(security.getSecurityUserId(), threadId, topicId);
        return Result.messageHandler(() -> threaddService.removeThreadTag(threadId, topicId));
    }

    /**
     * 管理员删除帖子。
     */
    @DeleteMapping("/threads/{thread_id}")
    public Result<Void> removeThreadByIdPermission(@PathVariable(name = "thread_id") Integer threadId,
                                                   @RequestParam(name = "topic_id") Integer topicId) {
        authorizationService.assertCanModerateDeleteThread(security.getSecurityUserId(), threadId, topicId);
        return Result.messageHandler(() -> threaddService.permRemoveThreadById(threadId));
    }
}
