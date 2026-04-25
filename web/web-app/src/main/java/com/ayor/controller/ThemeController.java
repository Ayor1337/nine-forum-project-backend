package com.ayor.controller;

import com.ayor.entity.app.dto.ThemeDTO;
import com.ayor.entity.app.vo.ThemeTopicVO;
import com.ayor.entity.app.vo.ThemeVO;
import com.ayor.result.Result;
import com.ayor.service.ThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
    /**
     * 新增主题。
     */

    @PreAuthorize("hasAnyRole('ROLE_OWNER')")
    @PostMapping
    public Result<Void> insertTheme(@RequestBody @Validated ThemeDTO themeVO) {
        return Result.messageHandler(() -> themeService.insertTheme(themeVO));
    }


}
