package com.ayor.controller;

import com.ayor.entity.vo.ThemeTopicVO;
import com.ayor.entity.vo.ThemeVO;
import com.ayor.result.Result;
import com.ayor.service.ThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/themes")
public class ThemeController {

    private final ThemeService themeService;

    /**
     * 获取全部主题列表。
     */

    @GetMapping
    public Result<List<ThemeVO>> getThemeList() {
        return Result.dataMessageHandler(themeService::getThemeList, "获取列表失败");
    }
    /**
     * 获取包含话题的主题聚合列表。
     */

    @GetMapping("/topics")
    public Result<List<ThemeTopicVO>> getThemesContainsTopics() {
        return Result.dataMessageHandler(themeService::getThemeTopicList, "获取列表失败");
    }
}
