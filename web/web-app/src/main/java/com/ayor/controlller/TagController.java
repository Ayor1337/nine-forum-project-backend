package com.ayor.controlller;

import com.ayor.entity.app.dto.TagDTO;
import com.ayor.entity.app.vo.TagVO;
import com.ayor.result.Result;
import com.ayor.service.TagService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tag")
public class TagController {

    private final TagService tagService;


    @GetMapping("/info/list_by_topic")
    public Result<List<TagVO>> getTagList(@RequestParam(name = "topic_id") Integer topicId) {
        return Result.dataMessageHandler(() -> tagService.listTagsByTopicId(topicId), "获取失败");
    }

    @PreAuthorize("hasRole('ROLE_OWNER') " +
            "or hasAuthority('PERM_INSERT_TAG')" +
            "and hasAuthority('TOPIC_' + #tagDTO.topicId)")
    @PutMapping("/perm/insert_new_tag")
    public Result<Void> insertNewTag(@RequestBody TagDTO tagDTO) {
        return Result.messageHandler(() -> tagService.insertNewTag(tagDTO));
    }



}
