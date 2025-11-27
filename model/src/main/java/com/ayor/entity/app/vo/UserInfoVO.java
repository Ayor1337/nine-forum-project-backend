package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 116L;

    private Integer accountId;

    private String username;

    private String nickname;

    private String avatarUrl;

    private String bannerUrl;

    private String bio;

    private String status;

    private UserPermissionVO permission;

    private Date createTime;

    private Date updateTime;

}
