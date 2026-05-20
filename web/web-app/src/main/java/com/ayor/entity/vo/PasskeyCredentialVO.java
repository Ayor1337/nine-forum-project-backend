package com.ayor.entity.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
/**
 * 当前账号绑定的 Passkey 列表项。
 */
public class PasskeyCredentialVO {

    private Long credentialId;

    private String label;

    private List<String> transports;

    private Date createTime;

    private Date lastUsedAt;
}
