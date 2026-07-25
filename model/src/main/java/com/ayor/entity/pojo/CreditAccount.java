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
@TableName("credit_account")
public class CreditAccount {

    @TableId(type = IdType.INPUT)
    private Integer accountId;

    private Long balance;

    private Date createTime;

    private Date updateTime;
}
