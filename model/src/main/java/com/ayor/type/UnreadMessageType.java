package com.ayor.type;

import lombok.Getter;

@Getter
public enum UnreadMessageType {

    REPLY_MESSAGE("reply"),

    SYSTEM_MESSAGE("system"),

    USER_MESSAGE("user");

    private final String type;

    UnreadMessageType(String type) {
        this.type = type;
    }
}
