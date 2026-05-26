package com.ayor.controller.permission;

import com.ayor.aspect.oplog.OperationLog;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermPostControllerTest {

    @Test
    void controllerShouldExposeModerationBasePath() {
        RequestMapping mapping = PermPostController.class.getAnnotation(RequestMapping.class);

        assertEquals("/api/perm/post", mapping.value()[0]);
    }

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
}
