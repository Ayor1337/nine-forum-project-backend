package com.ayor.service.impl;

import com.ayor.entity.pojo.Tag;
import com.ayor.entity.vo.TagVO;
import com.ayor.mapper.TagMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagMapper tagMapper;

    private TagServiceImpl tagService;

    @BeforeEach
    void setUp() {
        tagService = new TagServiceImpl();
        ReflectionTestUtils.setField(tagService, "baseMapper", tagMapper);
    }

    // 测试按主题ID查询标签时拒绝空主题ID
    @Test
    void listTagsByTopicIdRejectsNullTopicId() {
        assertThat(tagService.listTagsByTopicId(null)).isNull();
        verify(tagMapper, never()).getTagByTopicId(org.mockito.ArgumentMatchers.anyInt());
    }

    // 测试列表标签按主题 ID 映射标签实体到 VO
    @Test
    void listTagsByTopicIdMapsTagEntitiesToVos() {
        Tag tag = new Tag();
        tag.setTagId(5);
        tag.setTopicId(9);
        tag.setTag("java");
        when(tagMapper.getTagByTopicId(9)).thenReturn(List.of(tag));

        List<TagVO> result = tagService.listTagsByTopicId(9);

        assertThat(result).singleElement().satisfies(vo -> {
            assertThat(vo.getTagId()).isEqualTo(5);
            assertThat(vo.getTopicId()).isEqualTo(9);
            assertThat(vo.getTag()).isEqualTo("java");
        });
    }
}
