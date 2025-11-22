package com.ayor;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.ayor.dao.ThreaddRepository;
import com.ayor.entity.pojo.Threadd;
import com.ayor.service.ESIndexService;
import com.ayor.service.ThreaddService;
import com.ayor.util.QuillUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
@Slf4j
public class ElasticTest {

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private RestClient restClient;

    @Resource
    private ThreaddRepository threaddRepository;

    @Resource
    private ThreaddService threaddService;

    @Resource
    private ESIndexService esIndexService;

    @Resource
    private QuillUtils quillUtils;


    @Test
    public void createIndex() {
        log.info("索引创建成功");
    }

    @Test
    public void insertData() {
        esIndexService.initIndex();
    }

    @Test
    public void deleteIndex() {
        try {
            elasticsearchClient.indices()
                            .delete(builder -> builder.index("thread"));
            log.info("索引删除成功");
        } catch (IOException e) {
            log.error("索引删除失败: {} {}", e.getClass(), e.getMessage());
        }

    }

}
