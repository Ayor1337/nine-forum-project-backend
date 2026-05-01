package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.ThemeDTO;
import com.ayor.entity.vo.ThemeVO;
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
@RequestMapping("/api/themes")
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    /**
     * 分页查询主题列表。
     */
    @GetMapping
    public Result<PageEntity<ThemeVO>> getThemes(@RequestParam(name = "page_num") Integer pageNum,
                                                 @RequestParam(name = "page_size", defaultValue = "10") Integer pageSize) {
        return Result.dataMessageHandler(() -> themeService.getThemes(pageNum, pageSize), "获取失败");
    }

    /**
     * 查询指定主题。
     */
    @GetMapping("/{themeId}")
    public Result<ThemeVO> getTheme(@PathVariable("themeId") Integer themeId) {
        return Result.dataMessageHandler(() -> themeService.getThemeById(themeId), "主题不存在");
    }

    /**
     * 创建主题。
     */
    @PostMapping
    public Result<Void> createTheme(@RequestBody ThemeDTO themeDTO) {
        return Result.messageHandler(() -> themeService.createTheme(themeDTO));
    }

    /**
     * 更新指定主题。
     */
    @PutMapping("/{themeId}")
    public Result<Void> updateTheme(@PathVariable("themeId") Integer themeId, @RequestBody ThemeDTO themeDTO) {
        themeDTO.setThemeId(themeId);
        return Result.messageHandler(() -> themeService.updateTheme(themeDTO));
    }

    /**
     * 删除指定主题。
     */
    @DeleteMapping("/{themeId}")
    public Result<Void> deleteTheme(@PathVariable("themeId") Integer themeId) {
        return Result.messageHandler(() -> themeService.deleteTheme(themeId));
    }

}
