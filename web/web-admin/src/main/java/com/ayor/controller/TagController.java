package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Tag;
import com.ayor.entity.vo.TagVO;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理", description = "后台标签管理接口")
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 查询标签列表，支持按话题过滤；传入分页参数时返回分页结果。
     */
    @Operation(summary = "查询标签列表，支持按话题过滤；传入分页参数时返回分页结果")
    @GetMapping
    public Result<?> listTags(@Parameter(description = "话题ID") @RequestParam(value = "topic_id", required = false) Integer topicId,
                              @Parameter(description = "页码") @RequestParam(value = "page_num", required = false) Integer pageNum,
                              @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize) {
        if (pageNum != null) {
            return Result.dataMessageHandler(() -> tagService.pageTags(pageNum, pageSize, topicId), "分页查询标签失败");
        }
        return Result.dataMessageHandler(() -> tagService.listTags(topicId), "获取标签列表失败");
    }

    /**
     * 查询单个标签详情。
     */
    @Operation(summary = "查询单个标签详情")
    @GetMapping("/{tagId}")
    public Result<TagVO> getTag(@Parameter(description = "标签ID") @PathVariable("tagId") Integer tagId) {
        return Result.dataMessageHandler(() -> tagService.getTagById(tagId), "获取标签失败");
    }

    /**
     * 创建标签。
     */
    @Operation(summary = "创建标签")
    @PostMapping
    public Result<Void> createTag(@Parameter(description = "标签信息") @RequestBody Tag tag) {
        return Result.messageHandler(() -> tagService.createTag(tag));
    }

    /**
     * 更新指定标签。
     */
    @Operation(summary = "更新指定标签")
    @PutMapping("/{tagId}")
    public Result<Void> updateTag(@Parameter(description = "标签ID") @PathVariable("tagId") Integer tagId, @RequestBody Tag tag) {
        tag.setTagId(tagId);
        return Result.messageHandler(() -> tagService.updateTag(tag));
    }

    /**
     * 删除指定标签。
     */
    @Operation(summary = "删除指定标签")
    @DeleteMapping("/{tagId}")
    public Result<Void> deleteTag(@Parameter(description = "标签ID") @PathVariable("tagId") Integer tagId) {
        return Result.messageHandler(() -> tagService.deleteTag(tagId));
    }
}
