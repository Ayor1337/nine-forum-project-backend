package com.ayor.type;

import lombok.Getter;

@Getter
public enum MentionSourceType {

    THREAD("THREAD"),

    POST("POST");

    private final String value;

    MentionSourceType(String value) {
        this.value = value;
    }
}
