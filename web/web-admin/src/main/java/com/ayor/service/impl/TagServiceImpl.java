package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Tag;
import com.ayor.mapper.TagMapper;
import com.ayor.service.TagService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Override
    public List<Tag> listTags(Integer topicId) {
        return this.lambdaQuery()
                .eq(topicId != null, Tag::getTopicId, topicId)
                .list();
    }

    @Override
    public PageEntity<Tag> pageTags(Integer pageNum, Integer pageSize, Integer topicId) {
        Page<Tag> page = this.lambdaQuery()
                .eq(topicId != null, Tag::getTopicId, topicId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), page.getRecords());
    }

    @Override
    public String createTag(Tag tag) {
        if (tag == null || !StringUtils.hasText(tag.getTag())) {
            return "标签名称不能为空";
        }
        if (tag.getTopicId() == null) {
            return "请选择所属话题";
        }
        if (tag.getCreateTime() == null) {
            tag.setCreateTime(new Date());
        }
        return this.save(tag) ? null : "创建标签失败";
    }

    @Override
    public String updateTag(Tag tag) {
        if (tag == null || tag.getTagId() == null) {
            return "标签不存在";
        }
        Tag exist = this.getById(tag.getTagId());
        if (exist == null) {
            return "标签不存在";
        }
        if (StringUtils.hasText(tag.getTag())) {
            exist.setTag(tag.getTag());
        }
        if (tag.getTopicId() != null) {
            exist.setTopicId(tag.getTopicId());
        }
        return this.updateById(exist) ? null : "更新标签失败";
    }

    @Override
    public String deleteTag(Integer tagId) {
        if (tagId == null) {
            return "标签不存在";
        }
        return this.removeById(tagId) ? null : "删除标签失败";
    }
}
