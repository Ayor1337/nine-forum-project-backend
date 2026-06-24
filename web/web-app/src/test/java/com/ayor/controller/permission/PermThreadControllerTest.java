package com.ayor.controller.permission;

import com.ayor.aspect.oplog.OperationLog;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermThreadControllerTest {

    // 测试控制器暴露管理基础路径
    @Test
    void controllerShouldExposeModerationBasePath() {
        RequestMapping mapping = PermThreadController.class.getAnnotation(RequestMapping.class);

        assertEquals("/api/perm/thread", mapping.value()[0]);
    }

    // 测试更新标签保持原始路由并查询参数
    @Test
    void updateTagShouldKeepOriginalRouteAndQueryParam() throws NoSuchMethodException {
        Method method = PermThreadController.class.getMethod(
                "updateTag",
                Integer.class,
                Integer.class,
                com.ayor.entity.dto.TagUpdateDTO.class
        );

        PutMapping mapping = method.getAnnotation(PutMapping.class);

        assertEquals("/{thread_id}/tag", mapping.value()[0]);
        assertTrue(queryParamNames(method).contains("topic_id"));
        assertOperationLog(method, "UPDATE_THREAD_TAG", "thread", "threadId");
    }

    // 测试删除帖子串标签保持原始路由并查询参数
    @Test
    void deleteThreadTagShouldKeepOriginalRouteAndQueryParam() throws NoSuchMethodException {
        Method method = PermThreadController.class.getMethod(
                "deleteThreadTag",
                Integer.class,
                Integer.class
        );

        DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);

        assertEquals("/{thread_id}/tag", mapping.value()[0]);
        assertTrue(queryParamNames(method).contains("topic_id"));
        assertOperationLog(method, "DELETE_THREAD_TAG", "thread", "threadId");
    }

    // 测试移除帖子串按 ID 权限保持原始路由并查询参数
    @Test
    void removeThreadByIdPermissionShouldKeepOriginalRouteAndQueryParam() throws NoSuchMethodException {
        Method method = PermThreadController.class.getMethod(
                "removeThreadByIdPermission",
                Integer.class,
                Integer.class
        );

        DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);

        assertEquals("/{thread_id}", mapping.value()[0]);
        assertTrue(queryParamNames(method).contains("topic_id"));
        assertOperationLog(method, "DELETE_THREAD", "thread", "threadId");
    }

    // 测试设置公告暴露权限路由并查询参数
    @Test
    void setAnnouncementShouldExposePermissionRouteAndQueryParam() throws NoSuchMethodException {
        Method method = PermThreadController.class.getMethod(
                "setAnnouncement",
                Integer.class,
                Integer.class
        );

        PutMapping mapping = method.getAnnotation(PutMapping.class);

        assertEquals("/{thread_id}/announcement", mapping.value()[0]);
        assertTrue(queryParamNames(method).contains("topic_id"));
        assertOperationLog(method, "SET_ANNOUNCEMENT", "thread", "threadId");
    }

    // 测试取消设置公告暴露权限路由并查询参数
    @Test
    void unsetAnnouncementShouldExposePermissionRouteAndQueryParam() throws NoSuchMethodException {
        Method method = PermThreadController.class.getMethod(
                "unsetAnnouncement",
                Integer.class,
                Integer.class
        );

        DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);

        assertEquals("/{thread_id}/announcement", mapping.value()[0]);
        assertTrue(queryParamNames(method).contains("topic_id"));
        assertOperationLog(method, "UNSET_ANNOUNCEMENT", "thread", "threadId");
    }

    // 测试设置全局公告暴露权限路由
    @Test
    void setGlobalAnnouncementShouldExposePermissionRoute() throws NoSuchMethodException {
        Method method = PermThreadController.class.getMethod(
                "setGlobalAnnouncement",
                Integer.class
        );

        PutMapping mapping = method.getAnnotation(PutMapping.class);

        assertEquals("/{thread_id}/global-announcement", mapping.value()[0]);
        assertOperationLog(method, "SET_GLOBAL_ANNOUNCEMENT", "thread", "threadId");
    }

    // 测试取消设置全局公告暴露权限路由
    @Test
    void unsetGlobalAnnouncementShouldExposePermissionRoute() throws NoSuchMethodException {
        Method method = PermThreadController.class.getMethod(
                "unsetGlobalAnnouncement",
                Integer.class
        );

        DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);

        assertEquals("/{thread_id}/global-announcement", mapping.value()[0]);
        assertOperationLog(method, "UNSET_GLOBAL_ANNOUNCEMENT", "thread", "threadId");
    }

    private void assertOperationLog(Method method, String action, String targetType, String targetIdParam) {
        OperationLog operationLog = method.getAnnotation(OperationLog.class);

        assertTrue(operationLog.save());
        assertEquals(action, operationLog.action());
        assertEquals(targetType, operationLog.targetType());
        assertEquals(targetIdParam, operationLog.targetIdParam());
    }

    private Set<String> queryParamNames(Method method) {
        return Arrays.stream(method.getParameters())
                .map(parameter -> parameter.getAnnotation(RequestParam.class))
                .filter(annotation -> annotation != null)
                .map(this::requestParamName)
                .collect(Collectors.toSet());
    }

    private String requestParamName(RequestParam requestParam) {
        if (!requestParam.value().isEmpty()) {
            return requestParam.value();
        }
        return requestParam.name();
    }
}
