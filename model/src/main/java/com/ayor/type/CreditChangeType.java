package com.ayor.type;

import lombok.Getter;

@Getter
public enum CreditChangeType {

    ADMIN_GRANT("admin_grant"),

    ADMIN_DEDUCT("admin_deduct"),

    PURCHASE("purchase"),

    DAILY_CHECK_IN("daily_check_in");

    private final String type;

    CreditChangeType(String type) {
        this.type = type;
    }

    public static CreditChangeType fromAmount(long amount) {
        return amount >= 0 ? ADMIN_GRANT : ADMIN_DEDUCT;
    }
}
