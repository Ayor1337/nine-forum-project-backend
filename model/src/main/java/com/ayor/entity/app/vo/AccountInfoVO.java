package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 120L;

    private Integer accountId;

    private String bio;

    private String location;

    private Date birthday;

    private String website;
}
