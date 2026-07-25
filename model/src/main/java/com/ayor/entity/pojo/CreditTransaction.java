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
@TableName("credit_transaction")
public class CreditTransaction {

    @TableId(type = IdType.AUTO)
    private Long transactionId;

    private Integer accountId;

    private Long delta;

    private Long balanceAfter;

    private String changeType;

    private String remark;

    private Integer operatorId;

    private Date createTime;
}
