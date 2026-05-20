package com.ayor.service.impl;

import com.ayor.entity.dto.TagDTO;
import com.ayor.entity.vo.TagVO;
import com.ayor.entity.pojo.Tag;
import com.ayor.mapper.TagMapper;
import com.ayor.service.TagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
    /**
     * 获取所有标签列表。
     */


    @Override
    public List<TagVO> listTags() {
        List<Tag> tags = this.list();
        List<TagVO> tagVOList = new ArrayList<>();
        tags.forEach(tag -> {
            TagVO tagVO = new TagVO();
            BeanUtils.copyProperties(tag, tagVO);
            tagVOList.add(tagVO);
        });
        return tagVOList;
    }
    /**
     * 获取指定主题下已关联的标签列表。
     */

    @Override
    public List<TagVO> listTagsByTopicId(Integer topicId) {
        if (topicId == null) {
            return null;
        }
        List<Tag> tags = this.baseMapper.getTagByTopicId(topicId);
        List<TagVO> tagVOList = new ArrayList<>();
        tags.forEach(tag -> {
            TagVO tagVO = new TagVO();
            BeanUtils.copyProperties(tag, tagVO);
            tagVOList.add(tagVO);
        });
        return tagVOList;
    }
    /**
     * 新增一个主题标签，并保存标签与主题的关联。
     */

    @Override
    public String insertNewTag(TagDTO tagDTO) {
        boolean tagAlreadyExist = this.lambdaQuery().eq(Tag::getTag, tagDTO.getTag())
                .eq(Tag::getTopicId, tagDTO.getTopicId()).exists();
        if (tagAlreadyExist) {
            return "标签已存在";
        }
        Tag tag = new Tag();
        BeanUtils.copyProperties(tagDTO, tag);
        tag.setCreateTime(new Date());
        this.save(tag);
        return null;
    }



}
