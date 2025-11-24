package com.ayor.service.impl;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import com.ayor.dao.SearchLogDocRepository;
import com.ayor.dao.ThreaddRepository;
import com.ayor.entity.PageEntity;
import com.ayor.entity.app.documennt.SearchLogDoc;
import com.ayor.entity.app.documennt.ThreadDoc;
import com.ayor.entity.app.vo.HotKeywordVO;
import com.ayor.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
@Transactional
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ThreaddRepository threaddRepository;

    private final SearchLogDocRepository searchLogDocRepository;

    private final StringRedisTemplate redisTemplate;

    private final ElasticsearchOperations operations;

    private String buildKey(Integer userId) {
        return "search:history:" + userId;
    }


    @Override
    public PageEntity<ThreadDoc> searchThreads(String keyword, Integer userId, int pageNum, int pageSize) {
        pageNum = Math.max(pageNum, 1);
        insertSearchHistory(keyword, userId);
        Page<ThreadDoc> threads = threaddRepository.findThreaddByTitleOrContentIgnoreCase(
                keyword,
                keyword,
                PageRequest.of(pageNum - 1, pageSize,
                        Sort.by(Sort.Direction.DESC, "createTime")));

        if (userId != null) {
            SearchLogDoc searchLogDoc = SearchLogDoc.builder()
                    .id(null)
                    .keyword(keyword)
                    .userId(String.valueOf(userId))
                    .ts(Instant.now())
                    .build();
            searchLogDocRepository.save(searchLogDoc);
        }

        return new PageEntity<>(threads.getTotalElements(), threads.getContent());
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
