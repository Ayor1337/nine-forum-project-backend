package com.ayor.controller;

import com.ayor.entity.dto.ThemeDTO;
import com.ayor.entity.vo.ThemeTopicVO;
import com.ayor.entity.vo.ThemeVO;
import com.ayor.result.Result;
import com.ayor.service.AuthorizationService;
import com.ayor.service.ThemeService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/themes")
public class ThemeController {

    private final ThemeService themeService;

    private final AuthorizationService authorizationService;

    private final SecurityUtils securityUtils;
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

    @PostMapping
    public Result<Void> insertTheme(@RequestBody @Validated ThemeDTO themeVO) {
        authorizationService.assertCanManageTheme(securityUtils.getSecurityUserId());
        return Result.messageHandler(() -> themeService.insertTheme(themeVO));
    }


}
