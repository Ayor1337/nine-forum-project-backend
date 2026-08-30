package com.ayor.type;

public enum DecorationStatus {

    DRAFT(1),
    PUBLISHED(2),
    ARCHIVED(3);

    private final int code;

    DecorationStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static DecorationStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DecorationStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
