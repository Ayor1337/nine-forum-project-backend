package com.ayor.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ES 索引结构管理：按 resources/es 下的 JSON 定义创建索引并校验 mapping。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ESIndexManager {

    public static final String THREAD_INDEX = "thread";

    public static final String SEARCH_LOG_INDEX = "search_log";

    private final ElasticsearchOperations elasticsearchOperations;

    private final ObjectMapper objectMapper;

    /**
     * 确保所有索引存在且 mapping 与定义一致，供启动初始化与重建前调用。
     */
    public void ensureIndices() {
        List.of(
                Map.entry(THREAD_INDEX, "es/thread.json"),
                Map.entry(SEARCH_LOG_INDEX, "es/search-log.json")
        ).forEach(entry -> ensureIndex(entry.getKey(), entry.getValue()));
    }

    private void ensureIndex(String indexName, String definitionPath) {
        Map<String, Object> definition = readDefinition(definitionPath);
        Map<String, Object> settings = cast(definition.get("settings"));
        Map<String, Object> mappings = cast(definition.get("mappings"));

        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
        if (!indexOps.exists()) {
            indexOps.create(settings);
            indexOps.putMapping(Document.from(mappings));
            log.info("Elastic | 索引 [{}] 创建成功", indexName);
            return;
        }
        verifyMapping(indexOps, indexName, mappings);
    }

    private void verifyMapping(IndexOperations indexOps, String indexName, Map<String, Object> expectedMappings) {
        Set<String> missingFields = new HashSet<>(propertiesOf(expectedMappings));
        missingFields.removeAll(propertiesOf(indexOps.getMapping()));
        if (!missingFields.isEmpty()) {
            log.warn("Elastic | 索引 [{}] mapping 与定义不一致, 缺失字段: {}, 请触发全量重建", indexName, missingFields);
        }
    }

    private Set<String> propertiesOf(Map<String, Object> mappings) {
        if (mappings.get("properties") instanceof Map<?, ?> properties) {
            return properties.keySet().stream().map(Object::toString).collect(Collectors.toSet());
        }
        return Set.of();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> cast(Object value) {
        return (Map<String, Object>) value;
    }

    private Map<String, Object> readDefinition(String definitionPath) {
        try (InputStream inputStream = new ClassPathResource(definitionPath).getInputStream()) {
            return cast(objectMapper.readValue(inputStream, Map.class));
        } catch (IOException e) {
            throw new IllegalStateException("Elastic | 索引定义文件读取失败: " + definitionPath, e);
        }
    }
}
