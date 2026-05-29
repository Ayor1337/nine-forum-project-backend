package com.ayor.mapper;

import com.ayor.entity.pojo.AccountLoginSession;
import com.ayor.entity.vo.LoginSessionVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

public interface LoginSessionMapper extends BaseMapper<AccountLoginSession> {

    @Select("""
            SELECT session_id, ip_address, os_name, browser_name, device_type, login_time, expire_time, revoked_time
            FROM account_login_session
            WHERE account_id = #{accountId}
            ORDER BY login_time DESC
            """)
    List<LoginSessionVO> listByAccountId(@Param("accountId") Integer accountId);

    @Select("""
            SELECT *
            FROM account_login_session
            WHERE session_id = #{sessionId}
            LIMIT 1
            """)
    AccountLoginSession findBySessionId(@Param("sessionId") String sessionId);

    @Update("""
            UPDATE account_login_session
            SET revoked_time = #{revokedTime}
            WHERE session_id = #{sessionId}
              AND revoked_time IS NULL
            """)
    int markRevoked(@Param("sessionId") String sessionId, @Param("revokedTime") Date revokedTime);
}
