package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.longThat;
import static org.mockito.Mockito.doAnswer;
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

    // 测试创建会话记录并启用Redis键
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

    // 测试列出最近会话时分页并标记当前会话
    @Test
    void shouldPageRecentSessionsAndMarkCurrentSessionWhenListingSessions() {
        UserLoginSessionService service = service();
        LoginSessionVO first = new LoginSessionVO();
        first.setSessionId("session-1");
        LoginSessionVO second = new LoginSessionVO();
        second.setSessionId("session-2");
        when(loginSessionMapper.listByAccountId(eq(7), any(Date.class), eq(12), eq(0L)))
                .thenReturn(List.of(first, second));
        when(loginSessionMapper.countByAccountId(eq(7), any(Date.class))).thenReturn(2L);

        PageEntity<LoginSessionVO> page = service.listSessions(7, "session-2", 1, null);

        assertEquals(2L, page.getTotalSize());
        assertTrue(page.getData().get(1).isCurrent());
        verify(loginSessionMapper).listByAccountId(eq(7), any(Date.class), eq(12), eq(0L));
    }

    // 测试列出会话时使用六个月截断时间和偏移量
    @Test
    void shouldUseSixMonthCutoffAndOffsetWhenListingSessions() {
        UserLoginSessionService service = service();
        Date earliestAccepted = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(190));
        Date latestAccepted = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(170));
        when(loginSessionMapper.listByAccountId(eq(7), any(Date.class), eq(5), eq(10L)))
                .thenReturn(List.of());
        when(loginSessionMapper.countByAccountId(eq(7), any(Date.class))).thenReturn(0L);

        PageEntity<LoginSessionVO> page = service.listSessions(7, "session-2", 3, 5);

        assertEquals(0L, page.getTotalSize());
        ArgumentCaptor<Date> cutoffCaptor = ArgumentCaptor.forClass(Date.class);
        verify(loginSessionMapper).listByAccountId(eq(7), cutoffCaptor.capture(), eq(5), eq(10L));
        Date cutoff = cutoffCaptor.getValue();
        assertTrue(cutoff.after(earliestAccepted));
        assertTrue(cutoff.before(latestAccepted));
    }

    // 测试撤销归属非当前会话不带保存 Token
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

    // 测试密码修改后撤销同一账号的全部有效会话
    @Test
    void shouldRevokeAllActiveSessionsForAccount() {
        UserLoginSessionService service = service();
        AccountLoginSession first = session("session-1", "jwt-1");
        AccountLoginSession second = session("session-2", "jwt-2");
        when(loginSessionMapper.findActiveByAccountId(eq(7), any(Date.class)))
                .thenReturn(List.of(first, second));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service.revokeAllSessions(7);

        verify(redisTemplate).delete(CONST.LOGIN_SESSION_ACTIVE + "session-1");
        verify(redisTemplate).delete(CONST.LOGIN_SESSION_ACTIVE + "session-2");
        verify(valueOperations).set(eq(CONST.JWT_BLACK_LIST + "jwt-1"), eq(""),
                longThat(ttl -> ttl > 0 && ttl <= 60_000L), eq(TimeUnit.MILLISECONDS));
        verify(valueOperations).set(eq(CONST.JWT_BLACK_LIST + "jwt-2"), eq(""),
                longThat(ttl -> ttl > 0 && ttl <= 60_000L), eq(TimeUnit.MILLISECONDS));
        verify(loginSessionMapper).markRevoked(eq("session-1"), any(Date.class));
        verify(loginSessionMapper).markRevoked(eq("session-2"), any(Date.class));
    }

    @Test
    void shouldRejectBothOldJwtsAfterAllSessionsAreRevoked() {
        UserLoginSessionService service = service();
        JWTUtils jwtUtils = new JWTUtils();
        ReflectionTestUtils.setField(jwtUtils, "template", redisTemplate);
        ReflectionTestUtils.setField(jwtUtils, "key", "test-secret");
        ReflectionTestUtils.setField(jwtUtils, "expire", 7);
        UserDetails user = User.withUsername("tester").password("N/A").roles("USER").build();
        JWTUtils.LoginJwt firstJwt = jwtUtils.createLoginJwt(user, 7, "tester", "session-1");
        JWTUtils.LoginJwt secondJwt = jwtUtils.createLoginJwt(user, 7, "tester", "session-2");
        Map<String, Boolean> redisKeys = new HashMap<>();
        when(redisTemplate.hasKey(anyString())).thenAnswer(invocation ->
                redisKeys.getOrDefault(invocation.getArgument(0), false));
        redisKeys.put(CONST.LOGIN_SESSION_ACTIVE + "session-1", true);
        redisKeys.put(CONST.LOGIN_SESSION_ACTIVE + "session-2", true);
        doAnswer(invocation -> redisKeys.remove(invocation.getArgument(0)) != null)
                .when(redisTemplate).delete(anyString());
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doAnswer(invocation -> {
            redisKeys.put(invocation.getArgument(0), true);
            return null;
        }).when(valueOperations).set(anyString(), eq(""), anyLong(), eq(TimeUnit.MILLISECONDS));
        when(loginSessionMapper.findActiveByAccountId(eq(7), any(Date.class)))
                .thenReturn(List.of(
                        session("session-1", firstJwt.jwtId(), firstJwt.expireTime()),
                        session("session-2", secondJwt.jwtId(), secondJwt.expireTime())));

        assertNotNull(jwtUtils.resolveJwt("Bearer " + firstJwt.token()));
        assertNotNull(jwtUtils.resolveJwt("Bearer " + secondJwt.token()));

        service.revokeAllSessions(7);

        assertNull(jwtUtils.resolveJwt("Bearer " + firstJwt.token()));
        assertNull(jwtUtils.resolveJwt("Bearer " + secondJwt.token()));
    }

    private static AccountLoginSession session(String sessionId, String jwtId) {
        return session(sessionId, jwtId, new Date(System.currentTimeMillis() + 60_000));
    }

    private static AccountLoginSession session(String sessionId, String jwtId, Date expireTime) {
        AccountLoginSession session = new AccountLoginSession();
        session.setAccountId(7);
        session.setSessionId(sessionId);
        session.setJwtId(jwtId);
        session.setExpireTime(expireTime);
        return session;
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
