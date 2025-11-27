package com.ayor.service.impl;

import com.ayor.dao.ThreaddRepository;
import com.ayor.entity.pojo.Post;
import com.ayor.entity.pojo.Threadd;
import com.ayor.entity.pojo.Topic;
import com.ayor.service.ESIndexService;
import com.ayor.service.PostService;
import com.ayor.service.ThreaddService;
import com.ayor.util.QuillUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ELIndexServiceImpl implements ESIndexService {

    private final ThreaddRepository threaddRepository;

    private final ThreaddService threaddService;

    private final PostService postService;


    @Override
    public void initIndex() {
        try {
            initThreadIndex();
            initPostIndex();
        } catch (Exception e) {
            throw new RuntimeException("索引初始化失败: " + e.getMessage());
        }
    }

    private void initThreadIndex() {
        try {
            final int pageSize = 500;
            long pageNum = 1;
            while (true) {
                Page<Threadd> page = threaddService
                        .lambdaQuery()
                        .eq(Threadd::getIsDeleted, false)
                        .page(Page.of(pageNum, pageSize));
                List<Threadd> records = page.getRecords();
                if (records == null || records.isEmpty()) {
                    break;
                }
                threaddRepository.saveAll(threaddService.toThreadDocs(records));
                pageNum++;
            }
            log.info("Thread 索引初始化成功");
        } catch (Exception e) {
            throw new RuntimeException("Thread 索引初始化失败: " + e.getMessage());
        }
    }

    private void initPostIndex() {
        try {
            final int pageSize = 1000;
            long pageNum = 1;
            while (true) {
                Page<Post> page = postService
                        .lambdaQuery()
                        .eq(Post::getIsDeleted, false)
                        .orderByDesc(Post::getThreadId)
                        .page(Page.of(pageNum, pageSize));
                List<Post> records = page.getRecords();
                if (records == null || records.isEmpty()) {
                    break;
                }
                threaddRepository.saveAll(postService.toThreadDoc(records));
                pageNum++;
            }
            log.info("Post 索引初始化成功");
        } catch (Exception e) {
            throw new RuntimeException("Post 索引初始化失败: " + e.getMessage());
        }
    }


}
