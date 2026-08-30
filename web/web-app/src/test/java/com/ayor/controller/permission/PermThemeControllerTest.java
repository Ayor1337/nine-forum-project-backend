package com.ayor.controller.permission;

import com.ayor.aspect.oplog.OperationLog;
import com.ayor.entity.dto.ThemeDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermThemeControllerTest {

    // 测试控制器暴露权限版块基础路径
    @Test
    void controllerShouldExposePermissionThemeBasePath() {
        RequestMapping mapping = PermThemeController.class.getAnnotation(RequestMapping.class);

        assertEquals("/api/perm/theme", mapping.value()[0]);
    }

    // 测试新增版块使用基础路由
    @Test
    void insertThemeShouldUseBaseRoute() throws NoSuchMethodException {
        Method method = PermThemeController.class.getMethod("insertTheme", ThemeDTO.class);

        PostMapping mapping = method.getAnnotation(PostMapping.class);

        assertEquals(0, mapping.value().length);
        OperationLog operationLog = method.getAnnotation(OperationLog.class);
        assertTrue(operationLog.save());
        assertEquals("CREATE_THEME", operationLog.action());
        assertEquals("theme", operationLog.targetType());
        assertEquals("", operationLog.targetIdParam());
    }
}
