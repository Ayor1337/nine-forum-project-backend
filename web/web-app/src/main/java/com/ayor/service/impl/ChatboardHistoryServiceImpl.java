package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.ChatboardHistoryVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.ChatboardHistory;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ChatboardHistoryMapper;
import com.ayor.service.ChatboardHistoryService;
import com.ayor.service.UserRelationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

/**
 * 聊天记录服务实现
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ChatboardHistoryServiceImpl extends ServiceImpl<ChatboardHistoryMapper, ChatboardHistory> implements ChatboardHistoryService {

    private final AccountMapper accountMapper;

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final UserRelationService userRelationService;
    /**
     * 保存聊天室消息到聊天记录中。
     */

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
            simpMessagingTemplate.convertAndSend("/broadcast/topic/" + topicId, toVO(chatboardHistory, account));
            return null;
        }
        return "发送失败";
    }
    /**
     * 分页查询指定主题的聊天室历史消息。
     */

    @Override
    public PageEntity<ChatboardHistoryVO> getChatboardHistory(Integer accountId, Integer topicId, Integer pageNum, Integer pageSize) {
        List<Integer> blockedAccountIds = accountId == null
                ? List.of()
                : userRelationService.listBlockedAccountIdsEitherDirection(accountId);
        LambdaQueryWrapper<ChatboardHistory> wrapper = new LambdaQueryWrapper<ChatboardHistory>()
                .eq(ChatboardHistory::getTopicId, topicId)
                .notIn(blockedAccountIds != null && !blockedAccountIds.isEmpty(), ChatboardHistory::getAccountId, blockedAccountIds)
                .orderByDesc(ChatboardHistory::getCreateTime);
        Page<ChatboardHistory> page = this.baseMapper.selectPage(Page.of(pageNum, pageSize), wrapper);
        List<ChatboardHistory> records = page.getRecords();

        if (records == null || records.isEmpty()) {
            return new PageEntity<>(0L, new ArrayList<>());
        }

        List<ChatboardHistoryVO> chatboardHistoryVOS = new ArrayList<>();
        records.forEach(chatboardHistory -> {
            Account account = accountMapper.getAccountById(chatboardHistory.getAccountId());
            chatboardHistoryVOS.add(toVO(chatboardHistory, account));
        });

        return new PageEntity<>(page.getTotal(), chatboardHistoryVOS);
    }

    private ChatboardHistoryVO toVO(ChatboardHistory chatboardHistory, Account account) {
        ChatboardHistoryVO chatboardHistoryVO = new ChatboardHistoryVO();
        BeanUtils.copyProperties(chatboardHistory, chatboardHistoryVO);
        chatboardHistoryVO.setNickname(account.getNickname());
        chatboardHistoryVO.setAvatarUrl(account.getAvatarUrl());
        chatboardHistoryVO.setBannerUrl(account.getBannerUrl());
        return chatboardHistoryVO;
    }

}
