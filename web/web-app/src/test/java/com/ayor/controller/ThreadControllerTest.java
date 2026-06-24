package com.ayor.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreadControllerTest {

    // 测试按主题获取帖子串时暴露标签ID和选中状态查询参数
    @Test
    void getThreadsByTopicIdShouldExposeTagIdAndIsSelectedQueryParams() throws NoSuchMethodException {
        Method method = ThreadController.class.getMethod(
                "getThreadsByTopicId",
                Integer.class,
                Integer.class,
                Boolean.class,
                String.class,
                Integer.class,
                Integer.class
        );

        Set<String> queryParamNames = Arrays.stream(method.getParameters())
                .map(parameter -> parameter.getAnnotation(RequestParam.class))
                .filter(annotation -> annotation != null)
                .map(this::requestParamName)
                .collect(Collectors.toSet());

        assertTrue(queryParamNames.contains("tag_id"));
        assertTrue(queryParamNames.contains("is_selected"));
    }

    // 测试主题帖子串排行暴露周期指标和分页查询参数
    @Test
    void getTopicThreadRankingsShouldExposePeriodMetricAndPagingQueryParams() throws NoSuchMethodException {
        Method method = ThreadController.class.getMethod(
                "getTopicThreadRankings",
                Integer.class,
                String.class,
                String.class,
                Integer.class,
                Integer.class
        );

        Set<String> queryParamNames = Arrays.stream(method.getParameters())
                .map(parameter -> parameter.getAnnotation(RequestParam.class))
                .filter(annotation -> annotation != null)
                .map(this::requestParamName)
                .collect(Collectors.toSet());

        assertTrue(queryParamNames.contains("period"));
        assertTrue(queryParamNames.contains("metric"));
        assertTrue(queryParamNames.contains("page_num"));
        assertTrue(queryParamNames.contains("page_size"));
    }

    // 测试全站帖子串排行暴露周期指标和分页查询参数
    @Test
    void getThreadRankingsShouldExposePeriodMetricAndPagingQueryParams() throws NoSuchMethodException {
        Method method = ThreadController.class.getMethod(
                "getThreadRankings",
                String.class,
                String.class,
                Integer.class,
                Integer.class
        );

        Set<String> queryParamNames = Arrays.stream(method.getParameters())
                .map(parameter -> parameter.getAnnotation(RequestParam.class))
                .filter(annotation -> annotation != null)
                .map(this::requestParamName)
                .collect(Collectors.toSet());

        assertTrue(queryParamNames.contains("period"));
        assertTrue(queryParamNames.contains("metric"));
        assertTrue(queryParamNames.contains("page_num"));
        assertTrue(queryParamNames.contains("page_size"));
    }

    // 测试获取全局公告暴露公开路由
    @Test
    void getGlobalAnnouncementsShouldExposePublicRoute() throws NoSuchMethodException {
        Method method = ThreadController.class.getMethod("getGlobalAnnouncements");

        GetMapping mapping = method.getAnnotation(GetMapping.class);

        assertTrue(Arrays.asList(mapping.value()).contains("/announcements/global"));
    }

    private String requestParamName(RequestParam requestParam) {
        if (!requestParam.value().isEmpty()) {
            return requestParam.value();
        }
        return requestParam.name();
    }
}
