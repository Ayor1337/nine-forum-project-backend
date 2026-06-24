package com.ayor.service.impl;

import com.ayor.entity.pojo.ChatboardHistory;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ChatboardHistoryMapper;
import com.ayor.service.UserRelationService;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatboardHistoryServiceImplTest {

    @Mock
    private ChatboardHistoryMapper chatboardHistoryMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private UserRelationService userRelationService;

    // 测试排除拉黑账号从聊天室历史
    @Test
    void shouldExcludeBlockedAccountsFromChatboardHistory() {
        ChatboardHistoryServiceImpl service = new ChatboardHistoryServiceImpl(
                accountMapper,
                simpMessagingTemplate,
                userRelationService
        );
        ReflectionTestUtils.setField(service, "baseMapper", chatboardHistoryMapper);
        when(userRelationService.listBlockedAccountIdsEitherDirection(7)).thenReturn(List.of(11, 12));

        Page<ChatboardHistory> page = Page.of(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        when(chatboardHistoryMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        service.getChatboardHistory(7, 1, 1, 10);

        ArgumentCaptor<Wrapper<ChatboardHistory>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(chatboardHistoryMapper).selectPage(any(Page.class), wrapperCaptor.capture());

        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), ChatboardHistory.class);
        String targetSql = wrapperCaptor.getValue().getTargetSql();
        assertTrue(targetSql.contains("account_id NOT IN"), targetSql);
    }
}
