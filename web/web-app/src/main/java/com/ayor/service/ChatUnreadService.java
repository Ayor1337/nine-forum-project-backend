package com.ayor.service;

public interface ChatUnreadService {


    Long getUnread(Integer conversationId, String fromUser);

    long clearUnread(Integer conversationId, String fromUser);

    long addUnread(Integer conversationId, String fromUser);
}
