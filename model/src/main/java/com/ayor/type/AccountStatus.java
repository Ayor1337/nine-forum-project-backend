package com.ayor.type;

public enum AccountStatus {
    ACTIVE(1),
    MUTED(2),
    BANNED(3);

    private final int code;

    AccountStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static AccountStatus fromCode(Integer code) {
        if (code == null || code == 0) {
            return ACTIVE;
        }
        for (AccountStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return ACTIVE;
    }
}
