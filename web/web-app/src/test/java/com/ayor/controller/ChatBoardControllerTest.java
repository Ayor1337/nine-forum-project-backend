package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.ChatboardHistoryVO;
import com.ayor.service.ChatboardHistoryService;
import com.ayor.util.SecurityUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatBoardControllerTest {

    // 测试获取历史时把当前用户ID传给服务
    @Test
    void getHistoryShouldPassCurrentUserIdToService() {
        ChatboardHistoryService chatboardHistoryService = mock(ChatboardHistoryService.class);
        SecurityUtils securityUtils = mock(SecurityUtils.class);
        ChatBoardController controller = new ChatBoardController(chatboardHistoryService, securityUtils);
        when(securityUtils.getSecurityUserId()).thenReturn(7);
        when(chatboardHistoryService.getChatboardHistory(7, 3, 1, 10))
                .thenReturn(new PageEntity<ChatboardHistoryVO>(0L, List.of()));

        controller.getHistory(3, 1, 10);

        verify(chatboardHistoryService).getChatboardHistory(7, 3, 1, 10);
    }
}
