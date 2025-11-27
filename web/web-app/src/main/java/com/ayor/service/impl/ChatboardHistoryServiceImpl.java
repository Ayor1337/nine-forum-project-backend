package com.ayor.service.impl;

import com.ayor.entity.app.vo.ChatboardHistoryVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.ChatboardHistory;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ChatboardHistoryMapper;
import com.ayor.service.ChatboardHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatboardHistoryServiceImpl extends ServiceImpl<ChatboardHistoryMapper, ChatboardHistory> implements ChatboardHistoryService {

    private final AccountMapper accountMapper;

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public String insertChatboardHistory(Integer accountId,
                                         Integer topicId,
                                         String content) {
        Account account = accountMapper.getAccountById(accountId);
        if(account == null) {
            return "用户不存在";
        }
        if (content == null || content.isEmpty()) {
            return "内容不能为空";
        }
        if (content.length() > 50) {
            return "内容过长";
        }
        ChatboardHistory chatboardHistory = new ChatboardHistory(null, account.getAccountId(), topicId, content, new Date());
        if (this.baseMapper.insert(chatboardHistory) > 0) {
            simpMessagingTemplate.convertAndSend("/broadcast/topic/" + topicId, chatboardHistory);
            return null;
        }
        return "发送失败";
    }

    @Override
    public List<ChatboardHistoryVO> getChatboardHistory(Integer topicId) {
        List<ChatboardHistory> chatboardHistories = this.lambdaQuery()
                .eq(ChatboardHistory::getTopicId, topicId)
                .orderByAsc(ChatboardHistory::getCreateTime)
                .list();

        List<ChatboardHistoryVO> chatboardHistoryVOS = new ArrayList<>();
        chatboardHistories.forEach(chatboardHistory -> {
            ChatboardHistoryVO chatboardHistoryVO = new ChatboardHistoryVO();
            chatboardHistoryVO.setChatboardHistoryId(chatboardHistory.getChatboardHistoryId());
            BeanUtils.copyProperties(chatboardHistory, chatboardHistoryVO);
            chatboardHistoryVOS.add(chatboardHistoryVO);
        });

        return chatboardHistoryVOS;
    }

}
