package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.documennt.ThreadDoc;

import java.util.Set;

public interface SearchService {


    PageEntity<ThreadDoc> searchThreads(String keyword, Integer userId, int pageNum, int pageSize);

    Set<String> getSearchHistory(Integer userId);

    String removeSearchHistory(String keyword, Integer userId);

    String removeSearchHistory(Integer userId);
}
