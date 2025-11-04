package com.ayor.service;

import com.ayor.entity.stomp.MessageUnread;

public interface MessageUnreadService {
    Long getUnread(String user);

    MessageUnread getUnreadVO(String user);

    Long clearUnread(String user, Long value);

    long addUnread(String user, Long value);
}
