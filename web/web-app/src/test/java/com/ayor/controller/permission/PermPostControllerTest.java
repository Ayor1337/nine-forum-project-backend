package com.ayor.controller.permission;

import com.ayor.aspect.oplog.OperationLog;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermPostControllerTest {

    // 测试控制器暴露管理基础路径
    @Test
    void controllerShouldExposeModerationBasePath() {
        RequestMapping mapping = PermPostController.class.getAnnotation(RequestMapping.class);

        assertEquals("/api/perm/post", mapping.value()[0]);
    }

    // 测试删除帖子权限保持原始路由
    @Test
    void deletePostPermissionShouldKeepOriginalRoute() throws NoSuchMethodException {
        Method method = PermPostController.class.getMethod(
                "deletePostPermission",
                Integer.class
        );

        DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);

        assertEquals("/{post_id}", mapping.value()[0]);
        OperationLog operationLog = method.getAnnotation(OperationLog.class);
        assertTrue(operationLog.save());
        assertEquals("DELETE_POST", operationLog.action());
        assertEquals("post", operationLog.targetType());
        assertEquals("postId", operationLog.targetIdParam());
    }

    // 测试列表编辑历史带有快照暴露管理历史路由
    @Test
    void listEditHistoryWithSnapshotsShouldExposeModerationHistoryRoute() throws NoSuchMethodException {
        Method method = PermPostController.class.getMethod(
                "listEditHistoryWithSnapshots",
                Integer.class
        );

        GetMapping mapping = method.getAnnotation(GetMapping.class);

        assertEquals("/{post_id}/edit-history", mapping.value()[0]);
    }
}
