package com.ayor.service.impl;

import com.ayor.dao.ThreaddRepository;
import com.ayor.entity.PageEntity;
import com.ayor.entity.app.documennt.ThreadDoc;
import com.ayor.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;


@Service
@Transactional
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ThreaddRepository threaddRepository;

    private final StringRedisTemplate redisTemplate;

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

}
