package com.ayor.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String bio;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String location;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date birthday;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String website;
}
