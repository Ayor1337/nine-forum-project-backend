package com.ayor.service.impl;

import com.ayor.entity.app.dto.ThemeDTO;
import com.ayor.entity.app.vo.ThemeTopicVO;
import com.ayor.entity.app.vo.ThemeVO;
import com.ayor.entity.app.vo.TopicVO;
import com.ayor.entity.pojo.Theme;
import com.ayor.entity.pojo.Topic;
import com.ayor.mapper.ThemeMapper;
import com.ayor.mapper.TopicMapper;
import com.ayor.service.ThemeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ThemeServiceImpl extends ServiceImpl<ThemeMapper, Theme> implements ThemeService {

    @Resource
    private ThemeMapper themeMapper;

    @Resource
    private TopicMapper topicMapper;

    @Override
    public List<ThemeVO> getThemeList() {
        List<Theme> themes = themeMapper.getThemeList();
        List<ThemeVO> themeVOList = new ArrayList<>();
        themes.forEach(theme -> {

            if (topicMapper.getCountByThemeId(theme.getThemeId()) > 0
            && !theme.getIsDeleted()) {
                ThemeVO themeVO = new ThemeVO();
                BeanUtils.copyProperties(theme, themeVO);
                themeVOList.add(themeVO);
            }

        });

        return themeVOList;
    }

    @Override
    public String insertTheme(ThemeDTO themeDTO) {
        if (themeDTO == null) {
            return "请填写主题名称";
        }
        Theme theme = new Theme();
        BeanUtils.copyProperties(themeDTO, theme);
        return themeMapper.insert(theme) > 0 ? null : "添加失败, 未知异常";
    }

    @Override
    public List<ThemeTopicVO> getThemeTopicList() {
        List<Theme> themeList = themeMapper.getThemeList();
        List<ThemeTopicVO> themeTopicVOList = new ArrayList<>();

        themeList.forEach(theme -> {
            ThemeTopicVO themeTopicVO = new ThemeTopicVO();
            BeanUtils.copyProperties(theme, themeTopicVO);
            themeTopicVOList.add(themeTopicVO);
        });

        themeTopicVOList.forEach(themeTopicVO -> {
            Integer themeId = themeTopicVO.getThemeId();
            List<Topic> topicList = topicMapper.getTopicByThemeId(themeId);
            List<TopicVO> topicVOList = new ArrayList<>();
            topicList.forEach(topic -> {
                if (!topic.getIsDeleted()) {
                    TopicVO topicVO = new TopicVO();
                    BeanUtils.copyProperties(topic, topicVO);
                    topicVOList.add(topicVO);
                }
            });
            themeTopicVO.setTopics(topicVOList);
        });

        return themeTopicVOList;
    }

}
