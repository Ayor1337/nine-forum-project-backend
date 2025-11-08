package com.ayor.service;

import com.ayor.entity.stomp.MessageUnread;
import com.ayor.type.UnreadMessageType;

public interface MessageUnreadService {

    Long getUnread(String user, UnreadMessageType type);

    Long getUnread(String user, String type);

    MessageUnread getUnreadVO(String user, UnreadMessageType type);

    MessageUnread getUnreadVO(String user, String type);

    MessageUnread getUnreadVO(String user);

    Long clearUnread(String user, UnreadMessageType type, Long value);

    Long clearUnread(String user, UnreadMessageType type);

    long addUnread(String user, UnreadMessageType type, Long value);
}
