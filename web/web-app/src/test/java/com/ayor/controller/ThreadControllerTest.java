package com.ayor.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreadControllerTest {

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
                .map(RequestParam::value)
                .collect(Collectors.toSet());

        assertTrue(queryParamNames.contains("tagId"));
        assertTrue(queryParamNames.contains("isSelected"));
    }

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
                .map(RequestParam::value)
                .collect(Collectors.toSet());

        assertTrue(queryParamNames.contains("period"));
        assertTrue(queryParamNames.contains("metric"));
        assertTrue(queryParamNames.contains("page_num"));
        assertTrue(queryParamNames.contains("page_size"));
    }

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
                .map(RequestParam::value)
                .collect(Collectors.toSet());

        assertTrue(queryParamNames.contains("period"));
        assertTrue(queryParamNames.contains("metric"));
        assertTrue(queryParamNames.contains("page_num"));
        assertTrue(queryParamNames.contains("page_size"));
    }
}
