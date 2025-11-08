package com.ayor.service;

import com.ayor.entity.message.UserSystemMessage;

public interface BroadcastService {

    <T> void userSystemBroadcast(UserSystemMessage<T> message);

}
