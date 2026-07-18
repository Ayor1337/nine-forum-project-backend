package com.ayor.service;

/**
 * 权限校验服务接口
 *
 * 统一封装论坛系统中的各类权限校验逻辑，包括主题/分区管理、标签操作、
 * 帖子/评论编辑删除、会话访问等场景的鉴权。
 *
 * 主要功能:
 * - 主题/分区管理权限校验
 * - 标签操作权限校验
 * - 帖子/评论编辑删除权限校验
 * - 会话访问权限校验
 *
 * 技术特性:
 * - 基于角色和权限模型的细粒度访问控制
 * - 覆盖前台用户和后台管理两种场景
 *
 * @author ayor
 * @since 1.0.0
 */
public interface AuthorizationService {

    void assertCanManageTheme(Integer actorId);

    void assertCanManageTopic(Integer actorId);

    void assertCanCreateTheme(Integer actorId);

    void assertCanCreateTopic(Integer actorId);

    void assertCanUpdateTopic(Integer actorId, Integer topicId);

    void assertCanDeleteTopic(Integer actorId, Integer topicId);

    void assertCanCreateTag(Integer actorId, Integer topicId);

    void assertCanUpdateThreadTag(Integer actorId, Integer threadId, Integer topicId);

    void assertCanSetAnnouncement(Integer actorId, Integer threadId, Integer topicId);

    void assertCanSetGlobalAnnouncement(Integer actorId, Integer threadId);

    void assertCanModerateDeleteThread(Integer actorId, Integer threadId, Integer topicId);

    void assertCanModerateDeletePost(Integer actorId, Integer postId);

    void assertCanDeleteThread(Integer actorId, Integer threadId);

    void assertCanEditThread(Integer actorId, Integer threadId);

    void assertCanViewThreadEditSnapshots(Integer actorId, Integer threadId, Integer topicId);

    void assertCanDeletePost(Integer actorId, Integer postId);

    void assertCanEditPost(Integer actorId, Integer postId);

    void assertCanViewPostEditSnapshots(Integer actorId, Integer postId);

    void assertCanStartConversation(Integer actorId, Integer targetUserId);

    void assertCanSendConversationMessage(Integer actorId, Integer conversationId);

    void assertCanAccessConversation(Integer actorId, Integer conversationId);

    void assertCanClearConversationUnread(Integer actorId, Integer conversationId, Integer fromUserId);
}
