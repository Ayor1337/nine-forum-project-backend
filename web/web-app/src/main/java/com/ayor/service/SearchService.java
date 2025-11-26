package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.documennt.ThreadDoc;
import com.ayor.entity.app.vo.HotKeywordVO;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public interface SearchService {


    PageEntity<ThreadDoc> searchThreads(String keyword,
                                        Integer userId,
                                        Integer topicId,
                                        boolean enableHistory,
                                        boolean onlyThreadTopic,
                                        Long startTime,
                                        Long endTime,
                                        String order,
                                        int pageNum,
                                        int pageSize);

    Set<String> getSearchHistory(Integer userId);

    String removeSearchHistory(String keyword, Integer userId);

    String removeSearchHistory(Integer userId);

    List<HotKeywordVO> getHotKeywords(int size, Duration duration);
}
