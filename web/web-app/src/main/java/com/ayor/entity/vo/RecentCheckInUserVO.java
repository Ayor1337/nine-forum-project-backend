package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 最近签到用户展示信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentCheckInUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 119L;

    private Integer accountId;

    private String username;

    private String nickname;

    private String avatarUrl;

    private Date checkInTime;
}
