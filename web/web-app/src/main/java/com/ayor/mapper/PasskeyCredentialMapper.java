package com.ayor.mapper;

import com.ayor.entity.pojo.PasskeyCredential;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface PasskeyCredentialMapper extends BaseMapper<PasskeyCredential> {

    default PasskeyCredential findByCredentialId(String credentialId) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PasskeyCredential>()
                .eq(PasskeyCredential::getCredentialId, credentialId)
                .last("limit 1"));
    }

    @Update("""
            update passkey_credential
            set signature_count = #{signatureCount},
                last_used_at = #{lastUsedAt},
                update_time = #{lastUsedAt}
            where id = #{id}
            """)
    int updateAuthenticationState(@Param("id") Long id,
                                  @Param("signatureCount") Long signatureCount,
                                  @Param("lastUsedAt") java.util.Date lastUsedAt);
}
