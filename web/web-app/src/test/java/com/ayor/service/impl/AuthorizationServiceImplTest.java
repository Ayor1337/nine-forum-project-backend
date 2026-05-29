package com.ayor.service.impl;

import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Conversation;
import com.ayor.entity.pojo.Post;
import com.ayor.entity.pojo.Threadd;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ConversationMapper;
import com.ayor.mapper.PermissionMapper;
import com.ayor.mapper.PostMapper;
import com.ayor.mapper.RoleMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.UserPrivacySettingService;
import com.ayor.service.UserRelationService;
import com.ayor.type.DmPermission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceImplTest {

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private PermissionMapper permissionMapper;

    @Mock
    private ThreaddMapper threaddMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private UserRelationService userRelationService;

    @Mock
    private UserPrivacySettingService userPrivacySettingService;

    @Test
    void shouldAllowOwnerToManageThemeAndTopic() {
        AuthorizationServiceImpl service = createService();
        when(roleMapper.getRoleNameByUserId(1)).thenReturn("OWNER");

        assertDoesNotThrow(() -> service.assertCanManageTheme(1));
        assertDoesNotThrow(() -> service.assertCanManageTopic(1));
    }

    @Test
    void shouldAllowScopedModeratorToModerateDeleteThreadWithinTopic() {
        AuthorizationServiceImpl service = createService();
        lenient().when(roleMapper.getRoleNameByUserId(8)).thenReturn("MODERATOR");
        lenient().when(roleMapper.getTopicIdByUserId(8)).thenReturn(66);
        lenient().when(permissionMapper.getPermissionsByAccountId(8)).thenReturn(List.of("DELETE_THREAD"));

        Threadd thread = new Threadd();
        thread.setThreadId(19);
        thread.setTopicId(66);
        thread.setAccountId(5);
        thread.setIsDeleted(false);
        when(threaddMapper.selectById(19)).thenReturn(thread);

        assertDoesNotThrow(() -> service.assertCanModerateDeleteThread(8, 19, 66));
    }

    @Test
    void shouldDenyNonAuthorOnUserThreadDeleteEvenWithPermission() {
        AuthorizationServiceImpl service = createService();
        lenient().when(roleMapper.getRoleNameByUserId(8)).thenReturn("MODERATOR");
        lenient().when(roleMapper.getTopicIdByUserId(8)).thenReturn(66);
        lenient().when(permissionMapper.getPermissionsByAccountId(8)).thenReturn(List.of("DELETE_THREAD"));

        Threadd thread = new Threadd();
        thread.setThreadId(19);
        thread.setTopicId(66);
        thread.setAccountId(5);
        thread.setIsDeleted(false);
        when(threaddMapper.selectById(19)).thenReturn(thread);

        assertThrows(AccessDeniedException.class, () -> service.assertCanDeleteThread(8, 19));
    }

    @Test
    void shouldDenyScopedModeratorWhenThreadTopicDoesNotMatch() {
        AuthorizationServiceImpl service = createService();
        Threadd thread = new Threadd();
        thread.setThreadId(19);
        thread.setTopicId(77);
        thread.setAccountId(5);
        thread.setIsDeleted(false);
        when(threaddMapper.selectById(19)).thenReturn(thread);

        assertThrows(AccessDeniedException.class, () -> service.assertCanUpdateThreadTag(8, 19, 66));
    }

    @Test
    void shouldAllowScopedModeratorToUpdateThreadTagWithSpecificPermission() {
        AuthorizationServiceImpl service = createService();
        when(roleMapper.getRoleNameByUserId(8)).thenReturn("MODERATOR");
        when(roleMapper.getTopicIdByUserId(8)).thenReturn(66);
        when(permissionMapper.getPermissionsByAccountId(8)).thenReturn(List.of("UPDATE_THREAD_TAG"));

        Threadd thread = new Threadd();
        thread.setThreadId(19);
        thread.setTopicId(66);
        thread.setAccountId(5);
        thread.setIsDeleted(false);
        when(threaddMapper.selectById(19)).thenReturn(thread);

        assertDoesNotThrow(() -> service.assertCanUpdateThreadTag(8, 19, 66));
    }

    @Test
    void shouldAllowScopedModeratorToSetAnnouncementWithSpecificPermission() {
        AuthorizationServiceImpl service = createService();
        when(roleMapper.getRoleNameByUserId(8)).thenReturn("MODERATOR");
        when(roleMapper.getTopicIdByUserId(8)).thenReturn(66);
        when(permissionMapper.getPermissionsByAccountId(8)).thenReturn(List.of("SET_ANNOUNCEMENT"));

        Threadd thread = new Threadd();
        thread.setThreadId(19);
        thread.setTopicId(66);
        thread.setAccountId(5);
        thread.setIsDeleted(false);
        when(threaddMapper.selectById(19)).thenReturn(thread);

        assertDoesNotThrow(() -> service.assertCanSetAnnouncement(8, 19, 66));
    }

    @Test
    void shouldAllowScopedModeratorToDeletePostWithDeletePostPermission() {
        AuthorizationServiceImpl service = createService();
        when(roleMapper.getRoleNameByUserId(8)).thenReturn("MODERATOR");
        when(roleMapper.getTopicIdByUserId(8)).thenReturn(66);
        when(permissionMapper.getPermissionsByAccountId(8)).thenReturn(List.of("DELETE_POST"));

        Post post = new Post();
        post.setPostId(31);
        post.setAccountId(9);
        post.setTopicId(66);
        post.setIsDeleted(false);
        when(postMapper.selectById(31)).thenReturn(post);

        assertDoesNotThrow(() -> service.assertCanModerateDeletePost(8, 31));
    }

    @Test
    void shouldAllowScopedModeratorToUpdateBoundTopic() {
        AuthorizationServiceImpl service = createService();
        when(roleMapper.getRoleNameByUserId(8)).thenReturn("MODERATOR");
        when(roleMapper.getTopicIdByUserId(8)).thenReturn(66);
        when(permissionMapper.getPermissionsByAccountId(8)).thenReturn(List.of("UPDATE_TOPIC"));

        assertDoesNotThrow(() -> service.assertCanUpdateTopic(8, 66));
    }

    @Test
    void shouldDenyScopedModeratorToUpdateUnboundTopic() {
        AuthorizationServiceImpl service = createService();
        when(roleMapper.getRoleNameByUserId(8)).thenReturn("MODERATOR");
        when(roleMapper.getTopicIdByUserId(8)).thenReturn(66);
        when(permissionMapper.getPermissionsByAccountId(8)).thenReturn(List.of("UPDATE_TOPIC"));

        assertThrows(AccessDeniedException.class, () -> service.assertCanUpdateTopic(8, 77));
    }

    @Test
    void shouldAllowGlobalCreateTopicPermissionWithoutTopicScope() {
        AuthorizationServiceImpl service = createService();
        when(roleMapper.getRoleNameByUserId(8)).thenReturn("MODERATOR");
        when(permissionMapper.getPermissionsByAccountId(8)).thenReturn(List.of("CREATE_TOPIC"));

        assertDoesNotThrow(() -> service.assertCanCreateTopic(8));
    }

    @Test
    void shouldAllowAuthorToDeleteOwnPost() {
        AuthorizationServiceImpl service = createService();
        Post post = new Post();
        post.setPostId(31);
        post.setAccountId(9);
        post.setTopicId(44);
        post.setIsDeleted(false);
        when(postMapper.selectById(31)).thenReturn(post);

        assertDoesNotThrow(() -> service.assertCanDeletePost(9, 31));
    }

    @Test
    void shouldDenyNonAuthorOnUserPostDeleteEvenWithPermission() {
        AuthorizationServiceImpl service = createService();
        lenient().when(roleMapper.getRoleNameByUserId(8)).thenReturn("MODERATOR");
        lenient().when(roleMapper.getTopicIdByUserId(8)).thenReturn(44);
        lenient().when(permissionMapper.getPermissionsByAccountId(8)).thenReturn(List.of("DELETE_POST"));

        Post post = new Post();
        post.setPostId(31);
        post.setAccountId(9);
        post.setTopicId(44);
        post.setIsDeleted(false);
        when(postMapper.selectById(31)).thenReturn(post);

        assertThrows(AccessDeniedException.class, () -> service.assertCanDeletePost(8, 31));
    }

    @Test
    void shouldDenyConversationAccessForNonParticipant() {
        AuthorizationServiceImpl service = createService();
        Conversation conversation = new Conversation();
        conversation.setConversationId(7);
        conversation.setAlphaAccountId(1);
        conversation.setBetaAccountId(2);
        conversation.setIsDeleted(false);
        when(conversationMapper.selectById(7)).thenReturn(conversation);

        assertThrows(AccessDeniedException.class, () -> service.assertCanAccessConversation(3, 7));
    }

    @Test
    void shouldDenySendConversationMessageWhenBlockedEitherDirection() {
        AuthorizationServiceImpl service = createService();
        Conversation conversation = new Conversation();
        conversation.setConversationId(7);
        conversation.setAlphaAccountId(10);
        conversation.setBetaAccountId(22);
        conversation.setIsDeleted(false);
        when(conversationMapper.selectById(7)).thenReturn(conversation);
        when(userRelationService.isBlockedEitherDirection(10, 22)).thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> service.assertCanSendConversationMessage(10, 7));
    }

    @Test
    void shouldAllowSendConversationMessageForParticipantWithoutBlock() {
        AuthorizationServiceImpl service = createService();
        Conversation conversation = new Conversation();
        conversation.setConversationId(7);
        conversation.setAlphaAccountId(10);
        conversation.setBetaAccountId(22);
        conversation.setIsDeleted(false);
        when(conversationMapper.selectById(7)).thenReturn(conversation);
        when(userRelationService.isBlockedEitherDirection(10, 22)).thenReturn(false);

        assertDoesNotThrow(() -> service.assertCanSendConversationMessage(10, 7));
    }

    @Test
    void shouldDenyStartConversationWhenTargetRejectsDm() {
        AuthorizationServiceImpl service = createService();
        Account target = new Account();
        target.setAccountId(22);
        when(accountMapper.getAccountById(22)).thenReturn(target);
        when(userRelationService.isBlockedEitherDirection(10, 22)).thenReturn(false);
        when(userPrivacySettingService.getByAccountId(22)).thenReturn(
                com.ayor.entity.pojo.UserPrivacySetting.builder()
                        .accountId(22)
                        .dmPermission(DmPermission.NOBODY)
                        .build()
        );

        assertThrows(AccessDeniedException.class, () -> service.assertCanStartConversation(10, 22));
    }

    private AuthorizationServiceImpl createService() {
        return new AuthorizationServiceImpl(
                roleMapper,
                permissionMapper,
                threaddMapper,
                postMapper,
                conversationMapper,
                accountMapper,
                userRelationService,
                userPrivacySettingService
        );
    }
}
