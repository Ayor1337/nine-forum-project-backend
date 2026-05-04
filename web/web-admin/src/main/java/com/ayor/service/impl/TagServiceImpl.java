package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Tag;
import com.ayor.entity.vo.TagVO;
import com.ayor.mapper.TagMapper;
import com.ayor.service.TagService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    /**
     * 列出某个话题下的全部标签；话题 ID 为空时返回全部标签。
     */
    @Override
    public List<TagVO> listTags(Integer topicId) {
        return toVOList(this.lambdaQuery()
                .eq(topicId != null, Tag::getTopicId, topicId)
        .list());
    }

    @Override
    public TagVO getTagById(Integer tagId) {
        if (tagId == null) {
            return null;
        }
        return toVO(this.getById(tagId));
    }

    /**
     * 分页查询标签，并支持按话题过滤。
     */
    @Override
    public PageEntity<TagVO> pageTags(Integer pageNum, Integer pageSize, Integer topicId) {
        Page<Tag> page = this.lambdaQuery()
                .eq(topicId != null, Tag::getTopicId, topicId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVOList(page.getRecords()));
    }

    /**
     * 创建标签时补齐创建时间并校验所属话题是否存在。
     */
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

    /**
     * 更新标签名称或所属话题。
     */
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

    /**
     * 删除指定标签。
     */
    @Override
    public String deleteTag(Integer tagId) {
        if (tagId == null) {
            return "标签不存在";
        }
        return this.removeById(tagId) ? null : "删除标签失败";
    }

    private List<TagVO> toVOList(List<Tag> tags) {
        List<TagVO> tagVOS = new ArrayList<>();
        for (Tag tag : tags) {
            tagVOS.add(toVO(tag));
        }
        return tagVOS;
    }

    private TagVO toVO(Tag tag) {
        if (tag == null) {
            return null;
        }
        TagVO tagVO = new TagVO();
        BeanUtils.copyProperties(tag, tagVO);
        return tagVO;
    }
}
