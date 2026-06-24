package com.ayor.type;

import lombok.Getter;

@Getter
public enum UnreadMessageType {

    REPLY_MESSAGE("reply"),

    MENTION_MESSAGE("mention"),

    FOLLOW_MESSAGE("follow"),

    SYSTEM_MESSAGE("system"),

    USER_MESSAGE("user");

    private final String type;

    UnreadMessageType(String type) {
        this.type = type;
    }
}
