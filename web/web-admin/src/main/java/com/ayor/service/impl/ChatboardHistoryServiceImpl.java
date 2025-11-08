package com.ayor.service.impl;

import com.ayor.entity.pojo.ChatboardHistory;
import com.ayor.mapper.ChatboardHistoryMapper;
import com.ayor.service.ChatboardHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatboardHistoryServiceImpl extends ServiceImpl<ChatboardHistoryMapper, ChatboardHistory> implements ChatboardHistoryService {


}
