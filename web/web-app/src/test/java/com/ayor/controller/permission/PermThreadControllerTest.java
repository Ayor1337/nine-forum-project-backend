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

    @Test
    void controllerShouldExposeModerationBasePath() {
        RequestMapping mapping = PermThreadController.class.getAnnotation(RequestMapping.class);

        assertEquals("/api/perm/thread", mapping.value()[0]);
    }

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
