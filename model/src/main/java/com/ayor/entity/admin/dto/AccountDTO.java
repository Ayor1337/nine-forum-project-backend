package com.ayor.entity.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTO {

    private Integer accountId;

    private String username;

    private String nickname;

    private String avatarUrl;

    private String bannerUrl;

    private String bio;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    private Integer roleId;

    private Boolean isDeleted;
}
