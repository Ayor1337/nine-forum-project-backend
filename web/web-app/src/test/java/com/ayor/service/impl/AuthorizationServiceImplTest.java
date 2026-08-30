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

    // 测试所有者可管理版块和主题
    @Test
    void shouldAllowOwnerToManageThemeAndTopic() {
        AuthorizationServiceImpl service = createService();
        when(roleMapper.getRoleNameByUserId(1)).thenReturn("OWNER");

        assertDoesNotThrow(() -> service.assertCanManageTheme(1));
        assertDoesNotThrow(() -> service.assertCanManageTopic(1));
    }

    // 测试范围版主可在主题内管理删除帖子串
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

    // 测试拒绝非作者在用户帖子串删除即使带有权限
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

    // 测试帖子串主题不匹配时拒绝范围版主
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

    // 测试允许范围版主到更新帖子串标签带有指定权限
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

    // 测试范围版主拥有指定权限时可设置公告
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

    // 测试允许所有者到设置全局公告
    @Test
    void shouldAllowOwnerToSetGlobalAnnouncement() {
        AuthorizationServiceImpl service = createService();
        when(roleMapper.getRoleNameByUserId(1)).thenReturn("OWNER");

        Threadd thread = new Threadd();
        thread.setThreadId(19);
        thread.setTopicId(66);
        thread.setAccountId(5);
        thread.setIsDeleted(false);
        when(threaddMapper.selectById(19)).thenReturn(thread);

        assertDoesNotThrow(() -> service.assertCanSetGlobalAnnouncement(1, 19));
    }

    // 测试允许用户带有全局设置公告权限到设置全局公告
    @Test
    void shouldAllowUserWithGlobalSetAnnouncementPermissionToSetGlobalAnnouncement() {
        AuthorizationServiceImpl service = createService();
        when(roleMapper.getRoleNameByUserId(8)).thenReturn("MODERATOR");
        when(permissionMapper.getPermissionsByAccountId(8)).thenReturn(List.of("SET_ANNOUNCEMENT"));

        Threadd thread = new Threadd();
        thread.setThreadId(19);
        thread.setTopicId(66);
        thread.setAccountId(5);
        thread.setIsDeleted(false);
        when(threaddMapper.selectById(19)).thenReturn(thread);

        assertDoesNotThrow(() -> service.assertCanSetGlobalAnnouncement(8, 19));
    }

    // 测试拒绝全局公告不带全局权限
    @Test
    void shouldDenyGlobalAnnouncementWithoutGlobalPermission() {
        AuthorizationServiceImpl service = createService();
        when(roleMapper.getRoleNameByUserId(8)).thenReturn("MODERATOR");
        when(permissionMapper.getPermissionsByAccountId(8)).thenReturn(List.of("DELETE_THREAD"));

        Threadd thread = new Threadd();
        thread.setThreadId(19);
        thread.setTopicId(66);
        thread.setAccountId(5);
        thread.setIsDeleted(false);
        when(threaddMapper.selectById(19)).thenReturn(thread);

        assertThrows(AccessDeniedException.class, () -> service.assertCanSetGlobalAnnouncement(8, 19));
    }

    // 测试拒绝全局公告当帖子串已删除
    @Test
    void shouldDenyGlobalAnnouncementWhenThreadDeleted() {
        AuthorizationServiceImpl service = createService();

        Threadd thread = new Threadd();
        thread.setThreadId(19);
        thread.setTopicId(66);
        thread.setAccountId(5);
        thread.setIsDeleted(true);
        when(threaddMapper.selectById(19)).thenReturn(thread);

        assertThrows(AccessDeniedException.class, () -> service.assertCanSetGlobalAnnouncement(1, 19));
    }

    // 测试允许范围版主到删除帖子带有删除帖子权限
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

    // 测试允许范围版主到更新已绑定主题
    @Test
    void shouldAllowScopedModeratorToUpdateBoundTopic() {
        AuthorizationServiceImpl service = createService();
        when(roleMapper.getRoleNameByUserId(8)).thenReturn("MODERATOR");
        when(roleMapper.getTopicIdByUserId(8)).thenReturn(66);
        when(permissionMapper.getPermissionsByAccountId(8)).thenReturn(List.of("UPDATE_TOPIC"));

        assertDoesNotThrow(() -> service.assertCanUpdateTopic(8, 66));
    }

    // 测试拒绝范围版主到更新未绑定主题
    @Test
    void shouldDenyScopedModeratorToUpdateUnboundTopic() {
        AuthorizationServiceImpl service = createService();
        when(roleMapper.getRoleNameByUserId(8)).thenReturn("MODERATOR");
        when(roleMapper.getTopicIdByUserId(8)).thenReturn(66);
        when(permissionMapper.getPermissionsByAccountId(8)).thenReturn(List.of("UPDATE_TOPIC"));

        assertThrows(AccessDeniedException.class, () -> service.assertCanUpdateTopic(8, 77));
    }

    // 测试允许全局创建主题权限不带主题范围
    @Test
    void shouldAllowGlobalCreateTopicPermissionWithoutTopicScope() {
        AuthorizationServiceImpl service = createService();
        when(roleMapper.getRoleNameByUserId(8)).thenReturn("MODERATOR");
        when(permissionMapper.getPermissionsByAccountId(8)).thenReturn(List.of("CREATE_TOPIC"));

        assertDoesNotThrow(() -> service.assertCanCreateTopic(8));
    }

    // 测试作者可删除自己的帖子
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

    // 测试作者可编辑自己的帖子
    @Test
    void shouldAllowAuthorToEditOwnPost() {
        AuthorizationServiceImpl service = createService();
        Post post = new Post();
        post.setPostId(31);
        post.setAccountId(9);
        post.setTopicId(44);
        post.setIsDeleted(false);
        when(postMapper.selectById(31)).thenReturn(post);

        assertDoesNotThrow(() -> service.assertCanEditPost(9, 31));
    }

    // 测试拒绝非作者在用户帖子编辑即使带有权限
    @Test
    void shouldDenyNonAuthorOnUserPostEditEvenWithPermission() {
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

        assertThrows(AccessDeniedException.class, () -> service.assertCanEditPost(8, 31));
    }

    // 测试范围版主拥有删帖权限时可查看帖子编辑快照
    @Test
    void shouldAllowScopedModeratorToViewPostEditSnapshotsWithDeletePostPermission() {
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

        assertDoesNotThrow(() -> service.assertCanViewPostEditSnapshots(8, 31));
    }

    // 测试拒绝非作者在用户帖子删除即使带有权限
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

    // 测试拒绝会话访问用于非参与者
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

    // 测试拒绝发送会话消息当拉黑任一方向
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

    // 测试未互相拉黑的会话参与者可发送消息
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

    // 测试拒绝发起会话当目标拒绝私信
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
