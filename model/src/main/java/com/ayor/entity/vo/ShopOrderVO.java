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
public class ShopOrderVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 142L;

    private Long orderId;

    private Integer accountId;

    private String username;

    private String nickname;

    private Integer itemId;

    private String itemName;

    private Long price;

    private Integer quantity;

    private Integer status;

    private Date createTime;
}
