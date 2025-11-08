package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.admin.dto.ThemeDTO;
import com.ayor.entity.admin.vo.ThemeVO;
import com.ayor.result.Result;
import com.ayor.service.ThemeService;
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
@RequestMapping("/api/theme")
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    @GetMapping("/get_themes")
    public Result<PageEntity<ThemeVO>> getThemes(@RequestParam(name = "page_num") Integer pageNum,
                                                 @RequestParam(name = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> themeService.getThemes(pageNum, pageSize), "获取失败");
    }

    @PostMapping
    public Result<Void> createTheme(@RequestBody ThemeDTO themeDTO) {
        return Result.messageHandler(() -> themeService.createTheme(themeDTO));
    }

    @PutMapping
    public Result<Void> updateTheme(@RequestBody ThemeDTO themeDTO) {
        return Result.messageHandler(() -> themeService.updateTheme(themeDTO));
    }

    @DeleteMapping("/{theme_id}")
    public Result<Void> deleteTheme(@PathVariable("theme_id") Integer themeId) {
        return Result.messageHandler(() -> themeService.deleteTheme(themeId));
    }

}
