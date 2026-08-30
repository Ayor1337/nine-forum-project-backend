package com.ayor.util;

import com.ayor.entity.pojo.Conversation;
import com.ayor.entity.pojo.ConversationMessage;
import com.ayor.entity.vo.ConversationMessageVO;
import com.ayor.entity.vo.ConversationVO;
import com.ayor.entity.vo.UserInfoVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ConversationMessageMapper;
import com.ayor.mapper.ConversationUserSettingMapper;
import com.ayor.service.AccountService;
import com.ayor.service.PresenceService;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConversationViewUtilsTest {

    // 测试已撤回消息对发送者隐藏原文并显示发送者视角文案
    @Test
    void shouldHideRecalledMessageContentForSenderView() {
        AccountMapper accountMapper = mock(AccountMapper.class);
        AccountService accountService = mock(AccountService.class);
        ConversationMessageMapper messageMapper = mock(ConversationMessageMapper.class);
        ConversationViewUtils viewUtils = viewUtils(accountMapper, accountService, messageMapper);
        ConversationMessage message = recalledMessage(9, 7, 10);

        ConversationMessageVO result = viewUtils.toMessageVO(message, 10);

        assertNull(result.getContent());
        assertTrue(result.getIsDeleted());
        assertTrue(result.getDeletedBySender());
        assertEquals("你撤回了一条消息", result.getDisplayContent());
    }

    // 测试已撤回消息对接收方隐藏原文并显示对方视角文案
    @Test
    void shouldHideRecalledMessageContentForPartnerView() {
        AccountMapper accountMapper = mock(AccountMapper.class);
        AccountService accountService = mock(AccountService.class);
        ConversationMessageMapper messageMapper = mock(ConversationMessageMapper.class);
        ConversationViewUtils viewUtils = viewUtils(accountMapper, accountService, messageMapper);
        ConversationMessage message = recalledMessage(9, 7, 10);

        ConversationMessageVO result = viewUtils.toMessageVO(message, 22);

        assertNull(result.getContent());
        assertEquals(false, result.getDeletedBySender());
        assertEquals("对方撤回了一条消息", result.getDisplayContent());
    }

    // 测试会话摘要在最后消息撤回时返回固定摘要
    @Test
    void shouldUseRecallSummaryForLatestDeletedMessage() {
        AccountMapper accountMapper = mock(AccountMapper.class);
        AccountService accountService = mock(AccountService.class);
        ConversationMessageMapper messageMapper = mock(ConversationMessageMapper.class);
        ConversationViewUtils viewUtils = viewUtils(accountMapper, accountService, messageMapper);
        Conversation conversation = new Conversation();
        conversation.setConversationId(7);
        conversation.setAlphaAccountId(10);
        conversation.setBetaAccountId(22);
        conversation.setUpdateTime(new Date());
        UserInfoVO partner = new UserInfoVO();
        partner.setAccountId(22);
        ConversationMessage latest = recalledMessage(9, 7, 10);

        when(accountService.getUserInfo(22)).thenReturn(partner);
        when(messageMapper.selectOne(any())).thenReturn(latest);

        ConversationVO result = viewUtils.toConversationVO(conversation, 10);

        assertEquals(9, result.getLastMessageId());
        assertEquals("消息已撤回", result.getLastMessageContent());
        assertEquals(10, result.getLastMessageSenderId());
    }

    // 测试会话视图包含当前用户置顶和对方在线状态
    @Test
    void shouldIncludePinnedAndPartnerOnlineState() {
        AccountMapper accountMapper = mock(AccountMapper.class);
        AccountService accountService = mock(AccountService.class);
        ConversationMessageMapper messageMapper = mock(ConversationMessageMapper.class);
        ConversationUserSettingMapper settingMapper = mock(ConversationUserSettingMapper.class);
        PresenceService presenceService = mock(PresenceService.class);
        ConversationViewUtils viewUtils = new ConversationViewUtils(
                accountMapper,
                accountService,
                messageMapper,
                settingMapper,
                presenceService);
        Conversation conversation = new Conversation();
        conversation.setConversationId(7);
        conversation.setAlphaAccountId(10);
        conversation.setBetaAccountId(22);
        UserInfoVO partner = new UserInfoVO();
        partner.setAccountId(22);
        com.ayor.entity.pojo.ConversationUserSetting setting = new com.ayor.entity.pojo.ConversationUserSetting();
        setting.setPinned(true);

        when(accountService.getUserInfo(22)).thenReturn(partner);
        when(settingMapper.selectOne(any())).thenReturn(setting);
        when(presenceService.isOnline(22)).thenReturn(true);

        ConversationVO result = viewUtils.toConversationVO(conversation, 10);

        assertEquals(true, result.getPinned());
        assertEquals(true, result.getPartnerOnline());
    }

    private ConversationMessage recalledMessage(Integer messageId, Integer conversationId, Integer accountId) {
        ConversationMessage message = new ConversationMessage();
        message.setConversationMessageId(messageId);
        message.setConversationId(conversationId);
        message.setAccountId(accountId);
        message.setContent("secret");
        message.setCreateTime(new Date());
        message.setIsDeleted(true);
        return message;
    }

    private ConversationViewUtils viewUtils(AccountMapper accountMapper,
                                            AccountService accountService,
                                            ConversationMessageMapper messageMapper) {
        ConversationUserSettingMapper settingMapper = mock(ConversationUserSettingMapper.class);
        PresenceService presenceService = mock(PresenceService.class);
        return new ConversationViewUtils(accountMapper, accountService, messageMapper, settingMapper, presenceService);
    }
}
