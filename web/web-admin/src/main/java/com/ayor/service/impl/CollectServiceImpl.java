package com.ayor.service.impl;

import com.ayor.entity.pojo.Collect;
import com.ayor.mapper.CollectMapper;
import com.ayor.service.CollectService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements CollectService {


}

