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

import java.util.List;

@RestController
@RequestMapping("/api/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/list")
    public Result<List<Tag>> listTags(@RequestParam(value = "topic_id", required = false) Integer topicId) {
        return Result.dataMessageHandler(() -> tagService.listTags(topicId), "获取标签列表失败");
    }

    @GetMapping("/page")
    public Result<PageEntity<Tag>> pageTags(@RequestParam("page_num") Integer pageNum,
                                            @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                            @RequestParam(value = "topic_id", required = false) Integer topicId) {
        return Result.dataMessageHandler(() -> tagService.pageTags(pageNum, pageSize, topicId), "分页查询标签失败");
    }

    @PostMapping
    public Result<Void> createTag(@RequestBody Tag tag) {
        return Result.messageHandler(() -> tagService.createTag(tag));
    }

    @PutMapping("/{tag_id}")
    public Result<Void> updateTag(@PathVariable("tag_id") Integer tagId, @RequestBody Tag tag) {
        tag.setTagId(tagId);
        return Result.messageHandler(() -> tagService.updateTag(tag));
    }

    @DeleteMapping("/{tag_id}")
    public Result<Void> deleteTag(@PathVariable("tag_id") Integer tagId) {
        return Result.messageHandler(() -> tagService.deleteTag(tagId));
    }
}
