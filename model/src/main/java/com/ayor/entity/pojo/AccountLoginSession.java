package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("account_login_session")
public class AccountLoginSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sessionId;

    private Integer accountId;

    private String jwtId;

    private String ipAddress;

    private String userAgent;

    private String osName;

    private String browserName;

    private String deviceType;

    private Date loginTime;

    private Date expireTime;

    private Date revokedTime;
}
