package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.admin.vo.ThemeVO;
import com.ayor.result.Result;
import com.ayor.service.ThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/theme")
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    @GetMapping("/get_themes")
    public Result<PageEntity<ThemeVO>> getThemes(@RequestParam(name = "page_num") Integer pageNum,
                                                 @RequestParam(name = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> themeService.getThemes(pageNum, pageSize), "获取失败");
    }

}
