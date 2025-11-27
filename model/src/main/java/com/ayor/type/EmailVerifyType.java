package com.ayor.type;

import lombok.Getter;

@Getter
public enum EmailVerifyType {


    REGISTER("register");

    private final String type;

    EmailVerifyType(String  type) {
        this.type = type;
    }
}
