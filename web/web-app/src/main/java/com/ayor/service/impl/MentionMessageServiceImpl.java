package com.ayor.service.impl;

import com.ayor.aspect.unread.MessageUnreadNotif;
import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.MentionMessage;
import com.ayor.entity.vo.MentionMessageVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.MentionMessageMapper;
import com.ayor.service.MentionMessageService;
import com.ayor.service.MessageUnreadService;
import com.ayor.service.UserRelationService;
import com.ayor.type.AccountStatus;
import com.ayor.type.MentionSourceType;
import com.ayor.type.UnreadMessageType;
import com.ayor.util.STOMPUtils;
import com.ayor.util.TipTapUtils;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 提及消息服务实现。
 *
 * 负责解析帖子与评论中的 TipTap mention 节点，为被提及用户创建消息记录，
 * 并维护提及未读数与实时推送。
 */
@Service
@Transactional
@RequiredArgsConstructor
public class MentionMessageServiceImpl extends ServiceImpl<MentionMessageMapper, MentionMessage> implements MentionMessageService {

    private static final int SUMMARY_MAX_LENGTH = 120;

    private final TipTapUtils tipTapUtils;

    private final AccountMapper accountMapper;

    private final UserRelationService userRelationService;

    private final SimpMessagingTemplate messagingTemplate;

    private final STOMPUtils stompUtils;

    private final MessageUnreadService messageUnreadService;

    /**
     * 为帖子正文中的提及用户创建提及消息。
     *
     * @param content 帖子 TipTap JSON 内容
     * @param fromAccountId 发起提及的用户 ID
     * @param threadId 帖子 ID
     */
    @Override
    public void createThreadMentionMessages(String content, Integer fromAccountId, Integer threadId) {
        createMentionMessages(content, fromAccountId, MentionSourceType.THREAD, threadId, threadId,
                "/threads/" + threadId);
    }

    /**
     * 为评论正文中的提及用户创建提及消息。
     *
     * @param content 评论 TipTap JSON 内容
     * @param fromAccountId 发起提及的用户 ID
     * @param postId 评论 ID
     * @param threadId 评论所属帖子 ID
     */
    @Override
    public void createPostMentionMessages(String content, Integer fromAccountId, Integer postId, Integer threadId) {
        createMentionMessages(content, fromAccountId, MentionSourceType.POST, postId, threadId,
                "/threads/" + threadId + "#post-" + postId);
    }

    @Override
    @MessageUnreadNotif(
            accountId = "#accountId",
            subscribeDest = "/notif/mention",
            type = UnreadMessageType.MENTION_MESSAGE,
            doRead = true
    )
    /**
     * 分页获取当前用户收到的提及消息，并在读取后清空提及未读数。
     *
     * @param pageNum 页码，从 1 开始
     * @param pageSize 每页记录数
     * @param accountId 当前用户 ID
     * @return 提及消息分页结果
     */
    public PageEntity<MentionMessageVO> listMentionMessages(Integer pageNum, Integer pageSize, Integer accountId) {
        if (accountId == null) {
            return new PageEntity<>(0L, Collections.emptyList());
        }
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 7;
        }
        Page<MentionMessage> page = this.lambdaQuery()
                .eq(MentionMessage::getAccountId, accountId)
                .orderByDesc(MentionMessage::getCreateTime)
                .page(Page.of(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVOList(page.getRecords()));
    }

    /**
     * 统一处理帖子或评论中的提及节点并生成提及消息。
     */
    private void createMentionMessages(String content,
                                       Integer fromAccountId,
                                       MentionSourceType sourceType,
                                       Integer sourceId,
                                       Integer threadId,
                                       String path) {
        if (content == null || fromAccountId == null || sourceId == null || threadId == null || path == null) {
            return;
        }
        Account fromAccount = accountMapper.getAccountById(fromAccountId);
        if (!isMentionableAccount(fromAccount)) {
            return;
        }

        Set<Integer> uniqueMentionAccountIds = new LinkedHashSet<>();
        for (TipTapUtils.MentionTarget mentionTarget : tipTapUtils.extractMentions(content)) {
            if (mentionTarget.accountId() == null) {
                continue;
            }
            uniqueMentionAccountIds.add(mentionTarget.accountId());
        }
        if (uniqueMentionAccountIds.isEmpty()) {
            return;
        }

        List<Account> targetAccounts = accountMapper.getAccountsByIds(new ArrayList<>(uniqueMentionAccountIds));
        Map<Integer, Account> targetAccountMap = new LinkedHashMap<>();
        for (Account targetAccount : targetAccounts) {
            targetAccountMap.put(targetAccount.getAccountId(), targetAccount);
        }

        String summary = buildSummary(content);
        Date now = new Date();
        for (Integer targetAccountId : uniqueMentionAccountIds) {
            if (targetAccountId.equals(fromAccountId)) {
                continue;
            }
            Account targetAccount = targetAccountMap.get(targetAccountId);
            if (!isMentionableAccount(targetAccount)) {
                continue;
            }
            if (userRelationService.isBlockedEitherDirection(fromAccountId, targetAccountId)) {
                continue;
            }
            MentionMessage mentionMessage = new MentionMessage();
            mentionMessage.setAccountId(targetAccountId);
            mentionMessage.setFromAccountId(fromAccountId);
            mentionMessage.setSourceType(sourceType.getValue());
            mentionMessage.setSourceId(sourceId);
            mentionMessage.setThreadId(threadId);
            mentionMessage.setPath(path);
            mentionMessage.setContentSummary(summary);
            mentionMessage.setCreateTime(now);
            if (this.baseMapper.insert(mentionMessage) > 0) {
                pushMentionMessage(targetAccountId, toVO(mentionMessage, fromAccount));
            }
        }
    }

