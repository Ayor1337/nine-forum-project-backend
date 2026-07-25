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
public class CreditTransactionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 131L;

    private Long transactionId;

    private Integer accountId;

    private String username;

    private String nickname;

    private Long delta;

    private Long balanceAfter;

    private String changeType;

    private String remark;

    private Integer operatorId;

    private String operatorNickname;

    private Date createTime;
}
