package com.ayor.service.impl;

import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Conversation;
import com.ayor.entity.pojo.Post;
import com.ayor.entity.pojo.Threadd;
import com.ayor.entity.pojo.UserPrivacySetting;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ConversationMapper;
import com.ayor.mapper.PermissionMapper;
import com.ayor.mapper.PostMapper;
import com.ayor.mapper.RoleMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.AuthorizationService;
import com.ayor.service.UserPrivacySettingService;
import com.ayor.service.UserRelationService;
import com.ayor.type.DmPermission;
import com.ayor.type.PermissionType;
import com.ayor.type.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 论坛授权服务实现。
 *
 * <p>该服务集中处理论坛管理、主题范围权限、内容删除和私信会话访问的授权断言。
 * 调用方只需要传入当前操作者与目标资源标识；如果校验失败，本实现会统一抛出
 * {@link AccessDeniedException}。</p>
 */
@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private static final String ACCESS_DENIED = "Access denied";

    private final RoleMapper roleMapper;

    private final PermissionMapper permissionMapper;

    private final ThreaddMapper threaddMapper;

    private final PostMapper postMapper;

    private final ConversationMapper conversationMapper;

    private final AccountMapper accountMapper;

    private final UserRelationService userRelationService;

    private final UserPrivacySettingService userPrivacySettingService;

    /**
     * 断言操作者可以管理论坛主题分类。
     *
     * @param actorId 操作者账号 ID
     * @throws AccessDeniedException 当操作者不是论坛所有者时抛出
     */
    @Override
    public void assertCanManageTheme(Integer actorId) {
        assertOwner(actorId);
    }

    /**
     * 断言操作者可以管理话题。
     *
     * @param actorId 操作者账号 ID
     * @throws AccessDeniedException 当操作者不是论坛所有者时抛出
     */
    @Override
    public void assertCanManageTopic(Integer actorId) {
        assertOwner(actorId);
    }

    /**
     * 断言操作者可以在指定话题下创建标签。
     *
     * @param actorId 操作者账号 ID
     * @param topicId 目标话题 ID
     * @throws AccessDeniedException 当操作者没有目标话题的新增标签权限时抛出
     */
    @Override
    public void assertCanCreateTag(Integer actorId, Integer topicId) {
        assertTopicPermission(actorId, topicId, PermissionType.INSERT_TAG);
    }

    /**
     * 断言操作者可以更新指定帖子的话题标签。
     *
     * <p>目标帖子必须存在、未删除，并且归属于传入的话题。</p>
     *
     * @param actorId 操作者账号 ID
     * @param threadId 目标帖子 ID
     * @param topicId 目标话题 ID
     * @throws AccessDeniedException 当帖子无效、帖子不属于目标话题，或操作者没有更新标签权限时抛出
     */
    @Override
    public void assertCanUpdateThreadTag(Integer actorId, Integer threadId, Integer topicId) {
        Threadd thread = requireActiveThread(threadId);
        assertTopicBoundThread(thread, topicId);
        assertTopicPermission(actorId, topicId, PermissionType.UPDATE_TAG);
    }

    /**
     * 断言操作者可以设置或取消指定帖子的公告状态。
     *
     * <p>目标帖子必须存在、未删除，并且归属于传入的话题。当前公告管理复用话题内的标签更新权限。</p>
     *
     * @param actorId 操作者账号 ID
     * @param threadId 目标帖子 ID
     * @param topicId 目标话题 ID
     * @throws AccessDeniedException 当帖子无效、帖子不属于目标话题，或操作者没有更新标签权限时抛出
     */
    @Override
    public void assertCanSetAnnouncement(Integer actorId, Integer threadId, Integer topicId) {
        Threadd thread = requireActiveThread(threadId);
        assertTopicBoundThread(thread, topicId);
        assertTopicPermission(actorId, topicId, PermissionType.UPDATE_TAG);
    }

    /**
     * 断言操作者可以以版主管理身份删除指定帖子。
     *
     * <p>目标帖子必须存在、未删除，并且归属于传入的话题。</p>
     *
     * @param actorId 操作者账号 ID
     * @param threadId 目标帖子 ID
     * @param topicId 目标话题 ID
     * @throws AccessDeniedException 当帖子无效、帖子不属于目标话题，或操作者没有删帖权限时抛出
     */
    @Override
    public void assertCanModerateDeleteThread(Integer actorId, Integer threadId, Integer topicId) {
        Threadd thread = requireActiveThread(threadId);
        assertTopicBoundThread(thread, topicId);
        assertTopicPermission(actorId, topicId, PermissionType.DELETE_THREAD);
    }

    /**
     * 断言操作者可以删除指定帖子。
     *
     * <p>帖子作者可以删除自己的帖子；非作者需要拥有该帖子所属话题的删帖权限。</p>
     *
     * @param actorId 操作者账号 ID
     * @param threadId 目标帖子 ID
     * @throws AccessDeniedException 当操作者无效、帖子无效，或操作者没有删帖权限时抛出
     */
    @Override
    public void assertCanDeleteThread(Integer actorId, Integer threadId) {
        requireActor(actorId);
        Threadd thread = requireActiveThread(threadId);
        if (Objects.equals(thread.getAccountId(), actorId)) {
            return;
        }
        assertTopicPermission(actorId, thread.getTopicId(), PermissionType.DELETE_THREAD);
    }

    /**
     * 断言操作者可以删除指定回复。
     *
     * <p>回复作者可以删除自己的回复；非作者需要拥有该回复所属话题的删帖权限。</p>
     *
     * @param actorId 操作者账号 ID
     * @param postId 目标回复 ID
     * @throws AccessDeniedException 当操作者无效、回复无效，或操作者没有删帖权限时抛出
     */
    @Override
    public void assertCanDeletePost(Integer actorId, Integer postId) {
        requireActor(actorId);
        Post post = requireActivePost(postId);
        if (Objects.equals(post.getAccountId(), actorId)) {
            return;
        }
        assertTopicPermission(actorId, post.getTopicId(), PermissionType.DELETE_THREAD);
    }

    /**
     * 断言操作者可以向目标用户发起私信会话。
     *
     * <p>目标用户必须存在；双方任一方向存在拉黑关系时禁止发起。给自己发起会话直接放行。
     * 对其他目标用户，会按照目标用户的私信隐私设置校验：所有人、无人、关注者或互相关注。</p>
     *
     * @param actorId 操作者账号 ID
     * @param targetUserId 目标用户账号 ID
     * @throws AccessDeniedException 当操作者无效、目标用户不存在、双方存在拉黑关系，或不满足目标用户私信权限时抛出
     */
    @Override
    public void assertCanStartConversation(Integer actorId, Integer targetUserId) {
        requireActor(actorId);
        Account target = accountMapper.getAccountById(targetUserId);
        if (target == null) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }
        if (Objects.equals(actorId, targetUserId)) {
            return;
        }
        if (userRelationService.isBlockedEitherDirection(actorId, targetUserId)) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }
        UserPrivacySetting setting = userPrivacySettingService.getByAccountId(targetUserId);
        if (setting == null || setting.getDmPermission() == null) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }
        DmPermission permission = setting.getDmPermission();
        if (permission == DmPermission.EVERYONE) {
            return;
        }
        if (permission == DmPermission.NOBODY) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }
        if (permission == DmPermission.FOLLOWER_ONLY && userRelationService.isFollowing(actorId, targetUserId)) {
            return;
        }
        if (permission == DmPermission.MUTUAL_FOLLOW_ONLY && userRelationService.isMutualFollowing(actorId, targetUserId)) {
            return;
        }
        throw new AccessDeniedException(ACCESS_DENIED);
    }

    /**
     * 断言操作者可以访问指定私信会话。
     *
     * @param actorId 操作者账号 ID
     * @param conversationId 私信会话 ID
     * @throws AccessDeniedException 当操作者无效、会话无效，或操作者不是会话参与者时抛出
     */
    @Override
    public void assertCanAccessConversation(Integer actorId, Integer conversationId) {
        requireActor(actorId);
        Conversation conversation = requireConversation(conversationId);
        if (!isParticipant(actorId, conversation)) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }
    }

    /**
     * 断言操作者可以清除指定会话中来自某个用户的未读状态。
     *
     * <p>操作者必须是会话参与者，且 {@code fromUserId} 必须是该会话中的另一位参与者。</p>
     *
     * @param actorId 操作者账号 ID
     * @param conversationId 私信会话 ID
     * @param fromUserId 未读消息来源用户账号 ID
     * @throws AccessDeniedException 当会话无效、操作者不是参与者，或来源用户不是会话对方时抛出
     */
    @Override
    public void assertCanClearConversationUnread(Integer actorId, Integer conversationId, Integer fromUserId) {
        Conversation conversation = requireConversation(conversationId);
        if (!isParticipant(actorId, conversation)) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }
        Integer partnerId = Objects.equals(actorId, conversation.getAlphaAccountId())
                ? conversation.getBetaAccountId()
                : conversation.getAlphaAccountId();
        if (!Objects.equals(partnerId, fromUserId)) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }
    }

    /**
     * 断言操作者是论坛所有者。
     *
     * @param actorId 操作者账号 ID
     * @throws AccessDeniedException 当操作者无效或不是论坛所有者时抛出
     */
    private void assertOwner(Integer actorId) {
        requireActor(actorId);
        if (!RoleType.isOwner(roleMapper.getRoleNameByUserId(actorId))) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }
    }

    /**
     * 断言操作者拥有指定话题范围内的指定权限。
     *
     * <p>论坛所有者不受话题范围限制；其他用户必须具备对应权限，并且角色绑定的话题与目标话题一致。</p>
     *
     * @param actorId 操作者账号 ID
     * @param topicId 目标话题 ID
     * @param permission 需要校验的论坛权限
     * @throws AccessDeniedException 当操作者无效、话题无效，或权限及话题范围不匹配时抛出
     */
    private void assertTopicPermission(Integer actorId, Integer topicId, PermissionType permission) {
        requireActor(actorId);
        if (topicId == null) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }
        if (RoleType.isOwner(roleMapper.getRoleNameByUserId(actorId))) {
            return;
        }
        Integer scopedTopicId = roleMapper.getTopicIdByUserId(actorId);
        List<String> permissions = permissionMapper.getPermissionsByAccountId(actorId);
        boolean hasPermission = permissions != null && permissions.contains(permission.dbValue());
        if (!hasPermission || !Objects.equals(scopedTopicId, topicId)) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }
    }

    /**
     * 断言帖子归属于指定话题。
     *
     * @param thread 已加载的帖子实体
     * @param topicId 目标话题 ID
     * @throws AccessDeniedException 当帖子所属话题与目标话题不一致时抛出
     */
    private void assertTopicBoundThread(Threadd thread, Integer topicId) {
        if (!Objects.equals(thread.getTopicId(), topicId)) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }
    }

    /**
     * 获取有效帖子。
     *
     * @param threadId 帖子 ID
     * @return 未删除的帖子实体
     * @throws AccessDeniedException 当帖子不存在或已删除时抛出
     */
    private Threadd requireActiveThread(Integer threadId) {
        Threadd thread = threaddMapper.selectById(threadId);
        if (thread == null || Boolean.TRUE.equals(thread.getIsDeleted())) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }
        return thread;
    }

    /**
     * 获取有效回复。
     *
     * @param postId 回复 ID
     * @return 未删除的回复实体
     * @throws AccessDeniedException 当回复不存在或已删除时抛出
     */
    private Post requireActivePost(Integer postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || Boolean.TRUE.equals(post.getIsDeleted())) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }
        return post;
    }

    /**
     * 获取有效私信会话。
     *
     * @param conversationId 私信会话 ID
     * @return 未删除的私信会话实体
     * @throws AccessDeniedException 当会话不存在或已删除时抛出
     */
    private Conversation requireConversation(Integer conversationId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || Boolean.TRUE.equals(conversation.getIsDeleted())) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }
        return conversation;
    }

    /**
     * 判断账号是否为指定私信会话的参与者。
     *
     * @param actorId 操作者账号 ID
     * @param conversation 私信会话实体
     * @return 如果账号是会话任一参与者则返回 {@code true}
     */
    private boolean isParticipant(Integer actorId, Conversation conversation) {
        return Objects.equals(actorId, conversation.getAlphaAccountId())
                || Objects.equals(actorId, conversation.getBetaAccountId());
    }

    /**
     * 校验操作者账号 ID 是否有效。
     *
     * @param actorId 操作者账号 ID
     * @throws AccessDeniedException 当账号 ID 为空或小于等于 0 时抛出
     */
    private void requireActor(Integer actorId) {
        if (actorId == null || actorId <= 0) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }
    }
}
