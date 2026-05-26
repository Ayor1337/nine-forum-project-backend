package com.ayor.controller.permission;

import com.ayor.aspect.oplog.OperationLog;
import com.ayor.entity.dto.TopicDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermTopicControllerTest {

    @Test
    void controllerShouldExposePermissionTopicBasePath() {
        RequestMapping mapping = PermTopicController.class.getAnnotation(RequestMapping.class);

        assertEquals("/api/perm/topic", mapping.value()[0]);
    }

    @Test
    void insertTopicShouldUseBaseRoute() throws NoSuchMethodException {
        Method method = PermTopicController.class.getMethod("insertTopic", TopicDTO.class);

        PostMapping mapping = method.getAnnotation(PostMapping.class);

        assertEquals(0, mapping.value().length);
        assertOperationLog(method, "CREATE_TOPIC", "topic", "");
    }

    @Test
    void updateTopicShouldUseTopicIdRoute() throws NoSuchMethodException {
        Method method = PermTopicController.class.getMethod("updateTopic", Integer.class, TopicDTO.class);

        PutMapping mapping = method.getAnnotation(PutMapping.class);

        assertEquals("/{topic_id}", mapping.value()[0]);
        assertOperationLog(method, "UPDATE_TOPIC", "topic", "topicId");
    }

    @Test
    void deleteTopicShouldUseTopicIdRoute() throws NoSuchMethodException {
        Method method = PermTopicController.class.getMethod("deleteTopic", Integer.class);

        DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);

        assertEquals("/{topic_id}", mapping.value()[0]);
        assertOperationLog(method, "DELETE_TOPIC", "topic", "topicId");
    }

    private void assertOperationLog(Method method, String action, String targetType, String targetIdParam) {
        OperationLog operationLog = method.getAnnotation(OperationLog.class);

        assertTrue(operationLog.save());
        assertEquals(action, operationLog.action());
        assertEquals(targetType, operationLog.targetType());
        assertEquals(targetIdParam, operationLog.targetIdParam());
    }
}
