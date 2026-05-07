package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Threadd;
import com.ayor.entity.vo.ThreadVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PostMapper;
import com.ayor.mapper.TagMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.mapper.TopicMapper;
import com.ayor.service.MentionMessageService;
import com.ayor.type.ThreadOrderType;
import com.ayor.util.TipTapUtils;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void shouldQueryThreadsByTopicIdWithTagIdAndHotOrder() {
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

        PageEntity<ThreadVO> result = service.getThreadVOsByTopicId(1, 3, "hot", 1, 10);

        ArgumentCaptor<Wrapper<Threadd>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(threaddMapper).selectPage(any(Page.class), wrapperCaptor.capture());

        assertNotNull(result);
        assertEquals(1L, result.getTotalSize());
        assertEquals(1, result.getData().size());
        assertEquals(101, result.getData().get(0).getThreadId());
        assertNotNull(wrapperCaptor.getValue());
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
    void shouldReturnNullWhenTopicIdIsNull() {
        ThreaddServiceImpl service = createService();

        PageEntity<ThreadVO> result = service.getThreadVOsByTopicId(null, 3, "hot", 1, 10);

        assertNull(result);
        verifyNoInteractions(topicMapper, threaddMapper);
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
                mentionMessageService
        );
        ReflectionTestUtils.setField(service, "baseMapper", threaddMapper);
        return service;
    }
}
