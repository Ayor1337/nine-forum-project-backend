package com.ayor.entity.vo;

import lombok.Data;

import java.util.Date;

@Data
public class LoginSessionVO {

    private String sessionId;

    private String ipAddress;

    private String osName;

    private String browserName;

    private String deviceType;

    private Date loginTime;

    private Date expireTime;

    private Date revokedTime;

    private boolean current;
}
