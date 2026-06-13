package com.ayor.controller;

import com.ayor.entity.vo.TopicVO;
import com.ayor.result.Result;
import com.ayor.service.TopicService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(name = "话题")
public class TopicController {

    private final TopicService topicService;

    /**
     * 获取指定主题下的话题列表。
     */
    @Operation(summary = "获取指定主题下的话题列表")
    @GetMapping("/themes/{theme_id}/topics")
    public Result<List<TopicVO>> getTopicList(@Parameter(description = "主题 ID") @PathVariable(name = "theme_id") Integer themeId) {
        return Result.dataMessageHandler(() -> topicService.getTopicListByThemeId(themeId), "获取主题下的帖子列表失败");
    }
}
