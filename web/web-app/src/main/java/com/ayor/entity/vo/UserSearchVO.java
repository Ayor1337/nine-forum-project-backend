package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 118L;

    private Integer accountId;

    private String username;

    private String nickname;

    private String avatarUrl;
}
