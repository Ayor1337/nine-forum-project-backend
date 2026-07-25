package com.ayor.type;

import lombok.Getter;

@Getter
public enum ShopItemType {

    BADGE("badge"),

    AVATAR_FRAME("avatar_frame"),

    TITLE("title");

    private final String type;

    ShopItemType(String type) {
        this.type = type;
    }

    public static ShopItemType fromType(String type) {
        if (type == null) {
            return null;
        }
        for (ShopItemType itemType : values()) {
            if (itemType.type.equals(type)) {
                return itemType;
            }
        }
        return null;
    }

    /**
     * 是否为单装备类型（同类型同时只能装备一件）
     */
    public boolean isSingleEquip() {
        return this == AVATAR_FRAME || this == TITLE;
    }
}
