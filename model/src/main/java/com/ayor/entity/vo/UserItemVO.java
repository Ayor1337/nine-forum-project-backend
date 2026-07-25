package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 141L;

    private Long userItemId;

    private Integer itemId;

    private String name;

    private String itemType;

    private Integer quantity;

    private Boolean isEquipped;

    private Date acquireTime;
}
