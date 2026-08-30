package com.ayor.service.impl;

import com.ayor.config.RegistrationVerificationProperties;
import com.ayor.service.RegistrationVerificationGate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/**
 * 通过单次 Lua 执行实现幂等优先和多维原子限流。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisRegistrationVerificationGate implements RegistrationVerificationGate {

    private static final String PREFIX = "registration:verify:";
    private static final String GLOBAL_KEY = PREFIX + "global";
    private static final String GLOBAL_ALERT_KEY = PREFIX + "global:alert";

    private static final DefaultRedisScript<String> ACQUIRE_SCRIPT = new DefaultRedisScript<>("""
            local existing = redis.call('GET', KEYS[1])
            if existing then
                return 'REUSED|' .. existing .. '|0|0|0'
            end

            local emailCount = tonumber(redis.call('GET', KEYS[2]) or '0')
            local ipCount = tonumber(redis.call('GET', KEYS[3]) or '0')
            local globalCount = tonumber(redis.call('GET', KEYS[4]) or '0')
            local retryMillis = nil

            local function recordRetry(key, fallbackMillis)
                local ttl = redis.call('PTTL', key)
                if ttl < 0 then ttl = tonumber(fallbackMillis) end
                if not retryMillis or ttl > retryMillis then retryMillis = ttl end
            end

            if emailCount >= tonumber(ARGV[3]) then recordRetry(KEYS[2], ARGV[4]) end
            if ipCount >= tonumber(ARGV[5]) then recordRetry(KEYS[3], ARGV[6]) end
            if globalCount >= tonumber(ARGV[7]) then recordRetry(KEYS[4], ARGV[8]) end
            if retryMillis then
                return 'LIMITED||' .. tostring(math.max(1, math.ceil(retryMillis / 1000))) .. '|' .. tostring(globalCount) .. '|0'
            end

            emailCount = redis.call('INCR', KEYS[2])
            if emailCount == 1 then redis.call('PEXPIRE', KEYS[2], ARGV[4]) end
            ipCount = redis.call('INCR', KEYS[3])
            if ipCount == 1 then redis.call('PEXPIRE', KEYS[3], ARGV[6]) end
            globalCount = redis.call('INCR', KEYS[4])
            if globalCount == 1 then redis.call('PEXPIRE', KEYS[4], ARGV[8]) end
            redis.call('SET', KEYS[1], ARGV[1], 'PX', ARGV[2])

            local alert = 0
            if globalCount >= tonumber(ARGV[9]) then
                local alertTtl = redis.call('PTTL', KEYS[4])
                if alertTtl < 0 then alertTtl = tonumber(ARGV[8]) end
                local created = redis.call('SET', KEYS[5], '1', 'NX', 'PX', math.max(1, alertTtl))
                if created then alert = 1 end
            end
            return 'GRANTED|' .. ARGV[1] .. '|0|' .. tostring(globalCount) .. '|' .. tostring(alert)
            """, String.class);

    private static final DefaultRedisScript<Long> COMPLETE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final RegistrationVerificationProperties properties;

    @Override
    public Acquisition acquire(String email, String remoteAddress, String candidateJwtId) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        List<String> keys = List.of(
                idempotencyKey(normalizedEmail),
                PREFIX + "email:" + digest(normalizedEmail),
                PREFIX + "ip:" + digest(remoteAddress == null ? "" : remoteAddress),
                GLOBAL_KEY,
                GLOBAL_ALERT_KEY
        );
        int alertThreshold = (int) Math.ceil(properties.getGlobalLimit() * properties.getGlobalAlertPercent() / 100.0);
        String rawResult = redisTemplate.execute(
                ACQUIRE_SCRIPT,
                keys,
                candidateJwtId,
                millis(properties.getIdempotencySeconds()),
                Integer.toString(properties.getEmailLimit()),
                millis(properties.getEmailWindowSeconds()),
                Integer.toString(properties.getIpLimit()),
                millis(properties.getIpWindowSeconds()),
                Integer.toString(properties.getGlobalLimit()),
                millis(properties.getGlobalWindowSeconds()),
                Integer.toString(alertThreshold)
        );
        ParsedResult parsed = parse(rawResult);
        if (parsed.alert()) {
            log.warn("注册验证邮件全局配额接近上限 count={} limit={} windowSeconds={}",
                    parsed.globalCount(), properties.getGlobalLimit(), properties.getGlobalWindowSeconds());
        }
        return switch (parsed.status()) {
            case GRANTED -> Acquisition.granted(parsed.jwtId());
            case REUSED -> Acquisition.reused(parsed.jwtId());
            case LIMITED -> Acquisition.limited(parsed.retryAfterSeconds());
        };
    }

    @Override
    public void complete(String email, String jwtId) {
        redisTemplate.execute(COMPLETE_SCRIPT, List.of(idempotencyKey(email)), jwtId);
    }

    private ParsedResult parse(String rawResult) {
        if (rawResult == null) {
            throw new IllegalStateException("注册验证邮件门禁未返回结果");
        }
        String[] parts = rawResult.split("\\|", -1);
        if (parts.length != 5) {
            throw new IllegalStateException("注册验证邮件门禁返回格式无效");
        }
        try {
            Status status = Status.valueOf(parts[0]);
            String jwtId = parts[1].isBlank() ? null : parts[1];
            long retryAfterSeconds = Long.parseLong(parts[2]);
            long globalCount = Long.parseLong(parts[3]);
            if (!"0".equals(parts[4]) && !"1".equals(parts[4])) {
                throw new IllegalStateException("注册验证邮件门禁告警标志无效");
            }
            boolean alert = "1".equals(parts[4]);
            if ((status == Status.GRANTED || status == Status.REUSED) && jwtId == null) {
                throw new IllegalStateException("注册验证邮件门禁缺少 JWT ID");
            }
            if (status == Status.LIMITED && (jwtId != null || retryAfterSeconds < 1)) {
                throw new IllegalStateException("注册验证邮件门禁限流结果无效");
            }
            if (globalCount < 0) {
                throw new IllegalStateException("注册验证邮件门禁全局计数无效");
            }
            return new ParsedResult(status, jwtId, retryAfterSeconds, globalCount, alert);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("注册验证邮件门禁返回内容无效", exception);
        }
    }

    private String idempotencyKey(String email) {
        return PREFIX + "idem:" + digest(email.trim().toLowerCase(Locale.ROOT));
    }

    private String digest(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("运行环境不支持 SHA-256", exception);
        }
    }

    private String millis(long seconds) {
        return Long.toString(Math.multiplyExact(seconds, 1000));
    }

    private record ParsedResult(Status status, String jwtId, long retryAfterSeconds, long globalCount, boolean alert) {
    }
}
