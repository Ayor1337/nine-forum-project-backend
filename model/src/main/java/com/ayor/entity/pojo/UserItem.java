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
@TableName("user_item")
public class UserItem {

    @TableId(type = IdType.AUTO)
    private Long userItemId;

    private Integer accountId;

    private Integer itemId;

    private Integer quantity;

    private Boolean isEquipped;

    private Date acquireTime;
}
