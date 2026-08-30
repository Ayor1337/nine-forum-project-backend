package com.ayor.type;

public enum ShopItemStatus {

    LISTED(1),
    DELISTED(2);

    private final int code;

    ShopItemStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ShopItemStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ShopItemStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
