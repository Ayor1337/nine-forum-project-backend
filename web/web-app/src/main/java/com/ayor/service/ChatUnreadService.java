package com.ayor.service;

public interface ChatUnreadService {



    Long getUnread(Integer conversationId, Integer fromUserId);

    long clearUnread(Integer conversationId, Integer fromUserId);

    long addUnread(Integer conversationId, Integer fromUserId);
}
