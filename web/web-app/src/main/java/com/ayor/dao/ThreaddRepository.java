package com.ayor.dao;

import com.ayor.entity.app.documennt.ThreadDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreaddRepository extends ElasticsearchRepository<ThreadDoc, Integer> {

    Page<ThreadDoc> findThreaddByTitleOrContentIgnoreCase(String title, String content, Pageable pageable);

}
