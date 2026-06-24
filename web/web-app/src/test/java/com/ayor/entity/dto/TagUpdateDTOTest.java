package com.ayor.entity.dto;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TagUpdateDTOTest {

    // 测试请求体只包含标签ID
    @Test
    void shouldOnlyContainTagIdFromRequestBody() {
        Set<String> fieldNames = Arrays.stream(TagUpdateDTO.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertEquals(Set.of("tagId"), fieldNames);
    }
}
