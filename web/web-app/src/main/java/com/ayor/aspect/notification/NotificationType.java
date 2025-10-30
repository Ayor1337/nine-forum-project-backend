package com.ayor.aspect.notification;

import lombok.Getter;

@Getter
public enum NotificationType {

    RECEIVED_MSG("RECEIVED_MSG");


    private final String type;

    NotificationType(String type) {
        this.type = type;
    }


}
