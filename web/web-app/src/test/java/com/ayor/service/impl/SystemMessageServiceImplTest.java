package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.SystemMessage;
import com.ayor.entity.vo.SystemMessageVO;
import com.ayor.mapper.SystemMessageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SystemMessageServiceImplTest {

    @Mock
    private SystemMessageMapper systemMessageMapper;

    private SystemMessageServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SystemMessageServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", systemMessageMapper);
    }

    // 测试列表系统消息拒绝缺失账号并无效分页
    @Test
    void listSystemMessageRejectsMissingAccountAndInvalidPage() {
        assertThat(service.listSystemMessage(1, 10, null)).isNull();
        assertThat(service.listSystemMessage(0, 10, 7)).isNull();
        verify(systemMessageMapper, never()).selectPage(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    // 测试消息VO映射不暴露Mapper内部细节
    @Test
    void toVOListMapsMessagesWithoutExposingMapperInternals() {
        SystemMessage message = new SystemMessage();
        message.setSystemMessageId(3);
        message.setAccountId(7);
        message.setTitle("标题");
        message.setContent("内容");
        message.setCreateTime(new Date());

        @SuppressWarnings("unchecked")
        List<SystemMessageVO> result = (List<SystemMessageVO>) ReflectionTestUtils.invokeMethod(
                service, "toVOList", List.of(message));

        assertThat(result).singleElement().satisfies(vo -> {
            assertThat(vo.getSystemMessageId()).isEqualTo(3);
            assertThat(vo.getTitle()).isEqualTo("标题");
            assertThat(vo.getContent()).isEqualTo("内容");
        });
    }
}
