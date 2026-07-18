package com.ayor.service.impl;

import com.ayor.dao.ThreaddRepository;
import com.ayor.entity.pojo.Post;
import com.ayor.entity.pojo.Threadd;
import com.ayor.entity.document.ThreadDoc;
import com.ayor.service.ESIndexManager;
import com.ayor.service.ESIndexService;
import com.ayor.service.PostService;
import com.ayor.service.ThreaddService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitsIterator;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ES 索引服务实现：保障索引结构、全量灌入有效数据并清理失效文档。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ESIndexServiceImpl implements ESIndexService {

    private static final int BATCH_SIZE = 500;

    private static final int PURGE_BATCH_SIZE = 1000;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final ThreaddRepository threaddRepository;

    private final ThreaddService threaddService;

    private final PostService postService;

    private final ESIndexManager esIndexManager;

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 全量重建索引：保障结构后灌入全部有效数据, 最后清理 MySQL 中已不存在的文档。
     * 重建过程中重复触发会被忽略。
     */
    @Override
    public void initIndex() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Elastic | 索引重建正在进行中, 忽略重复触发");
            return;
        }
        try {
            esIndexManager.ensureIndices();
            Set<String> validIds = new HashSet<>();
            validIds.addAll(initThreadIndex());
            validIds.addAll(initPostIndex());
            purgeStaleDocs(validIds);
            log.info("Elastic | 索引初始化成功");
        } catch (Exception e) {
            throw new RuntimeException("Elastic | 索引初始化失败: " + e.getMessage(), e);
        } finally {
            running.set(false);
        }
    }

    private Set<String> initThreadIndex() {
        Set<String> ids = new HashSet<>();
        long pageNum = 1;
        while (true) {
            Page<Threadd> page = threaddService
                    .lambdaQuery()
                    .eq(Threadd::getIsDeleted, false)
                    .page(Page.of(pageNum, BATCH_SIZE));
            List<Threadd> records = page.getRecords();
            if (records == null || records.isEmpty()) {
                break;
            }
            threaddRepository.saveAll(threaddService.toThreadDocs(records));
            records.forEach(record -> ids.add("THREAD_" + record.getThreadId()));
            pageNum++;
        }
        log.info("Elastic | Thread 索引初始化成功, 共 {} 条", ids.size());
        return ids;
    }

    private Set<String> initPostIndex() {
        Set<String> ids = new HashSet<>();
        long pageNum = 1;
        while (true) {
            Page<Post> page = postService
                    .lambdaQuery()
                    .eq(Post::getIsDeleted, false)
                    .orderByDesc(Post::getThreadId)
                    .page(Page.of(pageNum, BATCH_SIZE));
            List<Post> records = page.getRecords();
            if (records == null || records.isEmpty()) {
                break;
            }
            threaddRepository.saveAll(postService.toThreadDoc(records));
            records.forEach(record -> ids.add("POST-" + record.getPostId()));
            pageNum++;
        }
        log.info("Elastic | Post 索引初始化成功, 共 {} 条", ids.size());
        return ids;
    }

    /**
     * 清理 ES 中存在但 MySQL 已失效（删除或逻辑删除）的文档。
     */
    private void purgeStaleDocs(Set<String> validIds) {
        List<String> staleIds = new ArrayList<>();
        Query query = Query.findAll();
        query.setPageable(PageRequest.of(0, PURGE_BATCH_SIZE));
        query.addSourceFilter(new FetchSourceFilter(true, new String[]{}, null));
        try (SearchHitsIterator<ThreadDoc> stream = elasticsearchOperations.searchForStream(query, ThreadDoc.class)) {
            while (stream.hasNext()) {
                String id = stream.next().getId();
                if (!validIds.contains(id)) {
                    staleIds.add(id);
                }
            }
        }
        for (int from = 0; from < staleIds.size(); from += PURGE_BATCH_SIZE) {
            int to = Math.min(from + PURGE_BATCH_SIZE, staleIds.size());
            threaddRepository.deleteAllById(staleIds.subList(from, to));
        }
        if (!staleIds.isEmpty()) {
            log.info("Elastic | 清理失效索引文档 {} 条", staleIds.size());
        }
    }
}
