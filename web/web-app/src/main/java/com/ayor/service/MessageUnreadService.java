package com.ayor.service;

import com.ayor.entity.stomp.MessageUnread;
import com.ayor.type.UnreadMessageType;

public interface MessageUnreadService {

    Long getUnread(Integer userId, UnreadMessageType type);

    Long getUnread(Integer userId, String type);

    MessageUnread getUnreadVO(Integer userId, UnreadMessageType type);

    MessageUnread getUnreadVO(Integer userId, String type);

    MessageUnread getUnreadVO(Integer userId);

    Long clearUnread(Integer userId, UnreadMessageType type, Long value);

    Long clearUnread(Integer userId, UnreadMessageType type);

    long addUnread(Integer userId, UnreadMessageType type, Long value);
}
