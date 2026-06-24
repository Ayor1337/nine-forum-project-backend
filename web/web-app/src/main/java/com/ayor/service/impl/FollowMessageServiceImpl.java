package com.ayor.service.impl;

import com.ayor.aspect.unread.MessageUnreadNotif;
import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.FollowMessage;
import com.ayor.entity.pojo.Threadd;
import com.ayor.entity.pojo.UserRelation;
import com.ayor.entity.vo.FollowMessageVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.FollowMessageMapper;
import com.ayor.mapper.UserRelationMapper;
import com.ayor.service.FollowMessageService;
import com.ayor.service.MessageUnreadService;
import com.ayor.service.UserRelationService;
import com.ayor.type.AccountStatus;
import com.ayor.type.RelationStatus;
import com.ayor.type.RelationType;
import com.ayor.type.UnreadMessageType;
import com.ayor.util.STOMPUtils;
import com.ayor.util.TipTapUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class FollowMessageServiceImpl extends ServiceImpl<FollowMessageMapper, FollowMessage> implements FollowMessageService {

    private static final int SUMMARY_MAX_LENGTH = 120;

    private final TipTapUtils tipTapUtils;

    private final AccountMapper accountMapper;

    private final UserRelationMapper userRelationMapper;

    private final UserRelationService userRelationService;

    private final SimpMessagingTemplate messagingTemplate;

    private final STOMPUtils stompUtils;

    private final MessageUnreadService messageUnreadService;

    @Override
    public void createThreadFollowMessages(Threadd thread) {
        if (thread == null || thread.getThreadId() == null || thread.getAccountId() == null) {
            return;
        }
        Account fromAccount = accountMapper.getAccountById(thread.getAccountId());
        if (!isActiveAccount(fromAccount)) {
            return;
        }

        List<UserRelation> relations = userRelationMapper.selectList(new LambdaQueryWrapper<UserRelation>()
                .eq(UserRelation::getToAccountId, thread.getAccountId())
                .eq(UserRelation::getRelationType, RelationType.FOLLOW)
                .eq(UserRelation::getStatus, RelationStatus.ACTIVE));
        if (relations.isEmpty()) {
            return;
        }

        List<Integer> followerIds = relations.stream()
                .map(UserRelation::getFromAccountId)
                .distinct()
                .toList();
        Map<Integer, Account> followerMap = new LinkedHashMap<>();
        for (Account follower : accountMapper.getAccountsByIds(followerIds)) {
            followerMap.put(follower.getAccountId(), follower);
        }

        String summary = buildSummary(thread.getContent());
        Date now = new Date();
        for (Integer followerId : followerIds) {
            if (followerId.equals(thread.getAccountId())) {
                continue;
            }
            Account follower = followerMap.get(followerId);
            if (!isActiveAccount(follower)) {
                continue;
            }
            if (userRelationService.isBlockedEitherDirection(followerId, thread.getAccountId())) {
                continue;
            }

            FollowMessage followMessage = new FollowMessage();
            followMessage.setAccountId(followerId);
            followMessage.setFromAccountId(thread.getAccountId());
            followMessage.setThreadId(thread.getThreadId());
            followMessage.setPath("/threads/" + thread.getThreadId());
            followMessage.setTitle(thread.getTitle());
            followMessage.setContentSummary(summary);
            followMessage.setCreateTime(now);
            if (this.baseMapper.insert(followMessage) > 0) {
                pushFollowMessage(followerId, toVO(followMessage, fromAccount));
            }
        }
    }

    @Override
    @MessageUnreadNotif(
            accountId = "#accountId",
            subscribeDest = "/notif/follow",
            type = UnreadMessageType.FOLLOW_MESSAGE,
            doRead = true
    )
    public PageEntity<FollowMessageVO> listFollowMessages(Integer pageNum, Integer pageSize, Integer accountId) {
        if (accountId == null) {
            return new PageEntity<>(0L, Collections.emptyList());
        }
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 7;
        }
        Page<FollowMessage> page = this.baseMapper.selectPage(Page.of(pageNum, pageSize),
                new LambdaQueryWrapper<FollowMessage>()
                        .eq(FollowMessage::getAccountId, accountId)
                        .orderByDesc(FollowMessage::getCreateTime));
        return new PageEntity<>(page.getTotal(), toVOList(page.getRecords()));
    }

    private List<FollowMessageVO> toVOList(List<FollowMessage> followMessages) {
        if (followMessages.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> fromAccountIds = followMessages.stream()
                .map(FollowMessage::getFromAccountId)
                .distinct()
                .toList();
        Map<Integer, Account> accountMap = new LinkedHashMap<>();
        for (Account account : accountMapper.getAccountsByIds(fromAccountIds)) {
            accountMap.put(account.getAccountId(), account);
        }
        List<FollowMessageVO> vos = new ArrayList<>(followMessages.size());
        for (FollowMessage followMessage : followMessages) {
            Account account = accountMap.get(followMessage.getFromAccountId());
            if (account == null) {
                continue;
            }
            vos.add(toVO(followMessage, account));
        }
        return vos;
    }

    private FollowMessageVO toVO(FollowMessage followMessage, Account fromAccount) {
        return FollowMessageVO.builder()
                .followMessageId(followMessage.getFollowMessageId())
                .fromAccountId(fromAccount.getAccountId())
                .fromUsername(fromAccount.getUsername())
                .fromNickname(fromAccount.getNickname())
                .fromAvatarUrl(fromAccount.getAvatarUrl())
                .threadId(followMessage.getThreadId())
                .title(followMessage.getTitle())
                .contentSummary(followMessage.getContentSummary())
                .path(followMessage.getPath())
                .createTime(followMessage.getCreateTime())
                .build();
    }

    private void pushFollowMessage(Integer accountId, FollowMessageVO messageVO) {
        if (!stompUtils.isUserSubscribed(accountId.toString(), "/notif/follow")) {
            messageUnreadService.addUnread(accountId, UnreadMessageType.FOLLOW_MESSAGE, 1L);
        } else {
            messagingTemplate.convertAndSendToUser(accountId.toString(), "/notif/follow", messageVO);
        }
        if (stompUtils.isUserSubscribed(accountId.toString(), "/notif/unread")) {
            messagingTemplate.convertAndSendToUser(accountId.toString(), "/notif/unread", messageUnreadService.getUnreadVO(accountId));
        }
        if (!stompUtils.isUserSubscribed(accountId.toString(), "/notif/unread/" + UnreadMessageType.FOLLOW_MESSAGE.getType())) {
            messagingTemplate.convertAndSendToUser(accountId.toString(),
                    "/notif/unread/" + UnreadMessageType.FOLLOW_MESSAGE.getType(),
                    messageUnreadService.getUnreadVO(accountId, UnreadMessageType.FOLLOW_MESSAGE));
        }
        if (stompUtils.isUserSubscribed(accountId.toString(), "/notif/unread-overview")) {
            messagingTemplate.convertAndSendToUser(accountId.toString(),
                    "/notif/unread-overview",
                    messageUnreadService.getUnreadOverviewVO(accountId));
        }
    }

    private boolean isActiveAccount(Account account) {
        return account != null
                && !account.isDeleted()
                && AccountStatus.fromCode(account.getStatus()) != AccountStatus.BANNED;
    }

    private String buildSummary(String content) {
        String text = tipTapUtils.extractText(content).trim();
        if (text.isEmpty()) {
            return "";
        }
        if (text.length() <= SUMMARY_MAX_LENGTH) {
            return text;
        }
        return text.substring(0, SUMMARY_MAX_LENGTH);
    }
}
