package com.ayor.service;

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

    void assertCanModerateDeleteThread(Integer actorId, Integer threadId, Integer topicId);

    void assertCanModerateDeletePost(Integer actorId, Integer postId);

    void assertCanDeleteThread(Integer actorId, Integer threadId);

    void assertCanDeletePost(Integer actorId, Integer postId);

    void assertCanStartConversation(Integer actorId, Integer targetUserId);

    void assertCanAccessConversation(Integer actorId, Integer conversationId);

    void assertCanClearConversationUnread(Integer actorId, Integer conversationId, Integer fromUserId);
}
