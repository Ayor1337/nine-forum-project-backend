package com.ayor.service.impl;

import com.ayor.entity.pojo.Tag;
import com.ayor.entity.vo.TagVO;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

class TagServiceImplTest {

    @Test
    void shouldReturnTagWhenTagExists() {
        TagServiceImpl service = new TagServiceImpl() {
            @Override
            public Tag getById(Serializable id) {
                return new Tag(7, "Spring", null, 3);
            }
        };

        TagVO result = service.getTagById(7);

        assertNotNull(result);
        assertEquals("Spring", result.getTag());
    }
}
