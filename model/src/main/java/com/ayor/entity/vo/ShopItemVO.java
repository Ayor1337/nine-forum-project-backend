package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 140L;

    private Integer itemId;

    private String name;

    private String itemKey;

    private String description;

    private String itemType;

    private Integer decorationId;

    private Long price;

    private Long stock;

    private Integer purchaseLimit;

    private Integer status;
}
