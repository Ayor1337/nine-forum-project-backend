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

    private String itemKey;

    private String itemType;

    private Integer quantity;

    private Boolean isEquipped;

    /**
     * 已发布装扮配置（JSON 文本，商品未绑定装扮时为空，前端回退 itemKey 渲染）
     */
    private String decorationConfig;

    private Date acquireTime;
}
