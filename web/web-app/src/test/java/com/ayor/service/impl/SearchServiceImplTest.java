package com.ayor.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ayor.dao.SearchLogDocRepository;
import com.ayor.entity.PageEntity;
import com.ayor.entity.document.ThreadDoc;
import com.ayor.service.UserRelationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHitsImpl;
import org.springframework.data.elasticsearch.core.TotalHitsRelation;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock
    private SearchLogDocRepository searchLogDocRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ElasticsearchOperations operations;

    @Mock
    private UserRelationService userRelationService;

    // 测试存在过滤条件时搜索帖子串仍要求关键词匹配
    @Test
    void searchThreadsShouldRequireKeywordMatchWhenFiltersExist() {
        SearchServiceImpl service = createService();
        when(operations.search(any(NativeQuery.class), eq(ThreadDoc.class))).thenReturn(emptyHits());

        service.searchThreads(" spring ", null, null, false, true, null, null, "rel", 1, 10);

        BoolQuery boolQuery = capturedBoolQuery();
        assertEquals(1, boolQuery.must().size());
        assertTrue(boolQuery.must().get(0).isMultiMatch());
        assertEquals("spring", boolQuery.must().get(0).multiMatch().query());
        assertTrue(boolQuery.should().isEmpty());
    }

    // 测试搜索帖子串时把元数据条件放入过滤器
    @Test
    void searchThreadsShouldPutMetadataConditionsInFilter() {
        SearchServiceImpl service = createService();
        when(operations.search(any(NativeQuery.class), eq(ThreadDoc.class))).thenReturn(emptyHits());

        service.searchThreads("spring", null, 7, false, true, 1_700_000_000_000L, 1_700_086_400_000L, "rel", 1, 10);

        BoolQuery boolQuery = capturedBoolQuery();
        assertEquals(1, boolQuery.must().size());
        assertEquals(3, boolQuery.filter().size());
        assertTrue(boolQuery.filter().stream().anyMatch(query -> isTermFilterOn(query, "topicId")));
        assertTrue(boolQuery.filter().stream().anyMatch(query -> isTermFilterOn(query, "isThreadTopic")));
        assertTrue(boolQuery.filter().stream().anyMatch(query -> query.isRange()));
    }

    // 测试搜索帖子串返回空分页用于空关键词不带副作用
    @Test
    void searchThreadsShouldReturnEmptyPageForBlankKeywordWithoutSideEffects() {
        SearchServiceImpl service = createService();

        PageEntity<ThreadDoc> result = service.searchThreads("   ", 1, null, true, false, null, null, "rel", 1, 10);

        assertEquals(0L, result.getTotalSize());
        assertEquals(List.of(), result.getData());
        verifyNoInteractions(redisTemplate, searchLogDocRepository, operations);
    }

    // 测试帖子串文档的过滤字段会被索引
    @Test
    void threadDocFilterFieldsShouldBeIndexed() throws NoSuchFieldException {
        assertTrue(ThreadDoc.class.getDeclaredField("topicId").getAnnotation(Field.class).index());
        assertTrue(ThreadDoc.class.getDeclaredField("createTime").getAnnotation(Field.class).index());
        assertTrue(ThreadDoc.class.getDeclaredField("isThreadTopic").getAnnotation(Field.class).index());
        assertTrue(ThreadDoc.class.getDeclaredField("accountId").getAnnotation(Field.class).index());
        assertTrue(ThreadDoc.class.getDeclaredField("threadId").getAnnotation(Field.class).index());
    }

    // 测试搜索帖子串排除拉黑作者
    @Test
    void searchThreadsShouldExcludeBlockedAuthors() {
        SearchServiceImpl service = createService();
        when(userRelationService.listBlockedAccountIdsEitherDirection(5)).thenReturn(List.of(11, 12));
        when(operations.search(any(NativeQuery.class), eq(ThreadDoc.class))).thenReturn(emptyHits());

        service.searchThreads("spring", 5, null, false, false, null, null, "rel", 1, 10);

        BoolQuery boolQuery = capturedBoolQuery();
        assertTrue(boolQuery.mustNot().stream().anyMatch(query -> query.isTerms()
                && "accountId".equals(query.terms().field())));
    }

    // 测试搜索历史被原子化裁剪并过期
    @Test
    void searchHistoryShouldBeAtomicallyTrimmedAndExpired() {
        SearchServiceImpl service = createService();
        when(operations.search(any(NativeQuery.class), eq(ThreadDoc.class))).thenReturn(emptyHits());

        service.searchThreads("spring", 5, null, true, false, null, null, "rel", 1, 10);

        verify(redisTemplate).execute(
                any(RedisScript.class),
                eq(List.of("search:history:5")),
                anyString(),
                eq("spring"),
                eq("50"),
                eq("7776000")
        );
    }

    private SearchServiceImpl createService() {
        return new SearchServiceImpl(null, searchLogDocRepository, redisTemplate, operations, userRelationService);
    }

    private BoolQuery capturedBoolQuery() {
        ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);
        verify(operations).search(queryCaptor.capture(), eq(ThreadDoc.class));
        Query query = queryCaptor.getValue().getQuery();
        assertTrue(query.isBool());
        return query.bool();
    }

    private boolean isTermFilterOn(Query query, String field) {
        return query.isTerm() && field.equals(query.term().field());
    }

    private SearchHits<ThreadDoc> emptyHits() {
        return new SearchHitsImpl<>(
                0L,
                TotalHitsRelation.EQUAL_TO,
                0f,
                Duration.ZERO,
                null,
                null,
                List.of(),
                null,
                null,
                null
        );
    }
}
