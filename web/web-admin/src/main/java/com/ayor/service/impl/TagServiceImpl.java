package com.ayor.service.impl;

import com.ayor.entity.pojo.Tag;
import com.ayor.mapper.TagMapper;
import com.ayor.service.TagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {


}
