package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Tag;
import com.ayor.result.Result;
import com.ayor.service.TagService;
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
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public Result<?> listTags(@RequestParam(value = "topic_id", required = false) Integer topicId,
                              @RequestParam(value = "page_num", required = false) Integer pageNum,
                              @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        if (pageNum != null) {
            return Result.dataMessageHandler(() -> tagService.pageTags(pageNum, pageSize, topicId), "分页查询标签失败");
        }
        return Result.dataMessageHandler(() -> tagService.listTags(topicId), "获取标签列表失败");
    }

    @PostMapping
    public Result<Void> createTag(@RequestBody Tag tag) {
        return Result.messageHandler(() -> tagService.createTag(tag));
    }

    @PutMapping("/{tagId}")
    public Result<Void> updateTag(@PathVariable("tagId") Integer tagId, @RequestBody Tag tag) {
        tag.setTagId(tagId);
        return Result.messageHandler(() -> tagService.updateTag(tag));
    }

    @DeleteMapping("/{tagId}")
    public Result<Void> deleteTag(@PathVariable("tagId") Integer tagId) {
        return Result.messageHandler(() -> tagService.deleteTag(tagId));
    }
}
