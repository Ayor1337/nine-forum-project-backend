package com.ayor.service;

import com.ayor.entity.vo.LoginSessionVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.List;

public interface UserLoginSessionService {

    void createSession(Integer accountId, String sessionId, String jwtId, Date expireTime, HttpServletRequest request);

    List<LoginSessionVO> listSessions(Integer accountId, String currentSessionId);

    String revokeSession(Integer accountId, String sessionId, String currentSessionId);

    void revokeCurrentSession(String sessionId);
}
