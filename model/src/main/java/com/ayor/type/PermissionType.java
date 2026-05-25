package com.ayor.type;

import java.util.Arrays;

public enum PermissionType {
    INSERT_TAG("INSERT_TAG"),
    UPDATE_TAG("UPDATE_TAG"),
    DELETE_THREAD("DELETE_THREAD");

    private final String dbValue;

    PermissionType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String dbValue() {
        return dbValue;
    }

    public static boolean isKnown(String permission) {
        return Arrays.stream(values())
                .anyMatch(permissionType -> permissionType.dbValue.equals(permission));
    }
}
