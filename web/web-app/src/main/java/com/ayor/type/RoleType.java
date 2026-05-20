package com.ayor.type;

public enum RoleType {
    OWNER;

    public static boolean isOwner(String roleName) {
        return OWNER.name().equalsIgnoreCase(roleName);
    }
}
