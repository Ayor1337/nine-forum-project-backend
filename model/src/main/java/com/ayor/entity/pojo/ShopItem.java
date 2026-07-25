package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("shop_item")
public class ShopItem {

    @TableId(type = IdType.AUTO)
    private Integer itemId;

    private String name;

    private String itemKey;

    private String description;

    private String itemType;

    private Long price;

    private Long stock;

    private Integer purchaseLimit;

    private Integer status;

    private Boolean isDeleted;

    private Date createTime;

    private Date updateTime;
}
