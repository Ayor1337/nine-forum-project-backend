package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.TopicDTO;
import com.ayor.entity.vo.TopicVO;
import com.ayor.result.Result;
import com.ayor.service.TopicService;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "话题管理", description = "后台话题管理接口")
@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    /**
     * 分页查询话题列表。
     */
    @Operation(summary = "分页查询话题列表")
    @GetMapping
    public Result<PageEntity<TopicVO>> getTopics(@Parameter(description = "页码") @RequestParam("page_num") Integer pageNum,
                                                 @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10") Integer pageSize,
                                                 @Parameter(description = "主题ID") @RequestParam(value = "theme_id", required = false) Integer themeId) {
        if (themeId != null) {
            return Result.dataMessageHandler(() -> topicService.getTopicsByThemeId(themeId, pageNum, pageSize), "获取话题列表失败");
        }
        return Result.dataMessageHandler(() -> topicService.getTopics(pageNum, pageSize), "获取话题列表失败");
    }

    /**
     * 获取话题下拉选项。
     */
    @Operation(summary = "获取话题下拉选项")
    @GetMapping("/options")
    public Result<List<TopicVO>> getTopicsAsOptions(@Parameter(description = "搜索关键字") @RequestParam(value = "query", required = false) String query) {
        return Result.dataMessageHandler(() -> topicService.getTopicsAsOptions(query), "获取话题列表失败");
    }

    /**
     * 查询单个话题详情。
     */
    @Operation(summary = "查询单个话题详情")
    @GetMapping("/{topicId}")
    public Result<TopicVO> getTopic(@Parameter(description = "话题ID") @PathVariable("topicId") Integer topicId) {
        return Result.dataMessageHandler(() -> topicService.getTopicById(topicId), "获取话题失败");
    }

    /**
     * 创建话题。
     */
    @Operation(summary = "创建话题")
    @PostMapping
    public Result<Void> createTopic(@Parameter(description = "话题信息") @RequestBody @Valid TopicDTO topicDTO) {
        return Result.messageHandler(() -> topicService.createTopic(topicDTO));
    }

    /**
     * 更新指定话题。
     */
    @Operation(summary = "更新指定话题")
    @PutMapping("/{topicId}")
    public Result<Void> updateTopic(@Parameter(description = "话题ID") @PathVariable("topicId") Integer topicId, @RequestBody @Valid TopicDTO topicDTO) {
        topicDTO.setTopicId(topicId);
        return Result.messageHandler(() -> topicService.updateTopic(topicDTO));
    }

    /**
     * 删除指定话题。
     */
    @Operation(summary = "删除指定话题")
    @DeleteMapping("/{topicId}")
    public Result<Void> deleteTopic(@Parameter(description = "话题ID") @PathVariable("topicId") Integer topicId) {
        return Result.messageHandler(() -> topicService.deleteTopic(topicId));
    }


}
