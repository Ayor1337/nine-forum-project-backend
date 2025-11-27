package com.ayor.controlller;

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
@RequestMapping("/api/theme")
public class ThemeController {

    private final ThemeService themeService;

    @GetMapping("/info/list")
    public Result<List<ThemeVO>> getThemeList() {
        return Result.dataMessageHandler(themeService::getThemeList, "获取列表失败");
    }

    @GetMapping("/info/list_themes_contains_topics")
    public Result<List<ThemeTopicVO>> getThemesContainsTopics() {
        return Result.dataMessageHandler(themeService::getThemeTopicList, "获取列表失败");
    }

    @PreAuthorize("hasAnyRole('ROLE_OWNER')")
    @PutMapping("/insert")
    public Result<Void> insertTheme(@RequestBody @Validated ThemeDTO themeVO) {
        return Result.messageHandler(() -> themeService.insertTheme(themeVO));
    }


}