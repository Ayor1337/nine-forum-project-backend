package com.ayor.service.impl;

import com.ayor.entity.pojo.AccountLoginSession;
import com.ayor.entity.vo.LoginSessionVO;
import com.ayor.mapper.LoginSessionMapper;
import com.ayor.service.UserLoginSessionService;
import com.ayor.util.CONST;
import com.ayor.util.JWTUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.longThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserLoginSessionServiceImplTest {

    @Mock
    private LoginSessionMapper loginSessionMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void shouldCreateSessionRecordAndActiveRedisKey() {
        UserLoginSessionService service = service();
        Date expireTime = new Date(System.currentTimeMillis() + 60_000);
        HttpServletRequest request = request("203.0.113.8",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/124.0 Safari/537.36");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service.createSession(7, "session-1", "jwt-1", expireTime, request);

        ArgumentCaptor<AccountLoginSession> captor = ArgumentCaptor.forClass(AccountLoginSession.class);
        verify(loginSessionMapper).insert(captor.capture());
        AccountLoginSession session = captor.getValue();
        assertEquals(7, session.getAccountId());
        assertEquals("session-1", session.getSessionId());
        assertEquals("jwt-1", session.getJwtId());
        assertEquals("203.0.113.8", session.getIpAddress());
        assertEquals("Windows", session.getOsName());
        assertEquals("Chrome", session.getBrowserName());
        assertEquals("Desktop", session.getDeviceType());
        verify(valueOperations).set(eq(CONST.LOGIN_SESSION_ACTIVE + "session-1"), eq("7"),
                longThat(ttl -> ttl > 0 && ttl <= 60_000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void shouldMarkCurrentSessionWhenListingSessions() {
        UserLoginSessionService service = service();
        LoginSessionVO first = new LoginSessionVO();
        first.setSessionId("session-1");
        LoginSessionVO second = new LoginSessionVO();
        second.setSessionId("session-2");
        when(loginSessionMapper.listByAccountId(7)).thenReturn(List.of(first, second));

        List<LoginSessionVO> sessions = service.listSessions(7, "session-2");

        assertTrue(sessions.get(1).isCurrent());
    }

    @Test
    void shouldRevokeOwnedNonCurrentSessionWithoutStoringToken() {
        UserLoginSessionService service = service();
        AccountLoginSession session = new AccountLoginSession();
        session.setAccountId(7);
        session.setSessionId("session-1");
        session.setJwtId("jwt-1");
        session.setExpireTime(new Date(System.currentTimeMillis() + 60_000));
        when(loginSessionMapper.findBySessionId("session-1")).thenReturn(session);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String result = service.revokeSession(7, "session-1", "session-current");

        assertNull(result);
        verify(redisTemplate).delete(CONST.LOGIN_SESSION_ACTIVE + "session-1");
        verify(valueOperations).set(eq(CONST.JWT_BLACK_LIST + "jwt-1"), eq(""),
                longThat(ttl -> ttl > 0 && ttl <= 60_000L), eq(TimeUnit.MILLISECONDS));
        verify(loginSessionMapper).markRevoked(eq("session-1"), org.mockito.ArgumentMatchers.any(Date.class));
    }

    private UserLoginSessionService service() {
        return new UserLoginSessionServiceImpl(loginSessionMapper, redisTemplate);
    }

    private static HttpServletRequest request(String remoteAddress, String userAgent) {
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getHeader("User-Agent")).thenReturn(userAgent);
        when(request.getRemoteAddr()).thenReturn(remoteAddress);
        return request;
    }
}
