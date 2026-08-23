package com.ayor.service.impl;

import com.ayor.config.RegistrationVerificationProperties;
import com.ayor.service.RegistrationVerificationGate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(OutputCaptureExtension.class)
class RedisRegistrationVerificationGateTest {

    private StringRedisTemplate redisTemplate;
    private RegistrationVerificationProperties properties;
    private RedisRegistrationVerificationGate gate;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        properties = new RegistrationVerificationProperties();
        gate = new RedisRegistrationVerificationGate(redisTemplate, properties);
    }

    @Test
    void acquireUsesOneScriptWithHashedKeysAndConfiguredBoundaries() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenReturn("GRANTED|candidate-id|0|1|0");

        RegistrationVerificationGate.Acquisition result = gate.acquire(
                " User@Example.COM ",
                "203.0.113.8",
                "candidate-id"
        );

        assertThat(result).isEqualTo(RegistrationVerificationGate.Acquisition.granted("candidate-id"));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(redisTemplate).execute(any(RedisScript.class), keysCaptor.capture(), argsCaptor.capture());
        assertThat(keysCaptor.getValue()).hasSize(5);
        assertThat(keysCaptor.getValue()).noneMatch(key -> key.contains("user@example.com") || key.contains("203.0.113.8"));
        assertThat(keysCaptor.getValue().get(0)).startsWith("registration:verify:idem:");
        assertThat(argsCaptor.getValue()).containsExactly(
                "candidate-id",
                "60000",
                "3",
                "900000",
                "10",
                "900000",
                "100",
                "600000",
                "80"
        );
    }

    @Test
    void acquireReturnsExistingJwtForIdempotentRequest() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenReturn("REUSED|existing-id|0|0|0");

        assertThat(gate.acquire("user@example.com", "127.0.0.1", "candidate-id"))
                .isEqualTo(RegistrationVerificationGate.Acquisition.reused("existing-id"));
    }

    @Test
    void acquireMapsQuotaBoundaryToLimitedResult() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenReturn("LIMITED||37|100|0");

        assertThat(gate.acquire("user@example.com", "127.0.0.1", "candidate-id"))
                .isEqualTo(RegistrationVerificationGate.Acquisition.limited(37));
    }

    @Test
    void luaChecksEmailIpAndGlobalLimitsBeforeMutatingCounters() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenReturn("GRANTED|candidate-id|0|1|0");

        gate.acquire("user@example.com", "127.0.0.1", "candidate-id");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<RedisScript<String>> scriptCaptor = ArgumentCaptor.forClass(RedisScript.class);
        verify(redisTemplate).execute(scriptCaptor.capture(), anyList(), any(Object[].class));
        String script = scriptCaptor.getValue().getScriptAsString();
        int emailCheck = script.indexOf("emailCount >=");
        int ipCheck = script.indexOf("ipCount >=");
        int globalCheck = script.indexOf("globalCount >= tonumber(ARGV[7])");
        int firstIncrement = script.indexOf("redis.call('INCR'");
        assertThat(emailCheck).isGreaterThanOrEqualTo(0).isLessThan(firstIncrement);
        assertThat(ipCheck).isGreaterThan(emailCheck).isLessThan(firstIncrement);
        assertThat(globalCheck).isGreaterThan(ipCheck).isLessThan(firstIncrement);
        assertThat(script).contains("'NX', 'PX'");
        assertThat(script).contains("ttl > retryMillis");
        assertThat(script).contains("if ttl < 0 then ttl = tonumber(fallbackMillis) end");
        assertThat(script).contains("'PX', math.max(1, alertTtl)");
    }

    @Test
    void malformedLuaResultFailsClosed() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenReturn("GRANTED||0|1|0");

        assertThrows(
                IllegalStateException.class,
                () -> gate.acquire("user@example.com", "127.0.0.1", "candidate-id")
        );
    }

    @Test
    void globalAlertIsLoggedOnlyWhenLuaGrantsAlertOwnership(CapturedOutput output) {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenReturn("GRANTED|first-id|0|80|1", "GRANTED|second-id|0|81|0");

        gate.acquire("first@example.com", "127.0.0.1", "first-id");
        gate.acquire("second@example.com", "127.0.0.2", "second-id");

        assertThat(output.getOut()).containsOnlyOnce("注册验证邮件全局配额接近上限");
        assertThat(output.getOut()).doesNotContain("first@example.com", "127.0.0.1", "first-id");
    }

    @Test
    void completeUsesCompareAndDeleteScriptWithHashedIdempotencyKey() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class))).thenReturn(1L);

        gate.complete("user@example.com", "jwt-id");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(redisTemplate).execute(any(RedisScript.class), keysCaptor.capture(), argsCaptor.capture());
        assertThat(keysCaptor.getValue()).singleElement().asString().startsWith("registration:verify:idem:");
        assertThat(keysCaptor.getValue().get(0)).doesNotContain("user@example.com");
        assertThat(argsCaptor.getValue()).containsExactly("jwt-id");
    }
}
