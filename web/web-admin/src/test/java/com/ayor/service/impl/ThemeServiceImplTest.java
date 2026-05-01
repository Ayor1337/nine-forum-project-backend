package com.ayor.service.impl;

import com.ayor.entity.pojo.Theme;
import com.ayor.entity.vo.ThemeVO;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

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
}
