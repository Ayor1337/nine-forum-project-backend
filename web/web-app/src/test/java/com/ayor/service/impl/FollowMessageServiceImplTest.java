package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.FollowMessage;
import com.ayor.entity.pojo.Threadd;
import com.ayor.entity.pojo.UserRelation;
import com.ayor.entity.vo.FollowMessageVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.FollowMessageMapper;
import com.ayor.mapper.UserRelationMapper;
import com.ayor.service.MessageUnreadService;
import com.ayor.service.UserRelationService;
import com.ayor.type.RelationStatus;
import com.ayor.type.RelationType;
import com.ayor.type.UnreadMessageType;
import com.ayor.util.STOMPUtils;
import com.ayor.util.TipTapUtils;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FollowMessageServiceImplTest {

    @Mock
    private FollowMessageMapper followMessageMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private UserRelationMapper userRelationMapper;

    @Mock
    private UserRelationService userRelationService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private STOMPUtils stompUtils;

    @Mock
    private MessageUnreadService messageUnreadService;

    // 测试创建主题帖关注消息时为有效粉丝落库并推送在线用户
    @Test
    void shouldCreateAndPushFollowMessageForSubscribedFollower() {
        FollowMessageServiceImpl service = createService();
        Threadd thread = createThread();
        Account author = createAccount(8, "author");
        Account follower = createAccount(7, "follower");
        when(accountMapper.getAccountById(8)).thenReturn(author);
        when(userRelationMapper.selectList(any(Wrapper.class))).thenReturn(List.of(createFollowRelation(7, 8)));
        when(accountMapper.getAccountsByIds(List.of(7))).thenReturn(List.of(follower));
        when(stompUtils.isUserSubscribed("7", "/notif/follow")).thenReturn(true);
        when(followMessageMapper.insert(any(FollowMessage.class))).thenAnswer(invocation -> {
            FollowMessage message = invocation.getArgument(0);
            message.setFollowMessageId(51);
            return 1;
        });

        service.createThreadFollowMessages(thread);

        ArgumentCaptor<FollowMessage> entityCaptor = ArgumentCaptor.forClass(FollowMessage.class);
        ArgumentCaptor<FollowMessageVO> voCaptor = ArgumentCaptor.forClass(FollowMessageVO.class);
        verify(followMessageMapper).insert(entityCaptor.capture());
        verify(messagingTemplate).convertAndSendToUser(eq("7"), eq("/notif/follow"), voCaptor.capture());
        assertEquals(7, entityCaptor.getValue().getAccountId());
        assertEquals(8, entityCaptor.getValue().getFromAccountId());
        assertEquals(101, entityCaptor.getValue().getThreadId());
        assertEquals(3, entityCaptor.getValue().getTopicId());
        assertEquals("/threads/101", entityCaptor.getValue().getPath());
        assertEquals("关注帖", entityCaptor.getValue().getTitle());
        assertEquals("hello follow", entityCaptor.getValue().getContentSummary());
        assertEquals(3, voCaptor.getValue().getTopicId());
        assertEquals("author", voCaptor.getValue().getFromNickname());
        verify(messageUnreadService, never()).addUnread(any(), any(), any());
    }

    // 测试粉丝未订阅关注目的地时只增加未读数
    @Test
    void shouldIncreaseUnreadWhenFollowerNotSubscribed() {
        FollowMessageServiceImpl service = createService();
        when(accountMapper.getAccountById(8)).thenReturn(createAccount(8, "author"));
        when(userRelationMapper.selectList(any(Wrapper.class))).thenReturn(List.of(createFollowRelation(7, 8)));
        when(accountMapper.getAccountsByIds(List.of(7))).thenReturn(List.of(createAccount(7, "follower")));
        when(stompUtils.isUserSubscribed("7", "/notif/follow")).thenReturn(false);
        when(followMessageMapper.insert(any(FollowMessage.class))).thenReturn(1);

        service.createThreadFollowMessages(createThread());

        verify(messageUnreadService).addUnread(7, UnreadMessageType.FOLLOW_MESSAGE, 1L);
        verify(messagingTemplate, never()).convertAndSendToUser(eq("7"), eq("/notif/follow"), any());
    }

    // 测试拉黑关系不创建关注消息
    @Test
    void shouldSkipBlockedFollower() {
        FollowMessageServiceImpl service = createService();
        when(accountMapper.getAccountById(8)).thenReturn(createAccount(8, "author"));
        when(userRelationMapper.selectList(any(Wrapper.class))).thenReturn(List.of(createFollowRelation(7, 8)));
        when(accountMapper.getAccountsByIds(List.of(7))).thenReturn(List.of(createAccount(7, "follower")));
        when(userRelationService.isBlockedEitherDirection(7, 8)).thenReturn(true);

        service.createThreadFollowMessages(createThread());

        verify(followMessageMapper, never()).insert(any(FollowMessage.class));
        verify(messageUnreadService, never()).addUnread(any(), any(), any());
    }

    // 测试分页读取关注消息并转换为VO
    @Test
    void shouldListFollowMessages() {
        FollowMessageServiceImpl service = createService();
        FollowMessage message = new FollowMessage();
        message.setFollowMessageId(51);
        message.setAccountId(7);
        message.setFromAccountId(8);
        message.setThreadId(101);
        message.setTopicId(3);
        message.setPath("/threads/101");
        message.setTitle("关注帖");
        message.setContentSummary("summary");
        message.setCreateTime(new Date());
        Page<FollowMessage> page = Page.of(1, 7);
        page.setRecords(List.of(message));
        page.setTotal(1);
        when(followMessageMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);
        when(accountMapper.getAccountsByIds(List.of(8))).thenReturn(List.of(createAccount(8, "author")));

        PageEntity<FollowMessageVO> result = service.listFollowMessages(1, 7, 7);

        assertEquals(1L, result.getTotalSize());
        assertEquals(1, result.getData().size());
        assertEquals(51, result.getData().get(0).getFollowMessageId());
        assertEquals(3, result.getData().get(0).getTopicId());
        assertEquals("author", result.getData().get(0).getFromNickname());
    }

    // 测试账号为空时不查询数据库
    @Test
    void shouldReturnEmptyWhenAccountIdIsNull() {
        FollowMessageServiceImpl service = createService();

        PageEntity<FollowMessageVO> result = service.listFollowMessages(1, 7, null);

        assertEquals(0L, result.getTotalSize());
        assertEquals(0, result.getData().size());
        verify(followMessageMapper, never()).selectPage(any(), any());
    }

    private FollowMessageServiceImpl createService() {
        FollowMessageServiceImpl service = new FollowMessageServiceImpl(
                new TipTapUtils(),
                accountMapper,
                userRelationMapper,
                userRelationService,
                messagingTemplate,
                stompUtils,
                messageUnreadService
        );
        ReflectionTestUtils.setField(service, "baseMapper", followMessageMapper);
        return service;
    }

    private Threadd createThread() {
        Threadd thread = new Threadd();
        thread.setThreadId(101);
        thread.setTopicId(3);
        thread.setAccountId(8);
        thread.setTitle("关注帖");
        thread.setContent("{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"hello follow\"}]}]}");
        return thread;
    }

    private Account createAccount(Integer accountId, String nickname) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setUsername("user-" + accountId);
        account.setNickname(nickname);
        account.setAvatarUrl("avatar-" + accountId);
        account.setStatus(1);
        return account;
    }

    private UserRelation createFollowRelation(Integer fromAccountId, Integer toAccountId) {
        UserRelation relation = new UserRelation();
        relation.setFromAccountId(fromAccountId);
        relation.setToAccountId(toAccountId);
        relation.setRelationType(RelationType.FOLLOW);
        relation.setStatus(RelationStatus.ACTIVE);
        return relation;
    }
}
