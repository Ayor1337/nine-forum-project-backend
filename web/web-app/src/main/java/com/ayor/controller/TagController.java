package com.ayor.controller;

import com.ayor.entity.vo.TagVO;
import com.ayor.result.Result;
import com.ayor.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/topics/{topic_id}/tags")
public class TagController {

    private final TagService tagService;

    /**
     * 获取指定主题下的话题标签列表。
     *
     * @param topicId 主题 ID
     * @return 标签列表
     */


    @GetMapping
    public Result<List<TagVO>> getTagByTopicId(@PathVariable(name = "topic_id") Integer topicId) {
        return Result.dataMessageHandler(() -> tagService.listTagsByTopicId(topicId), "获取失败");
    }

}
