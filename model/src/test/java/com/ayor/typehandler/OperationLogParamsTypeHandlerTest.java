package com.ayor.typehandler;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OperationLogParamsTypeHandlerTest {

    private final OperationLogParamsTypeHandler typeHandler = new OperationLogParamsTypeHandler();

    // 测试读取 JSON 对象作为 Map
    @Test
    void readsJsonObjectAsMap() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("params")).thenReturn("{\"topicId\":7,\"name\":\"java\"}");

        Map<String, Object> params = typeHandler.getNullableResult(resultSet, "params");

        assertThat(params).containsEntry("topicId", 7);
        assertThat(params).containsEntry("name", "java");
    }

    // 测试包装旧格式 JSON 数组作为参数
    @Test
    void wrapsLegacyJsonArrayAsArgs() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("params")).thenReturn("[7,\"java\"]");

        Map<String, Object> params = typeHandler.getNullableResult(resultSet, "params");

        assertThat(params).containsKey("args");
    }

    // 测试包装旧格式纯文本作为原始值
    @Test
    void wrapsLegacyPlainTextAsRawValue() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("params")).thenReturn("[ignored]");

        Map<String, Object> params = typeHandler.getNullableResult(resultSet, "params");

        assertThat(params).containsEntry("raw", "[ignored]");
    }
}
