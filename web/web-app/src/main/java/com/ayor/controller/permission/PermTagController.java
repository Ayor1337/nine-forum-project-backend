package com.ayor.controller.permission;

import com.ayor.aspect.oplog.OperationLog;
import com.ayor.entity.dto.TagDTO;
import com.ayor.result.Result;
import com.ayor.service.AuthorizationService;
import com.ayor.service.TagService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/perm/topic/{topic_id}/tag")
public class PermTagController {

    private final TagService tagService;

    private final AuthorizationService authorizationService;

    private final SecurityUtils securityUtils;

    @OperationLog(value = "新增话题标签", save = true, action = "CREATE_TAG", targetType = "tag", targetIdParam = "topicId")
    @PostMapping
    public Result<Void> insertNewTag(@PathVariable(name = "topic_id") Integer topicId,
                                     @RequestBody TagDTO tagDTO) {
        authorizationService.assertCanCreateTag(securityUtils.getSecurityUserId(), topicId);
        tagDTO.setTopicId(topicId);
        return Result.messageHandler(() -> tagService.insertNewTag(tagDTO));
    }
}
