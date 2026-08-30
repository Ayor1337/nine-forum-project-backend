package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditBalanceVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 130L;

    private Integer accountId;

    private Long balance;
}
