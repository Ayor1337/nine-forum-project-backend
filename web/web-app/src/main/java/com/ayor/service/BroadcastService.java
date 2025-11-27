package com.ayor.service;

import com.ayor.aspect.unread.MessageUnreadNotif;
import com.ayor.entity.message.UserSystemMessage;
import com.ayor.entity.message.UserViolationMessage;
import com.ayor.type.UnreadMessageType;

public interface BroadcastService {

    <T> void userSystemBroadcast(UserSystemMessage<T> message);

    <T> void userViolationBroadcast(UserViolationMessage<T> message);
}
