package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Announcement;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Tag;
import com.ayor.entity.pojo.Threadd;
import com.ayor.entity.pojo.ThreadEditHistory;
import com.ayor.entity.dto.ThreadDTO;
import com.ayor.entity.vo.AnnouncementVO;
import com.ayor.entity.vo.ThreadVO;
import com.ayor.entity.vo.ThreadBreadcrumbVO;
import com.ayor.entity.pojo.Topic;
import com.ayor.image.ImageStorageService;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.AnnouncementMapper;
import com.ayor.mapper.PostMapper;
import com.ayor.mapper.TagMapper;
import com.ayor.mapper.ThreadEditHistoryMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.mapper.TopicMapper;
import com.ayor.service.AuthorizationService;
import com.ayor.service.ImageAssetService;
import com.ayor.service.FollowMessageService;
import com.ayor.service.ForumRealtimeService;
import com.ayor.service.MentionMessageService;
import com.ayor.service.UserRelationService;
import com.ayor.service.CacheInvalidationService;
import com.ayor.mq.EsIndexSyncProducer;
import com.ayor.type.ThreadOrderType;
import com.ayor.util.TipTapUtils;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    private AnnouncementMapper announcementMapper;

    @Mock
    private TopicMapper topicMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private MentionMessageService mentionMessageService;

    @Mock
    private FollowMessageService followMessageService;

    @Mock
    private ForumRealtimeService forumRealtimeService;

    @Mock
    private ImageAssetService imageAssetService;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private UserRelationService userRelationService;

    @Mock
    private ThreadEditHistoryMapper threadEditHistoryMapper;

    @Mock
    private CacheInvalidationService cacheInvalidationService;

    @Mock
    private EsIndexSyncProducer esIndexSyncProducer;

    // 测试帖子串排行方法使用帖子串排行缓存
    @Test
    void threadRankingMethodsShouldUseThreadRankingCache() throws NoSuchMethodException {
        Method topicMethod = ThreaddServiceImpl.class.getMethod(
                "getThreadRankingsByTopicId",
                Integer.class,
                Integer.class,
                String.class,
                String.class,
                Integer.class,
                Integer.class
        );
        Method allMethod = ThreaddServiceImpl.class.getMethod(
                "getThreadRankings",
                Integer.class,
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

    // 测试按主题查询帖子串时传入标签ID和选中状态并按热门排序
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

        PageEntity<ThreadVO> result = service.getThreadVOsByTopicId(7, 1, 3, true, "hot", 1, 10);

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

    // 测试帖子串列表返回正文中的全部图片URL
    @Test
    void shouldReturnAllImageUrlsInThreadList() {
        ThreaddServiceImpl service = createService();
        when(topicMapper.isTopicDelete(1)).thenReturn(false);

        Threadd thread = createThread();
        thread.setContent(imageDocument(8));
        Page<Threadd> page = Page.of(1, 10);
        page.setRecords(List.of(thread));
        page.setTotal(1);

        Account account = new Account();
        account.setAccountId(11);
        account.setNickname("tester");
        account.setAvatarUrl("avatar");
        when(accountMapper.getAccountById(11)).thenReturn(account);
        when(threaddMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        PageEntity<ThreadVO> result = service.getThreadVOsByTopicId(7, 1, null, null, "latest", 1, 10);

        assertNotNull(result);
        assertEquals(expectedImageUrls(8), result.getData().get(0).getImageUrls());
    }

    // 测试排除拉黑账号从主题帖子串分页
    @Test
    void shouldExcludeBlockedAccountsFromTopicThreadPages() {
        ThreaddServiceImpl service = createService();
        when(topicMapper.isTopicDelete(1)).thenReturn(false);
        when(userRelationService.listBlockedAccountIdsEitherDirection(7)).thenReturn(List.of(11, 12));

        Page<Threadd> page = Page.of(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        when(threaddMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        service.getThreadVOsByTopicId(7, 1, null, null, "hot", 1, 10);

        ArgumentCaptor<Wrapper<Threadd>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(threaddMapper).selectPage(any(Page.class), wrapperCaptor.capture());

        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Threadd.class);
        String targetSql = wrapperCaptor.getValue().getTargetSql();
        assertTrue(targetSql.contains("account_id NOT IN"), targetSql);
    }

    // 测试解析点赞排序
    @Test
    void shouldParseLikesOrder() {
        assertEquals(ThreadOrderType.LIKES, ThreadOrderType.fromValue("likes"));
    }

    // 测试解析收藏排序
    @Test
    void shouldParseCollectsOrder() {
        assertEquals(ThreadOrderType.COLLECTS, ThreadOrderType.fromValue("collects"));
    }

    // 测试传入不支持排序时回退到热门排序
    @Test
    void shouldFallbackToHotOrderWhenUnsupportedOrderProvided() {
        assertEquals(ThreadOrderType.HOT, ThreadOrderType.fromValue("unknown"));
    }

    // 测试查询主题帖子串排行按周期并点赞指标
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

        PageEntity<ThreadVO> result = service.getThreadRankingsByTopicId(7, 1, "day", "likes", 1, 10);

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

    // 测试查询全部帖子串排行不带主题过滤并浏览指标
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

        PageEntity<ThreadVO> result = service.getThreadRankings(7, "week", "views", 1, 10);

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

    // 测试回退到默认排行周期并收藏指标
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

        PageEntity<ThreadVO> result = service.getThreadRankings(7, "unknown", "collects", 1, 10);

        ArgumentCaptor<Wrapper<Threadd>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(threaddMapper).selectPage(any(Page.class), wrapperCaptor.capture());

        assertNotNull(result);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Threadd.class);
        String targetSql = wrapperCaptor.getValue().getTargetSql();
        assertTrue(targetSql.contains("create_time"), targetSql);
        assertTrue(targetSql.contains("collect_count"), targetSql);
    }

    // 测试拒绝用户帖子串分页当查看者拉黑任一方向
    @Test
    void shouldDenyUserThreadPagesWhenViewerBlockedEitherDirection() {
        ThreaddServiceImpl service = createService();
        when(userRelationService.isBlockedEitherDirection(7, 18)).thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> service.getThreadPagesByUserId(7, 18, 1, 10));

        verifyNoInteractions(threaddMapper);
    }

    // 测试匿名访问用户帖子串分页时不做拉黑检查
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

    // 测试拒绝帖子串详情当查看者拉黑任一方向
    @Test
    void shouldDenyThreadDetailWhenViewerBlockedEitherDirection() {
        ThreaddServiceImpl service = createService();
        Threadd thread = createThread();
        when(threaddMapper.selectById(101)).thenReturn(thread);
        when(userRelationService.isBlockedEitherDirection(7, 11)).thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> service.getThreadById(7, 101));
    }

    // 测试主题ID为空时返回空
    @Test
    void shouldReturnNullWhenTopicIdIsNull() {
        ThreaddServiceImpl service = createService();

        PageEntity<ThreadVO> result = service.getThreadVOsByTopicId(7, null, 3, true, "hot", 1, 10);

        assertNull(result);
        verifyNoInteractions(topicMapper, threaddMapper);
    }

    // 测试面包屑返回帖子与所属主题名称
    @Test
    void shouldReturnThreadAndTopicNamesForBreadcrumb() {
        ThreaddServiceImpl service = createService();
        Threadd thread = createThread();
        Topic topic = new Topic();
        topic.setTopicId(1);
        topic.setTitle("技术交流");
        topic.setIsDeleted(false);
        when(threaddMapper.selectById(101)).thenReturn(thread);
        when(topicMapper.selectById(1)).thenReturn(topic);

        ThreadBreadcrumbVO result = service.getThreadBreadcrumbById(101);

        assertNotNull(result);
        assertEquals("hot-thread", result.getThreadName());
        assertEquals("技术交流", result.getTopicName());
    }

    // 测试面包屑不暴露已删除的帖子或主题
    @Test
    void shouldReturnNullForBreadcrumbWhenTopicIsDeleted() {
        ThreaddServiceImpl service = createService();
        Threadd thread = createThread();
        Topic topic = new Topic();
        topic.setTopicId(1);
        topic.setIsDeleted(true);
        when(threaddMapper.selectById(101)).thenReturn(thread);
        when(topicMapper.selectById(1)).thenReturn(topic);

        assertNull(service.getThreadBreadcrumbById(101));
    }

    // 测试保存帖子后同步图片引用串
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
        verify(followMessageService).createThreadFollowMessages(any(Threadd.class));
        verify(cacheInvalidationService).clearThreadRanking();
        verify(forumRealtimeService).publishThreadCreated(any(Threadd.class));
    }

    // 测试创建帖子允许0、1、7张图片
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 7})
    void shouldInsertThreadWithUpToSevenImages(int imageCount) {
        ThreaddServiceImpl service = createService();
        ThreadDTO dto = new ThreadDTO();
        dto.setTitle("hello");
        dto.setTopicId(2);
        dto.setContent(imageDocument(imageCount));
        when(threaddMapper.insert(any(Threadd.class))).thenReturn(1);

        String result = service.insertThread(dto, 8);

        assertNull(result);
        verify(threaddMapper).insert(any(Threadd.class));
    }

    // 测试创建帖子超过7张时在上传和任何写入副作用前拒绝
    @Test
    void shouldRejectInsertThreadWithEightImagesBeforeSideEffects() {
        ImageStorageService storageService = mock(ImageStorageService.class);
        TipTapUtils tipTapUtils = new TipTapUtils();
        ReflectionTestUtils.setField(tipTapUtils, "imageStorageService", storageService);
        ThreaddServiceImpl service = createService(tipTapUtils);
        ThreadDTO dto = new ThreadDTO();
        dto.setTitle("hello");
        dto.setTopicId(2);
        dto.setContent(imageDocument(8).replace(
                "https://example.com/0.png",
                "data:image/png;base64,AA=="
        ));

        String result = service.insertThread(dto, 8);

        assertEquals("帖子最多只能包含7张图片", result);
        verifyNoInteractions(storageService);
        verify(threaddMapper, never()).insert(any(Threadd.class));
        verifyNoInteractions(
                imageAssetService,
                mentionMessageService,
                followMessageService,
                cacheInvalidationService,
                forumRealtimeService,
                esIndexSyncProducer
        );
    }

    // 测试保存帖子串失败时不推送实时事件
    @Test
    void shouldNotPublishRealtimeEventWhenInsertThreadFails() {
        ThreaddServiceImpl service = createService();
        ThreadDTO dto = new ThreadDTO();
        dto.setTitle("hello");
        dto.setTopicId(2);
        dto.setContent("{\"type\":\"doc\",\"content\":[]}");
        when(threaddMapper.insert(any(Threadd.class))).thenReturn(0);

        String result = service.insertThread(dto, 8);

        assertEquals("添加失败", result);
        verify(forumRealtimeService, never()).publishThreadCreated(any(Threadd.class));
    }

    // 测试发帖时携带有效标签则随帖子保存
    @Test
    void shouldInsertThreadWithValidTag() {
        ThreaddServiceImpl service = createService();
        ThreadDTO dto = new ThreadDTO();
        dto.setTitle("hello");
        dto.setTopicId(2);
        dto.setTagId(3);
        dto.setContent("{\"type\":\"doc\",\"content\":[]}");

        Tag tag = new Tag();
        tag.setTagId(3);
        tag.setTopicId(2);
        when(tagMapper.getTagById(3)).thenReturn(tag);
        doAnswer(invocation -> {
            Threadd threadd = invocation.getArgument(0);
            threadd.setThreadId(322);
            return 1;
        }).when(threaddMapper).insert(any(Threadd.class));

        String result = service.insertThread(dto, 8);

        assertNull(result);
        ArgumentCaptor<Threadd> captor = ArgumentCaptor.forClass(Threadd.class);
        verify(threaddMapper).insert(captor.capture());
        assertEquals(3, captor.getValue().getTagId());
    }

    // 测试发帖时标签不存在则拒绝
    @Test
    void shouldRejectInsertThreadWhenTagMissing() {
        ThreaddServiceImpl service = createService();
        ThreadDTO dto = new ThreadDTO();
        dto.setTitle("hello");
        dto.setTopicId(2);
        dto.setTagId(99);
        dto.setContent("{\"type\":\"doc\",\"content\":[]}");
        when(tagMapper.getTagById(99)).thenReturn(null);

        String result = service.insertThread(dto, 8);

        assertEquals("标签不存在", result);
        verify(threaddMapper, never()).insert(any(Threadd.class));
    }

    // 测试发帖时标签不属于该主题则拒绝
    @Test
    void shouldRejectInsertThreadWhenTagNotInTopic() {
        ThreaddServiceImpl service = createService();
        ThreadDTO dto = new ThreadDTO();
        dto.setTitle("hello");
        dto.setTopicId(2);
        dto.setTagId(3);
        dto.setContent("{\"type\":\"doc\",\"content\":[]}");

        Tag tag = new Tag();
        tag.setTagId(3);
        tag.setTopicId(9);
        when(tagMapper.getTagById(3)).thenReturn(tag);

        String result = service.insertThread(dto, 8);

        assertEquals("标签不属于该主题", result);
        verify(threaddMapper, never()).insert(any(Threadd.class));
    }

    // 测试编辑帖子时更新为有效标签
    @Test
    void shouldUpdateTagWhenEditingThread() {
        ThreaddServiceImpl service = createService();
        Threadd thread = createThread();
        when(threaddMapper.selectById(101)).thenReturn(thread);
        when(threaddMapper.updateById(any(Threadd.class))).thenReturn(1);

        Tag tag = new Tag();
        tag.setTagId(5);
        tag.setTopicId(1);
        when(tagMapper.getTagById(5)).thenReturn(tag);

        ThreadDTO dto = new ThreadDTO();
        dto.setTitle("new-title");
        dto.setTopicId(1);
        dto.setTagId(5);
        dto.setContent("{\"type\":\"doc\",\"content\":[]}");

        String result = service.editThread(101, dto, 11);

        assertNull(result);
        ArgumentCaptor<Threadd> captor = ArgumentCaptor.forClass(Threadd.class);
        verify(threaddMapper).updateById(captor.capture());
        assertEquals(5, captor.getValue().getTagId());
        verify(threaddMapper, never()).removeThreadTag(any(), any());
    }

    // 测试编辑帖子允许7张图片
    @Test
    void shouldEditThreadWithSevenImages() {
        ThreaddServiceImpl service = createService();
        Threadd thread = createThread();
        when(threaddMapper.selectById(101)).thenReturn(thread);
        when(threaddMapper.updateById(any(Threadd.class))).thenReturn(1);

        ThreadDTO dto = new ThreadDTO();
        dto.setTitle("new-title");
        dto.setTopicId(1);
        dto.setTagId(3);
        dto.setContent(imageDocument(7));
        Tag tag = new Tag();
        tag.setTagId(3);
        tag.setTopicId(1);
        when(tagMapper.getTagById(3)).thenReturn(tag);

        String result = service.editThread(101, dto, 11);

        assertNull(result);
        verify(threadEditHistoryMapper).insert(any(ThreadEditHistory.class));
        verify(threaddMapper).updateById(any(Threadd.class));
    }

    // 测试编辑帖子超过7张时在上传、历史快照和更新副作用前拒绝
    @Test
    void shouldRejectEditThreadWithEightImagesBeforeSideEffects() {
        ImageStorageService storageService = mock(ImageStorageService.class);
        TipTapUtils tipTapUtils = new TipTapUtils();
        ReflectionTestUtils.setField(tipTapUtils, "imageStorageService", storageService);
        ThreaddServiceImpl service = createService(tipTapUtils);
        when(threaddMapper.selectById(101)).thenReturn(createThread());

        ThreadDTO dto = new ThreadDTO();
        dto.setTitle("new-title");
        dto.setTopicId(1);
        dto.setContent(imageDocument(8).replace(
                "https://example.com/0.png",
                "data:image/png;base64,AA=="
        ));

        String result = service.editThread(101, dto, 11);

        assertEquals("帖子最多只能包含7张图片", result);
        verifyNoInteractions(storageService, threadEditHistoryMapper);
        verify(threaddMapper, never()).updateById(any(Threadd.class));
        verifyNoInteractions(imageAssetService, mentionMessageService, cacheInvalidationService, esIndexSyncProducer);
    }

    // 测试编辑帖子时不传标签则清除原标签
    @Test
    void shouldClearTagWhenEditingThreadWithNullTagId() {
        ThreaddServiceImpl service = createService();
        Threadd thread = createThread();
        when(threaddMapper.selectById(101)).thenReturn(thread);
        when(threaddMapper.updateById(any(Threadd.class))).thenReturn(1);
        when(threaddMapper.removeThreadTag(101, 1)).thenReturn(true);

        ThreadDTO dto = new ThreadDTO();
        dto.setTitle("new-title");
        dto.setTopicId(1);
        dto.setContent("{\"type\":\"doc\",\"content\":[]}");

        String result = service.editThread(101, dto, 11);

        assertNull(result);
        verify(threaddMapper).removeThreadTag(101, 1);
    }

    // 测试编辑帖子时标签不属于该主题则拒绝
    @Test
    void shouldRejectEditThreadWhenTagNotInTopic() {
        ThreaddServiceImpl service = createService();
        Threadd thread = createThread();
        when(threaddMapper.selectById(101)).thenReturn(thread);

        Tag tag = new Tag();
        tag.setTagId(5);
        tag.setTopicId(9);
        when(tagMapper.getTagById(5)).thenReturn(tag);

        ThreadDTO dto = new ThreadDTO();
        dto.setTitle("new-title");
        dto.setTopicId(1);
        dto.setTagId(5);
        dto.setContent("{\"type\":\"doc\",\"content\":[]}");

        String result = service.editThread(101, dto, 11);

        assertEquals("标签不属于该主题", result);
        verify(threaddMapper, never()).updateById(any(Threadd.class));
    }

    // 测试设置主题公告使用公告表
    @Test
    void shouldSetTopicAnnouncementUsingAnnouncementTable() {
        ThreaddServiceImpl service = createService();
        Threadd thread = createThread();
        when(threaddMapper.selectOne(any(Wrapper.class))).thenReturn(thread);
        when(announcementMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(announcementMapper.insert(any(Announcement.class))).thenReturn(1);

        String result = service.setAnnouncementByThreadId(101, 1);

        ArgumentCaptor<Announcement> captor = ArgumentCaptor.forClass(Announcement.class);
        verify(announcementMapper).insert(captor.capture());
        assertNull(result);
        assertEquals(101, captor.getValue().getThreadId());
        assertFalse(captor.getValue().getIsGlobal());
        assertNotNull(captor.getValue().getCreateTime());
    }

    // 测试拒绝重复主题公告
    @Test
    void shouldRejectDuplicateTopicAnnouncement() {
        ThreaddServiceImpl service = createService();
        Threadd thread = createThread();
        Announcement existing = new Announcement();
        existing.setAnnouncementId(5);
        existing.setThreadId(101);
        existing.setIsGlobal(false);
        when(threaddMapper.selectOne(any(Wrapper.class))).thenReturn(thread);
        when(announcementMapper.selectOne(any(Wrapper.class))).thenReturn(existing);

        String result = service.setAnnouncementByThreadId(101, 1);

        assertEquals("该帖子已经是公告", result);
    }

    // 测试移除主题公告从公告表
    @Test
    void shouldRemoveTopicAnnouncementFromAnnouncementTable() {
        ThreaddServiceImpl service = createService();
        Threadd thread = createThread();
        Announcement existing = new Announcement();
        existing.setAnnouncementId(5);
        existing.setThreadId(101);
        existing.setIsGlobal(false);
        when(threaddMapper.selectOne(any(Wrapper.class))).thenReturn(thread);
        when(announcementMapper.selectOne(any(Wrapper.class))).thenReturn(existing);
        when(announcementMapper.deleteById(5)).thenReturn(1);

        String result = service.removeAnnouncementByThreadId(101, 1);

        assertNull(result);
        verify(announcementMapper).deleteById(5);
    }

    // 测试返回主题公告从公告 Mapper
    @Test
    void shouldReturnTopicAnnouncementsFromAnnouncementMapper() {
        ThreaddServiceImpl service = createService();
        AnnouncementVO announcementVO = new AnnouncementVO();
        announcementVO.setAnnouncementId(7);
        announcementVO.setThreadId(101);
        announcementVO.setTopicId(1);
        announcementVO.setIsGlobal(false);
        when(announcementMapper.getTopicAnnouncements(1)).thenReturn(List.of(announcementVO));

        List<AnnouncementVO> result = service.getAnnouncementThreads(1);

        assertEquals(1, result.size());
        assertEquals(7, result.get(0).getAnnouncementId());
        assertFalse(result.get(0).getIsGlobal());
    }

    // 测试设置全局公告使用公告表
    @Test
    void shouldSetGlobalAnnouncementUsingAnnouncementTable() {
        ThreaddServiceImpl service = createService();
        Threadd thread = createThread();
        when(threaddMapper.selectById(101)).thenReturn(thread);
        when(announcementMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(announcementMapper.insert(any(Announcement.class))).thenReturn(1);

        String result = service.setGlobalAnnouncementByThreadId(101);

        ArgumentCaptor<Announcement> captor = ArgumentCaptor.forClass(Announcement.class);
        verify(announcementMapper).insert(captor.capture());
        assertNull(result);
        assertEquals(101, captor.getValue().getThreadId());
        assertTrue(captor.getValue().getIsGlobal());
    }

    // 测试移除全局公告从公告表
    @Test
    void shouldRemoveGlobalAnnouncementFromAnnouncementTable() {
        ThreaddServiceImpl service = createService();
        Threadd thread = createThread();
        Announcement existing = new Announcement();
        existing.setAnnouncementId(5);
        existing.setThreadId(101);
        existing.setIsGlobal(true);
        when(threaddMapper.selectById(101)).thenReturn(thread);
        when(announcementMapper.selectOne(any(Wrapper.class))).thenReturn(existing);
        when(announcementMapper.deleteById(5)).thenReturn(1);

        String result = service.removeGlobalAnnouncementByThreadId(101);

        assertNull(result);
        verify(announcementMapper).deleteById(5);
    }

    // 测试返回全局公告从公告 Mapper
    @Test
    void shouldReturnGlobalAnnouncementsFromAnnouncementMapper() {
        ThreaddServiceImpl service = createService();
        AnnouncementVO announcementVO = new AnnouncementVO();
        announcementVO.setAnnouncementId(7);
        announcementVO.setThreadId(101);
        announcementVO.setIsGlobal(true);
        when(announcementMapper.getGlobalAnnouncements()).thenReturn(List.of(announcementVO));

        List<AnnouncementVO> result = service.getGlobalAnnouncementThreads();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsGlobal());
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
        return createService(new TipTapUtils());
    }

    private ThreaddServiceImpl createService(TipTapUtils tipTapUtils) {
        ThreaddServiceImpl service = new ThreaddServiceImpl(
                accountMapper,
                announcementMapper,
                topicMapper,
                postMapper,
                tipTapUtils,
                tagMapper,
                mentionMessageService,
                followMessageService,
                forumRealtimeService,
                imageAssetService,
                authorizationService,
                userRelationService,
                threadEditHistoryMapper,
                cacheInvalidationService,
                esIndexSyncProducer
        );
        ReflectionTestUtils.setField(service, "baseMapper", threaddMapper);
        return service;
    }

    private String imageDocument(int imageCount) {
        StringBuilder builder = new StringBuilder("{\"type\":\"doc\",\"content\":[");
        for (int index = 0; index < imageCount; index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append("{\"type\":\"image\",\"attrs\":{\"src\":\"https://example.com/")
                    .append(index)
                    .append(".png\"}}");
        }
        return builder.append("]}").toString();
    }

    private List<String> expectedImageUrls(int imageCount) {
        List<String> urls = new java.util.ArrayList<>(imageCount);
        for (int index = 0; index < imageCount; index++) {
            urls.add("https://example.com/" + index + ".png");
        }
        return urls;
    }
}
