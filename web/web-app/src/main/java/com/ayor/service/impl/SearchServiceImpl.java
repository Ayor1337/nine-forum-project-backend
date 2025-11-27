package com.ayor.service.impl;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.json.JsonData;
import com.ayor.dao.SearchLogDocRepository;
import com.ayor.dao.ThreaddRepository;
import com.ayor.entity.PageEntity;
import com.ayor.entity.app.documennt.SearchLogDoc;
import com.ayor.entity.app.documennt.ThreadDoc;
import com.ayor.entity.app.vo.HotKeywordVO;
import com.ayor.service.SearchService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
@Transactional
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ThreaddRepository threaddRepository;

    private final SearchLogDocRepository searchLogDocRepository;

    private final StringRedisTemplate redisTemplate;

    private final ElasticsearchOperations operations;

    static final List<String> ORDER = List.of("asc", "desc", "rel");

    private String buildKey(Integer userId) {
        return "search:history:" + userId;
    }

    @Override
    public PageEntity<ThreadDoc> searchThreads(String keyword,
                                               Integer userId,
                                               Integer topicId,
                                               boolean enableHistory,
                                               boolean onlyThreadTopic,
                                               Long startTime,
                                               Long endTime,
                                               String order,
                                               int pageNum,
                                               int pageSize) {
        pageNum = Math.max(pageNum, 1);

        // 插入搜索历史
        if (enableHistory && userId != null) {
            insertSearchHistory(keyword, userId);
        }

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        boolQuery.must(m -> m.
                        match(t -> t
                                .field("title")
                                .query(keyword)
                        )
                )
                .must(m -> m
                        .match(t -> t
                                .field("content")
                                .query(keyword)
                        )
                );
        // 如果有时间范围，则做时间筛选
        if (startTime != null && endTime != null) {


            String gte = Instant.ofEpochMilli(startTime).toString();
            String lte = Instant.ofEpochMilli(endTime).toString();

            boolQuery.must(m -> m
                    .range(r -> r
                            .date(d -> d.
                                    field("createTime")
                                    .gte(gte)
                                    .lte(lte)
                            )
                    )
            );
        }

        // 如果有主题则对主题进行判断
        if (topicId != null) {
            boolQuery.must(m -> m.match(t -> t.field("topicId").query(topicId)));
        }

        // 如果是只搜索主题帖
        if (onlyThreadTopic) {
            boolQuery.must(m -> m.match(t -> t.field("isThreadTopic").query(true)));
        }

        // 构建查询
        NativeQueryBuilder nativeQueryBuilder = NativeQuery.builder()
                .withQuery(q -> q.bool(boolQuery.build()));

        // 如果有排序方式则按照排序方式来设置，如果没有则按照得分排序
        if (order != null && ORDER.contains(order)) {
            switch (order) {
                case "asc" -> nativeQueryBuilder.withSort(Sort.by(Sort.Direction.ASC, "createTime"));
                case "desc" -> nativeQueryBuilder.withSort(Sort.by(Sort.Direction.DESC, "createTime"));
                case "rel" -> nativeQueryBuilder.withSort(Sort.by(Sort.Direction.DESC, "_score"));
            }
        } else {
            nativeQueryBuilder.withSort(Sort.by(Sort.Direction.DESC, "_score"));
        }
        NativeQuery query = nativeQueryBuilder
                .withPageable(PageRequest.of(pageNum - 1, pageSize))
                .build();
        // 搜索
        SearchHits<ThreadDoc> threads = operations.search(query, ThreadDoc.class);

        List<ThreadDoc> list = threads.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .toList();

        // 保存搜索记录，用来统计搜索
        if (userId != null) {
            SearchLogDoc searchLogDoc = SearchLogDoc.builder()
                    .id(null)
                    .keyword(keyword)
                    .userId(String.valueOf(userId))
                    .ts(Instant.now())
                    .build();
            searchLogDocRepository.save(searchLogDoc);
        }

        return new PageEntity<>(threads.getTotalHits(), list);
    }

    @Override
    public Set<String> getSearchHistory(Integer userId) {
        return redisTemplate.opsForZSet()
                .reverseRange(buildKey(userId), 0, 5);
    }

    private void insertSearchHistory(String keyword, Integer userId) {
        if (keyword == null || keyword.isEmpty()) {
            return;
        }
    	redisTemplate.opsForZSet().add(buildKey(userId), keyword, System.currentTimeMillis());
    }

    @Override
    public String removeSearchHistory(String keyword, Integer userId) {
        Long removed = redisTemplate.opsForZSet().remove(buildKey(userId), keyword);
        if (removed != null && removed > 0) {
            return null;
        }
        return "删除失败";
    }

    @Override
    public String removeSearchHistory(Integer userId) {
        Boolean removed = redisTemplate.delete("search:history:" + userId);
        return removed ? null : "删除失败";
    }

    @Override
    public List<HotKeywordVO> getHotKeywords(int size, Duration duration) {

        NativeQuery query = NativeQuery.builder()
                .withAggregation("hot_keywords", Aggregation.of(a -> a
                        .terms(t -> t
                                .field("keyword")
                                .size(size)
                        )
                ))
                .withQuery(q -> q
                        .range(r -> r
                                .date(d -> d
                                        .field("ts")
                                        .gte("now-" + duration.toDays() + "d")
                                        .lte("now")
                                )
                        )
                )
                .withMaxResults(0)
                .build();

        SearchHits<SearchLogDoc> searchHits = operations.search(query, SearchLogDoc.class);

        ElasticsearchAggregations aggregations =
                (ElasticsearchAggregations) searchHits.getAggregations();

        List<StringTermsBucket> buckets = aggregations.aggregationsAsMap()
                .get("hot_keywords")
                .aggregation()
                .getAggregate()
                .sterms()
                .buckets()
                .array();
        List<HotKeywordVO> hotKeywords = new ArrayList<>();
        buckets.forEach(bucket -> {
            hotKeywords.add(new HotKeywordVO(
                    bucket.key().stringValue(),
                    bucket.docCount()));
        });
        return hotKeywords;
    }

}
