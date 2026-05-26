package com.ayor.controller.permission;

import com.ayor.aspect.oplog.OperationLog;
import com.ayor.entity.dto.ThemeDTO;
import com.ayor.result.Result;
import com.ayor.service.AuthorizationService;
import com.ayor.service.ThemeService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/perm/theme")
public class PermThemeController {

    private final ThemeService themeService;

    private final AuthorizationService authorizationService;

    private final SecurityUtils securityUtils;

    @OperationLog(value = "新增主题", save = true, action = "CREATE_THEME", targetType = "theme")
    @PostMapping
    public Result<Void> insertTheme(@RequestBody @Validated ThemeDTO themeDTO) {
        authorizationService.assertCanCreateTheme(securityUtils.getSecurityUserId());
        return Result.messageHandler(() -> themeService.insertTheme(themeDTO));
    }
}
