package com.ayor.type;

import lombok.Getter;

@Getter
public enum NotificationType {

    RECEIVED_MSG("RECEIVED_MSG"),

    SEND_MSG("SEND_MSG");

    private final String type;

    NotificationType(String type) {
        this.type = type;
    }


}
