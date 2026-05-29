package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Threadd;
import com.ayor.entity.dto.ThreadDTO;
import com.ayor.entity.vo.ThreadVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PostMapper;
import com.ayor.mapper.TagMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.mapper.TopicMapper;
import com.ayor.service.AuthorizationService;
import com.ayor.service.ImageAssetService;
import com.ayor.service.MentionMessageService;
import com.ayor.service.UserRelationService;
import com.ayor.type.ThreadOrderType;
import com.ayor.util.TipTapUtils;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThreaddServiceImplTest {

    @Mock
    private ThreaddMapper threaddMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private TopicMapper topicMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private MentionMessageService mentionMessageService;

    @Mock
    private ImageAssetService imageAssetService;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private UserRelationService userRelationService;

    @Test
    void threadRankingMethodsShouldUseThreadRankingCache() throws NoSuchMethodException {
        Method topicMethod = ThreaddServiceImpl.class.getMethod(
                "getThreadRankingsByTopicId",
                Integer.class,
                String.class,
                String.class,
                Integer.class,
                Integer.class
        );
        Method allMethod = ThreaddServiceImpl.class.getMethod(
                "getThreadRankings",
                String.class,
                String.class,
                Integer.class,
                Integer.class
        );

        Cacheable topicCacheable = topicMethod.getAnnotation(Cacheable.class);
        Cacheable allCacheable = allMethod.getAnnotation(Cacheable.class);

        assertNotNull(topicCacheable);
        assertNotNull(allCacheable);
        assertEquals("threadRanking", topicCacheable.value()[0]);
        assertEquals("threadRanking", allCacheable.value()[0]);
        assertEquals("#result == null || #result.totalSize == 0", topicCacheable.unless());
        assertEquals("#result == null || #result.totalSize == 0", allCacheable.unless());
    }

    @Test
    void shouldQueryThreadsByTopicIdWithTagIdSelectedAndHotOrder() {
        ThreaddServiceImpl service = createService();
        when(topicMapper.isTopicDelete(1)).thenReturn(false);

        Threadd thread = createThread();
        Page<Threadd> page = Page.of(1, 10);
        page.setRecords(List.of(thread));
        page.setTotal(1);

        Account account = new Account();
        account.setAccountId(11);
        account.setNickname("tester");
        account.setAvatarUrl("avatar");

        when(accountMapper.getAccountById(11)).thenReturn(account);
        when(threaddMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        PageEntity<ThreadVO> result = service.getThreadVOsByTopicId(1, 3, true, "hot", 1, 10);

        ArgumentCaptor<Wrapper<Threadd>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(threaddMapper).selectPage(any(Page.class), wrapperCaptor.capture());

        assertNotNull(result);
        assertEquals(1L, result.getTotalSize());
        assertEquals(1, result.getData().size());
        assertEquals(101, result.getData().get(0).getThreadId());
        assertNotNull(wrapperCaptor.getValue());
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Threadd.class);
        String targetSql = wrapperCaptor.getValue().getTargetSql();
        assertTrue(targetSql.contains("tag_id"), targetSql);
        assertTrue(targetSql.contains("is_selected"), targetSql);
    }

    @Test
    void shouldParseLikesOrder() {
        assertEquals(ThreadOrderType.LIKES, ThreadOrderType.fromValue("likes"));
    }

    @Test
    void shouldParseCollectsOrder() {
        assertEquals(ThreadOrderType.COLLECTS, ThreadOrderType.fromValue("collects"));
    }

    @Test
    void shouldFallbackToHotOrderWhenUnsupportedOrderProvided() {
        assertEquals(ThreadOrderType.HOT, ThreadOrderType.fromValue("unknown"));
    }

    @Test
    void shouldQueryTopicThreadRankingsByPeriodAndLikesMetric() {
        ThreaddServiceImpl service = createService();
        when(topicMapper.isTopicDelete(1)).thenReturn(false);

        Threadd thread = createThread();
        Page<Threadd> page = Page.of(1, 10);
        page.setRecords(List.of(thread));
        page.setTotal(1);

        Account account = new Account();
        account.setAccountId(11);
        account.setNickname("tester");
        account.setAvatarUrl("avatar");

        when(accountMapper.getAccountById(11)).thenReturn(account);
        when(threaddMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        PageEntity<ThreadVO> result = service.getThreadRankingsByTopicId(1, "day", "likes", 1, 10);

        ArgumentCaptor<Wrapper<Threadd>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(threaddMapper).selectPage(any(Page.class), wrapperCaptor.capture());

        assertNotNull(result);
        assertEquals(1L, result.getTotalSize());
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Threadd.class);
        String targetSql = wrapperCaptor.getValue().getTargetSql();
        assertTrue(targetSql.contains("topic_id"), targetSql);
        assertTrue(targetSql.contains("is_deleted"), targetSql);
        assertTrue(targetSql.contains("create_time"), targetSql);
        assertTrue(targetSql.contains("like_count"), targetSql);
    }

    @Test
    void shouldQueryAllThreadRankingsWithoutTopicFilterAndViewsMetric() {
        ThreaddServiceImpl service = createService();
        Threadd thread = createThread();
        Page<Threadd> page = Page.of(1, 10);
        page.setRecords(List.of(thread));
        page.setTotal(1);

        Account account = new Account();
        account.setAccountId(11);
        account.setNickname("tester");
        account.setAvatarUrl("avatar");

        when(accountMapper.getAccountById(11)).thenReturn(account);
        when(threaddMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        PageEntity<ThreadVO> result = service.getThreadRankings("week", "views", 1, 10);

        ArgumentCaptor<Wrapper<Threadd>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(threaddMapper).selectPage(any(Page.class), wrapperCaptor.capture());

        assertNotNull(result);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Threadd.class);
        String targetSql = wrapperCaptor.getValue().getTargetSql();
        assertFalse(targetSql.contains("topic_id"), targetSql);
        assertTrue(targetSql.contains("is_deleted"), targetSql);
        assertTrue(targetSql.contains("create_time"), targetSql);
        assertTrue(targetSql.contains("view_count"), targetSql);
    }

    @Test
    void shouldFallbackToDefaultRankingPeriodAndCollectsMetric() {
        ThreaddServiceImpl service = createService();
        Threadd thread = createThread();
        Page<Threadd> page = Page.of(1, 10);
        page.setRecords(List.of(thread));
        page.setTotal(1);

        Account account = new Account();
        account.setAccountId(11);
        account.setNickname("tester");
        account.setAvatarUrl("avatar");

        when(accountMapper.getAccountById(11)).thenReturn(account);
        when(threaddMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        PageEntity<ThreadVO> result = service.getThreadRankings("unknown", "collects", 1, 10);

        ArgumentCaptor<Wrapper<Threadd>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(threaddMapper).selectPage(any(Page.class), wrapperCaptor.capture());

        assertNotNull(result);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Threadd.class);
        String targetSql = wrapperCaptor.getValue().getTargetSql();
        assertTrue(targetSql.contains("create_time"), targetSql);
        assertTrue(targetSql.contains("collect_count"), targetSql);
    }

    @Test
    void shouldDenyUserThreadPagesWhenViewerBlockedEitherDirection() {
        ThreaddServiceImpl service = createService();
        when(userRelationService.isBlockedEitherDirection(7, 18)).thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> service.getThreadPagesByUserId(7, 18, 1, 10));

        verifyNoInteractions(threaddMapper);
    }

    @Test
    void shouldAllowAnonymousUserThreadPagesWithoutBlockCheck() {
        ThreaddServiceImpl service = createService();
        Page<Threadd> page = Page.of(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        when(threaddMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        PageEntity<ThreadVO> result = service.getThreadPagesByUserId(null, 18, 1, 10);

        assertNotNull(result);
        assertEquals(0L, result.getTotalSize());
        verifyNoInteractions(userRelationService);
    }

    @Test
    void shouldReturnNullWhenTopicIdIsNull() {
        ThreaddServiceImpl service = createService();

        PageEntity<ThreadVO> result = service.getThreadVOsByTopicId(null, 3, true, "hot", 1, 10);

        assertNull(result);
        verifyNoInteractions(topicMapper, threaddMapper);
    }

    @Test
    void shouldSyncImageRefsAfterSavingThread() {
        ThreaddServiceImpl service = createService();
        ThreadDTO dto = new ThreadDTO();
        dto.setTitle("hello");
        dto.setTopicId(2);
        dto.setContent("{\"type\":\"doc\",\"content\":[]}");

        doAnswer(invocation -> {
            Threadd threadd = invocation.getArgument(0);
            threadd.setThreadId(321);
            return 1;
        }).when(threaddMapper).insert(any(Threadd.class));

        String result = service.insertThread(dto, 8);

        assertNull(result);
        verify(imageAssetService).syncContentRefs("THREAD", 321, "{\"type\":\"doc\",\"content\":[]}", 8);
        verify(mentionMessageService).createThreadMentionMessages("{\"type\":\"doc\",\"content\":[]}", 8, 321);
    }

    private Threadd createThread() {
        Threadd thread = new Threadd();
        thread.setThreadId(101);
        thread.setTitle("hot-thread");
        thread.setTopicId(1);
        thread.setTagId(3);
        thread.setAccountId(11);
        thread.setContent("{\"type\":\"doc\",\"content\":[]}");
        thread.setIsDeleted(false);
        thread.setCreateTime(new Date());
        return thread;
    }

    private ThreaddServiceImpl createService() {
        ThreaddServiceImpl service = new ThreaddServiceImpl(
                accountMapper,
                topicMapper,
                postMapper,
                new TipTapUtils(),
                tagMapper,
                mentionMessageService,
                imageAssetService,
                authorizationService,
                userRelationService
        );
        ReflectionTestUtils.setField(service, "baseMapper", threaddMapper);
        return service;
    }
}
