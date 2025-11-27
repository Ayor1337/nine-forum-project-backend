package com.ayor.dao;

import com.ayor.entity.app.documennt.SearchLogDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchLogDocRepository extends ElasticsearchRepository<SearchLogDoc, Integer> {

}
