package com.ayor.service.impl;

import com.ayor.entity.pojo.Theme;
import com.ayor.entity.vo.ThemeVO;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ThemeServiceImplTest {

    @Test
    void shouldReturnThemeVoWhenThemeExists() {
        ThemeServiceImpl service = new ThemeServiceImpl() {
            @Override
            public Theme getById(Serializable id) {
                return new Theme(1, "公告主题", true);
            }
        };

        ThemeVO result = service.getThemeById(1);

        assertNotNull(result);
        assertEquals(1, result.getThemeId());
        assertEquals("公告主题", result.getTitle());
        assertTrue(result.getIsDeleted());
    }

    @Test
    void shouldReturnNullWhenThemeDoesNotExist() {
        ThemeServiceImpl service = new ThemeServiceImpl() {
            @Override
            public Theme getById(Serializable id) {
                return null;
            }
        };

        ThemeVO result = service.getThemeById(999);

        assertNull(result);
    }

    @Test
    void shouldEvictThemeCachesAfterMutations() throws NoSuchMethodException {
        assertThemeCacheEvict(ThemeServiceImpl.class.getMethod("createTheme", com.ayor.entity.dto.ThemeDTO.class));
        assertThemeCacheEvict(ThemeServiceImpl.class.getMethod("updateTheme", com.ayor.entity.dto.ThemeDTO.class));
        assertThemeCacheEvict(ThemeServiceImpl.class.getMethod("deleteTheme", Integer.class));
    }

    private void assertThemeCacheEvict(Method method) {
        Caching caching = method.getAnnotation(Caching.class);
        assertNotNull(caching, method.getName() + " 应声明缓存失效");

        Set<String> cacheNames = Arrays.stream(caching.evict())
                .flatMap(cacheEvict -> Arrays.stream(cacheEvict.value()))
                .collect(Collectors.toSet());

        assertEquals(Set.of("themeList", "themeTopicList"), cacheNames);

        Set<String> cacheKeys = Arrays.stream(caching.evict())
                .map(CacheEvict::key)
                .collect(Collectors.toSet());

        assertEquals(Set.of("'all'"), cacheKeys);
    }
}
