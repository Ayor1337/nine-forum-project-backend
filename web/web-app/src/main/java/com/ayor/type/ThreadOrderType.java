package com.ayor.type;

import lombok.Getter;

@Getter
public enum ThreadOrderType {

    HOT("hot"),

    LATEST("latest"),

    LIKES("likes"),

    COLLECTS("collects"),

    VIEWS("views"),

    REPLIES("replies");

    private final String value;

    ThreadOrderType(String value) {
        this.value = value;
    }

    public static ThreadOrderType fromValue(String value) {
        for (ThreadOrderType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return HOT;
    }
}
