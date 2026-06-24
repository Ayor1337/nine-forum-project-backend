package com.ayor.service.impl;

import com.ayor.entity.dto.ThemeDTO;
import com.ayor.entity.pojo.Theme;
import com.ayor.entity.pojo.Topic;
import com.ayor.entity.vo.ThemeTopicVO;
import com.ayor.entity.vo.ThemeVO;
import com.ayor.mapper.ThemeMapper;
import com.ayor.mapper.TopicMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThemeServiceImplTest {

    @Mock
    private ThemeMapper themeMapper;

    @Mock
    private TopicMapper topicMapper;

    // 测试获取版块列表只返回未删除版块带有主题
    @Test
    void getThemeListOnlyReturnsNonDeletedThemesWithTopics() {
        ThemeServiceImpl service = createService();
        Theme visible = theme(1, "技术", false);
        Theme empty = theme(2, "空主题", false);
        Theme deleted = theme(3, "已删除", true);
        when(themeMapper.getThemeList()).thenReturn(List.of(visible, empty, deleted));
        when(topicMapper.getCountByThemeId(1)).thenReturn(2);
        when(topicMapper.getCountByThemeId(2)).thenReturn(0);
        when(topicMapper.getCountByThemeId(3)).thenReturn(1);

        List<ThemeVO> result = service.getThemeList();

        assertThat(result).singleElement().satisfies(vo -> {
            assertThat(vo.getThemeId()).isEqualTo(1);
            assertThat(vo.getTitle()).isEqualTo("技术");
        });
    }

    // 测试新增版块拒绝空DTO并返回Mapper失败消息
    @Test
    void insertThemeRejectsNullDtoAndReturnsMapperFailureMessage() {
        ThemeServiceImpl service = createService();
        ThemeDTO dto = new ThemeDTO();
        dto.setTitle("技术");
        when(themeMapper.insert(any(Theme.class))).thenReturn(0);

        assertThat(service.insertTheme(null)).isEqualTo("请填写主题名称");
        assertThat(service.insertTheme(dto)).isEqualTo("添加失败, 未知异常");
    }

    // 测试新增版块会复制DTO且成功时返回空
    @Test
    void insertThemeCopiesDtoAndReturnsNullWhenInserted() {
        ThemeServiceImpl service = createService();
        ThemeDTO dto = new ThemeDTO();
        dto.setTitle("技术");
        when(themeMapper.insert(any(Theme.class))).thenReturn(1);

        assertThat(service.insertTheme(dto)).isNull();
        verify(themeMapper).insert(any(Theme.class));
    }

    // 测试获取版块主题列表过滤条件已删除主题
    @Test
    void getThemeTopicListFiltersDeletedTopics() {
        ThemeServiceImpl service = createService();
        Theme theme = theme(1, "技术", false);
        Topic visible = new Topic();
        visible.setTopicId(10);
        visible.setTitle("Java");
        visible.setIsDeleted(false);
        Topic deleted = new Topic();
        deleted.setTopicId(11);
        deleted.setTitle("Deleted");
        deleted.setIsDeleted(true);
        when(themeMapper.getThemeList()).thenReturn(List.of(theme));
        when(topicMapper.getTopicByThemeId(1)).thenReturn(List.of(visible, deleted));

        List<ThemeTopicVO> result = service.getThemeTopicList();

        assertThat(result).singleElement().satisfies(vo -> {
            assertThat(vo.getThemeId()).isEqualTo(1);
            assertThat(vo.getTopics()).singleElement()
                    .satisfies(topicVO -> assertThat(topicVO.getTopicId()).isEqualTo(10));
        });
    }

    private ThemeServiceImpl createService() {
        ThemeServiceImpl service = new ThemeServiceImpl(themeMapper, topicMapper);
        ReflectionTestUtils.setField(service, "baseMapper", themeMapper);
        return service;
    }

    private Theme theme(Integer themeId, String name, boolean deleted) {
        Theme theme = new Theme();
        theme.setThemeId(themeId);
        theme.setTitle(name);
        theme.setIsDeleted(deleted);
        return theme;
    }
}
