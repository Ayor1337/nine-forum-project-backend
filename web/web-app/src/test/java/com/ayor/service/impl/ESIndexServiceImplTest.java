package com.ayor.service.impl;

import com.ayor.dao.ThreaddRepository;
import com.ayor.entity.document.ThreadDoc;
import com.ayor.mapper.PostMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.ESIndexManager;
import com.ayor.service.PostService;
import com.ayor.service.ThreaddService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitsIterator;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ESIndexServiceImplTest {

    @Mock
    private ThreaddRepository threaddRepository;

    @Mock
    private ThreaddService threaddService;

    @Mock
    private PostService postService;

    @Mock
    private ESIndexManager esIndexManager;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private ThreaddMapper threaddMapper;

    @Mock
    private PostMapper postMapper;

    private ESIndexServiceImpl createService() {
        return new ESIndexServiceImpl(
                threaddRepository,
                threaddService,
                postService,
                esIndexManager,
                elasticsearchOperations
        );
    }

    @SuppressWarnings("unchecked")
    private void givenEmptyDatabase() {
        when(threaddService.lambdaQuery()).thenReturn(new LambdaQueryChainWrapper<>(threaddMapper));
        when(postService.lambdaQuery()).thenReturn(new LambdaQueryChainWrapper<>(postMapper));
        when(threaddMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(Page.of(1, 500));
        when(postMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(Page.of(1, 500));
    }

    @SuppressWarnings("unchecked")
    private SearchHitsIterator<ThreadDoc> iteratorOf(String... ids) {
        SearchHitsIterator<ThreadDoc> iterator = mock(SearchHitsIterator.class);
        Boolean[] remaining = new Boolean[ids.length - 1];
        java.util.Arrays.fill(remaining, Boolean.TRUE);
        when(iterator.hasNext()).thenReturn(true, remaining).thenReturn(false);
        SearchHit<ThreadDoc> first = null;
        SearchHit<ThreadDoc>[] rest = new SearchHit[Math.max(ids.length - 1, 0)];
        for (int i = 0; i < ids.length; i++) {
            SearchHit<ThreadDoc> hit = mock(SearchHit.class);
            when(hit.getId()).thenReturn(ids[i]);
            if (i == 0) {
                first = hit;
            } else {
                rest[i - 1] = hit;
            }
        }
        when(iterator.next()).thenReturn(first, rest);
        return iterator;
    }

    // 测试全量重建清理 MySQL 中已失效的文档
    @Test
    void shouldPurgeStaleDocsAfterFullRebuild() {
        ESIndexServiceImpl service = createService();
        givenEmptyDatabase();
        SearchHitsIterator<ThreadDoc> iterator = iteratorOf("THREAD_1", "POST-2", "POST-3");
        when(elasticsearchOperations.searchForStream(any(Query.class), eq(ThreadDoc.class)))
                .thenReturn(iterator);

        service.initIndex();

        verify(esIndexManager).ensureIndices();
        ArgumentCaptor<Iterable<String>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(threaddRepository).deleteAllById(captor.capture());
        assertIterableEquals(List.of("THREAD_1", "POST-2", "POST-3"), captor.getValue());
    }

    // 测试索引中无文档时不执行删除
    @Test
    void shouldNotDeleteWhenIndexIsEmpty() {
        ESIndexServiceImpl service = createService();
        givenEmptyDatabase();
        SearchHitsIterator<ThreadDoc> emptyIterator = mock(SearchHitsIterator.class);
        when(emptyIterator.hasNext()).thenReturn(false);
        when(elasticsearchOperations.searchForStream(any(Query.class), eq(ThreadDoc.class)))
                .thenReturn(emptyIterator);

        service.initIndex();

        verify(threaddRepository, never()).deleteAllById(any());
    }

    // 测试重建进行中重复触发被忽略
    @Test
    void shouldIgnoreConcurrentRebuild() {
        ESIndexServiceImpl service = createService();
        AtomicBoolean running = (AtomicBoolean) ReflectionTestUtils.getField(service, "running");
        running.set(true);

        service.initIndex();

        verifyNoInteractions(esIndexManager);
        verifyNoInteractions(elasticsearchOperations);
    }

    // 测试重建失败释放运行锁并抛出异常
    @Test
    void shouldReleaseLockAndRethrowWhenRebuildFails() {
        ESIndexServiceImpl service = createService();
        doThrow(new RuntimeException("es down")).when(esIndexManager).ensureIndices();

        assertThrows(RuntimeException.class, service::initIndex);

        AtomicBoolean running = (AtomicBoolean) ReflectionTestUtils.getField(service, "running");
        org.junit.jupiter.api.Assertions.assertFalse(running.get());
    }
}