    /**
     * 将提及消息实体列表转换为视图对象列表。
     */
    private List<MentionMessageVO> toVOList(List<MentionMessage> mentionMessages) {
        if (mentionMessages.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> fromAccountIds = mentionMessages.stream()
                .map(MentionMessage::getFromAccountId)
                .distinct()
                .toList();
        Map<Integer, Account> accountMap = new LinkedHashMap<>();
        for (Account account : accountMapper.getAccountsByIds(fromAccountIds)) {
            accountMap.put(account.getAccountId(), account);
        }
        List<MentionMessageVO> vos = new ArrayList<>(mentionMessages.size());
        for (MentionMessage mentionMessage : mentionMessages) {
            Account account = accountMap.get(mentionMessage.getFromAccountId());
            if (account == null) {
                continue;
            }
            vos.add(toVO(mentionMessage, account));
        }
        return vos;
    }

    /**
     * 将单条提及消息和发起人信息转换为视图对象。
     */
    private MentionMessageVO toVO(MentionMessage mentionMessage, Account fromAccount) {
        return MentionMessageVO.builder()
                .mentionMessageId(mentionMessage.getMentionMessageId())
                .fromAccountId(fromAccount.getAccountId())
                .fromUsername(fromAccount.getUsername())
                .fromNickname(fromAccount.getNickname())
                .fromAvatarUrl(fromAccount.getAvatarUrl())
                .contentSummary(mentionMessage.getContentSummary())
                .path(mentionMessage.getPath())
                .sourceType(mentionMessage.getSourceType())
                .createTime(mentionMessage.getCreateTime())
                .build();
    }

    /**
     * 向被提及用户推送提及消息，并同步更新提及未读数及总未读数。
     */
    private void pushMentionMessage(Integer accountId, MentionMessageVO messageVO) {
        if (!stompUtils.isUserSubscribed(accountId.toString(), "/notif/mention")) {
            messageUnreadService.addUnread(accountId, UnreadMessageType.MENTION_MESSAGE, 1L);
        } else {
            messagingTemplate.convertAndSendToUser(accountId.toString(), "/notif/mention", messageVO);
        }
        if (stompUtils.isUserSubscribed(accountId.toString(), "/notif/unread")) {
            messagingTemplate.convertAndSendToUser(accountId.toString(), "/notif/unread", messageUnreadService.getUnreadVO(accountId));
        }
        if (!stompUtils.isUserSubscribed(accountId.toString(), "/notif/unread/" + UnreadMessageType.MENTION_MESSAGE.getType())) {
            messagingTemplate.convertAndSendToUser(accountId.toString(),
                    "/notif/unread/" + UnreadMessageType.MENTION_MESSAGE.getType(),
                    messageUnreadService.getUnreadVO(accountId, UnreadMessageType.MENTION_MESSAGE));
        }
        if (stompUtils.isUserSubscribed(accountId.toString(), "/notif/unread-overview")) {
            messagingTemplate.convertAndSendToUser(accountId.toString(),
                    "/notif/unread-overview",
                    messageUnreadService.getUnreadOverviewVO(accountId));
        }
    }

    /**
     * 判断账号是否允许被提及。
     */
    private boolean isMentionableAccount(Account account) {
        return account != null
                && !account.isDeleted()
                && AccountStatus.fromCode(account.getStatus()) != AccountStatus.BANNED;
    }

    /**
     * 从 TipTap 内容中提取纯文本摘要，用于消息列表展示。
     */
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
