package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.vo.ChatboardHistoryVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.ChatboardHistory;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ChatboardHistoryMapper;
import com.ayor.service.ChatboardHistoryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
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
    public PageEntity<ChatboardHistoryVO> getChatboardHistory(Integer topicId, Integer pageNum, Integer pageSize) {
        Page<ChatboardHistory> page = this.lambdaQuery()
                .eq(ChatboardHistory::getTopicId, topicId)
                .orderByDesc(ChatboardHistory::getCreateTime)
                .page(Page.of(pageNum, pageSize));
        List<ChatboardHistory> records = page.getRecords();

        if (records == null || records.isEmpty()) {
            return new PageEntity<>(0L, new ArrayList<>());
        }

        List<ChatboardHistoryVO> chatboardHistoryVOS = new ArrayList<>();
        records.forEach(chatboardHistory -> {
            ChatboardHistoryVO chatboardHistoryVO = new ChatboardHistoryVO();
            chatboardHistoryVO.setChatboardHistoryId(chatboardHistory.getChatboardHistoryId());
            Account account = accountMapper.getAccountById(chatboardHistory.getAccountId());
            chatboardHistoryVO.setNickname(account.getNickname());
            chatboardHistoryVO.setAvatarUrl(account.getAvatarUrl());
            chatboardHistoryVO.setBannerUrl(account.getBannerUrl());
            BeanUtils.copyProperties(chatboardHistory, chatboardHistoryVO);
            chatboardHistoryVOS.add(chatboardHistoryVO);
        });

        return new PageEntity<>(page.getTotal(), chatboardHistoryVOS);
    }

}
