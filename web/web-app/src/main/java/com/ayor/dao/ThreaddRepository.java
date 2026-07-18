package com.ayor.dao;

import com.ayor.entity.document.ThreadDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThreaddRepository extends ElasticsearchRepository<ThreadDoc, String> {

    void deleteByThreadId(Integer threadId);

}
