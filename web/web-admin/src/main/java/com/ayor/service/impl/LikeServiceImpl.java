package com.ayor.service.impl;

import com.ayor.entity.pojo.LikeThread;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.LikeMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.LikeService;
import com.ayor.util.QuillUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeServiceImpl extends ServiceImpl<LikeMapper, LikeThread> implements LikeService {

    private final AccountMapper accountMapper;

    private final ThreaddMapper threaddMapper;

    private final QuillUtils quillUtils;

}
