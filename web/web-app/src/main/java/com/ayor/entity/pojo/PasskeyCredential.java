package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("passkey_credential")
/**
 * Passkey 凭证数据库实体。
 */
public class PasskeyCredential {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("account_id")
    private Integer accountId;

    @TableField("credential_id")
    private String credentialId;

    @TableField("user_handle")
    private String userHandle;

    @TableField("attestation_object")
    private String attestationObject;

    @TableField("client_data_json")
    private String clientDataJson;

    @TableField("signature_count")
    private Long signatureCount;

    @TableField("transports")
    private String transports;

    @TableField("backup_eligible")
    private Boolean backupEligible;

    @TableField("backup_state")
    private Boolean backupState;

    @TableField("uv_initialized")
    private Boolean uvInitialized;

    @TableField("label")
    private String label;

    @TableField("last_used_at")
    private Date lastUsedAt;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;
}
