package com.ayor.type;

import lombok.Getter;

@Getter
public enum UserViolationType {

    NICKNAME_VIOLATION("nickname"),

    BANNER_VIOLATION("banner"),

    AVATAR_VIOLATION("avatar");

    private final String value;

    UserViolationType(String value) {
        this.value = value;
    }

}
