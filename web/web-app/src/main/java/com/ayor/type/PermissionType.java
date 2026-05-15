package com.ayor.type;

public enum PermissionType {
    INSERT_TAG("INSERT_TAG"),
    UPDATE_TAG("UPDATE_TAG"),
    DELETE_THREAD("sDELETE_THREAD");

    private final String dbValue;

    PermissionType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String dbValue() {
        return dbValue;
    }
}
