package com.ayor.controller;

import com.ayor.entity.app.dto.TagDTO;
import com.ayor.entity.app.vo.TagVO;
import com.ayor.result.Result;
import com.ayor.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/topics/{topic_id}/tags")
public class TagController {

    private final TagService tagService;


    @GetMapping
    public Result<List<TagVO>> getTagList(@PathVariable(name = "topic_id") Integer topicId) {
        return Result.dataMessageHandler(() -> tagService.listTagsByTopicId(topicId), "获取失败");
    }

    @PreAuthorize("hasRole('ROLE_OWNER') " +
            "or hasAuthority('PERM_INSERT_TAG')" +
            "and hasAuthority('TOPIC_' + #topicId)")
    @PostMapping
    public Result<Void> insertNewTag(@PathVariable(name = "topic_id") Integer topicId,
                                     @RequestBody TagDTO tagDTO) {
        tagDTO.setTopicId(topicId);
        return Result.messageHandler(() -> tagService.insertNewTag(tagDTO));
    }



}
