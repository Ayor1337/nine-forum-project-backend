package com.ayor.entity.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Data
@Schema(description = "登录会话视图对象")
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
