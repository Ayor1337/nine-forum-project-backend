package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.LoginSessionVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;

public interface UserLoginSessionService {

    void createSession(Integer accountId, String sessionId, String jwtId, Date expireTime, HttpServletRequest request);

    PageEntity<LoginSessionVO> listSessions(Integer accountId, String currentSessionId, Integer pageNum, Integer pageSize);

    String revokeSession(Integer accountId, String sessionId, String currentSessionId);

    void revokeCurrentSession(String sessionId);
}
