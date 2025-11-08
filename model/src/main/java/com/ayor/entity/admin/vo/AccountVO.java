package com.ayor.entity.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountVO {

    private Integer accountId;

    private String username;

    private String nickname;

    private String bio;

    private String avatarUrl;

    private String bannerUrl;

    private Integer status;

    private Boolean isDeleted;
}
