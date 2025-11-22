package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Tag;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface TagService extends IService<Tag> {

    List<Tag> listTags(Integer topicId);

    String createTag(Tag tag);

    String updateTag(Tag tag);

    String deleteTag(Integer tagId);

    PageEntity<Tag> pageTags(Integer pageNum, Integer pageSize, Integer topicId);
}
