package com.ayor.controller;

import com.ayor.entity.vo.ThreadBreadcrumbVO;
import com.ayor.result.Result;
import com.ayor.service.ThreaddService;
import com.ayor.service.TopicService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BreadControllerTest {

    // 测试帖子面包屑返回帖子名称和主题名称
    @Test
    void getThreadInfoShouldReturnThreadAndTopicNames() {
        TopicService topicService = mock(TopicService.class);
        ThreaddService threaddService = mock(ThreaddService.class);
        ThreadBreadcrumbVO breadcrumb = new ThreadBreadcrumbVO("帖子标题", "主题标题");
        when(threaddService.getThreadBreadcrumbById(101)).thenReturn(breadcrumb);
        BreadController controller = new BreadController(topicService, threaddService);

        Result<ThreadBreadcrumbVO> result = controller.getThreadInfo(101);

        assertEquals("帖子标题", result.getData().getThreadName());
        assertEquals("主题标题", result.getData().getTopicName());
    }
}
