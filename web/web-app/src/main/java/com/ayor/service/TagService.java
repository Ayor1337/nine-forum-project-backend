package com.ayor.service;

import com.ayor.entity.app.dto.TagDTO;
import com.ayor.entity.app.vo.TagVO;
import com.ayor.entity.pojo.Tag;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface TagService extends IService<Tag> {
    List<TagVO> listTags();

    List<TagVO> listTagsByTopicId(Integer topicId);

    String insertNewTag(TagDTO tagDTO);
}
