package com.ayor.result;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {

    // 测试成功结果带数据时使用成功状态码和消息
    @Test
    void okWithDataUsesSuccessCodeAndMessage() {
        Result<String> result = Result.ok("payload");

        assertThat(result.getCode()).isEqualTo(ResultCodeEnum.SUCCESS.getCode());
        assertThat(result.getMessage()).isEqualTo(ResultCodeEnum.SUCCESS.getMessage());
        assertThat(result.getData()).isEqualTo("payload");
    }

    // 测试成功结果不带数据时数据为空
    @Test
    void okWithoutDataLeavesDataNull() {
        Result<Void> result = Result.ok();

        assertThat(result.getCode()).isEqualTo(ResultCodeEnum.SUCCESS.getCode());
        assertThat(result.getMessage()).isEqualTo(ResultCodeEnum.SUCCESS.getMessage());
        assertThat(result.getData()).isNull();
    }

    // 测试失败结果带自定义状态码和消息且不设置数据
    @Test
    void failWithCustomCodeAndMessageDoesNotSetData() {
        Result<Object> result = Result.fail(403, "forbidden");

        assertThat(result.getCode()).isEqualTo(403);
        assertThat(result.getMessage()).isEqualTo("forbidden");
        assertThat(result.getData()).isNull();
    }

    // 测试消息处理器将空消息转为成功且文本消息转为失败
    @Test
    void messageHandlerConvertsNullMessageToSuccessAndTextToFailure() {
        Result<Void> success = Result.messageHandler(() -> null);
        Result<Void> failure = Result.messageHandler(() -> "bad request");

        assertThat(success.getCode()).isEqualTo(ResultCodeEnum.SUCCESS.getCode());
        assertThat(failure.getCode()).isEqualTo(ResultCodeEnum.FAIL.getCode());
        assertThat(failure.getMessage()).isEqualTo("bad request");
    }

    // 测试数据消息处理器将空数据转为失败
    @Test
    void dataMessageHandlerConvertsNullDataToFailure() {
        Result<String> success = Result.dataMessageHandler(() -> "value", "missing");
        Result<String> failure = Result.dataMessageHandler(() -> null, "missing");

        assertThat(success.getData()).isEqualTo("value");
        assertThat(success.getCode()).isEqualTo(ResultCodeEnum.SUCCESS.getCode());
        assertThat(failure.getCode()).isEqualTo(ResultCodeEnum.FAIL.getCode());
        assertThat(failure.getMessage()).isEqualTo("missing");
        assertThat(failure.getData()).isNull();
    }

    // 测试JSON序列化会写出空字段
    @Test
    void toJsonStringWritesNullFields() {
        String json = Result.ok().toJSONString();

        assertThat(json).contains("\"code\":200");
        assertThat(json).contains("\"data\":null");
    }
}
