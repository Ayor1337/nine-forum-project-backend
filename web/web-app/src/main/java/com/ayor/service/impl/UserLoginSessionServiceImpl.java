package com.ayor.service.impl;

import com.ayor.entity.pojo.AccountLoginSession;
import com.ayor.entity.vo.LoginSessionVO;
import com.ayor.mapper.LoginSessionMapper;
import com.ayor.service.UserLoginSessionService;
import com.ayor.util.CONST;
import com.ayor.util.LoginDeviceInfo;
import com.ayor.util.SimpleUserAgentParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserLoginSessionServiceImpl implements UserLoginSessionService {

    private static final int MAX_HEADER_VALUE_LENGTH = 512;

    private final LoginSessionMapper loginSessionMapper;

    private final StringRedisTemplate redisTemplate;

    @Override
    public void createSession(Integer accountId, String sessionId, String jwtId, Date expireTime, HttpServletRequest request) {
        String userAgent = trimToLength(request.getHeader("User-Agent"), MAX_HEADER_VALUE_LENGTH);
        LoginDeviceInfo deviceInfo = SimpleUserAgentParser.parse(userAgent);
        AccountLoginSession session = new AccountLoginSession();
        session.setAccountId(accountId);
        session.setSessionId(sessionId);
        session.setJwtId(jwtId);
        session.setIpAddress(resolveIpAddress(request));
        session.setUserAgent(userAgent);
        session.setOsName(deviceInfo.osName());
        session.setBrowserName(deviceInfo.browserName());
        session.setDeviceType(deviceInfo.deviceType());
        session.setLoginTime(new Date());
        session.setExpireTime(expireTime);
        loginSessionMapper.insert(session);
        redisTemplate.opsForValue().set(CONST.LOGIN_SESSION_ACTIVE + sessionId, String.valueOf(accountId),
                ttlMillis(expireTime), TimeUnit.MILLISECONDS);
    }

    @Override
    public List<LoginSessionVO> listSessions(Integer accountId, String currentSessionId) {
        return loginSessionMapper.listByAccountId(accountId).stream()
                .peek(session -> session.setCurrent(session.getSessionId().equals(currentSessionId)))
                .toList();
    }

    @Override
    public String revokeSession(Integer accountId, String sessionId, String currentSessionId) {
        if (sessionId.equals(currentSessionId)) {
            return "不能踢出当前会话";
        }
        AccountLoginSession session = loginSessionMapper.findBySessionId(sessionId);
        if (session == null || !accountId.equals(session.getAccountId())) {
            return "会话不存在";
        }
        revoke(session);
        return null;
    }

    @Override
    public void revokeCurrentSession(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return;
        }
        AccountLoginSession session = loginSessionMapper.findBySessionId(sessionId);
        if (session != null) {
            revoke(session);
        }
    }

    private void revoke(AccountLoginSession session) {
        redisTemplate.delete(CONST.LOGIN_SESSION_ACTIVE + session.getSessionId());
        redisTemplate.opsForValue().set(CONST.JWT_BLACK_LIST + session.getJwtId(), "",
                ttlMillis(session.getExpireTime()), TimeUnit.MILLISECONDS);
        loginSessionMapper.markRevoked(session.getSessionId(), new Date());
    }

    private static String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = firstIp(request.getHeader("X-Forwarded-For"));
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor;
        }
        String realIp = request.getHeader("X-Real-IP");
        return StringUtils.hasText(realIp) ? realIp : request.getRemoteAddr();
    }

    private static String firstIp(String forwardedFor) {
        if (!StringUtils.hasText(forwardedFor)) {
            return null;
        }
        return forwardedFor.split(",")[0].trim();
    }

    private static String trimToLength(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private static long ttlMillis(Date expireTime) {
        return Math.max(expireTime.getTime() - System.currentTimeMillis(), 0);
    }
}
