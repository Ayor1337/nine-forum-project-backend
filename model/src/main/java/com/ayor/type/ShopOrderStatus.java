package com.ayor.type;

public enum ShopOrderStatus {

    SUCCESS(1),
    REFUNDED(2);

    private final int code;

    ShopOrderStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
