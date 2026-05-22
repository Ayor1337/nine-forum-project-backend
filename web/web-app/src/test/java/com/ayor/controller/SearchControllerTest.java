package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.UserSearchVO;
import com.ayor.result.Result;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchControllerTest {

    @Test
    void searchUserShouldExposeQueryAndPagingParams() throws NoSuchMethodException {
        Method method = SearchController.class.getMethod(
                "searchUser",
                String.class,
                int.class,
                int.class
        );

        Set<String> queryParamNames = Arrays.stream(method.getParameters())
                .map(parameter -> parameter.getAnnotation(RequestParam.class))
                .filter(annotation -> annotation != null)
                .map(RequestParam::name)
                .collect(Collectors.toSet());

        assertTrue(queryParamNames.contains("query"));
        assertTrue(queryParamNames.contains("page_num"));
        assertTrue(queryParamNames.contains("page_size"));
    }

    @Test
    void searchUserShouldReturnPagedUserSearchResult() throws NoSuchMethodException {
        Method method = SearchController.class.getMethod(
                "searchUser",
                String.class,
                int.class,
                int.class
        );

        ParameterizedType resultType = (ParameterizedType) method.getGenericReturnType();
        assertEquals(Result.class, resultType.getRawType());

        ParameterizedType pageType = (ParameterizedType) resultType.getActualTypeArguments()[0];
        assertEquals(PageEntity.class, pageType.getRawType());

        Type userType = pageType.getActualTypeArguments()[0];
        assertEquals(UserSearchVO.class, userType);
    }
}
