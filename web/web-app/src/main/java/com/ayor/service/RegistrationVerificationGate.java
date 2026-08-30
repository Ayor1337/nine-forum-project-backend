package com.ayor.service;

/**
 * 注册验证邮件发送前的 Redis 门禁。
 */
public interface RegistrationVerificationGate {

    Acquisition acquire(String email, String remoteAddress, String candidateJwtId);

    void complete(String email, String jwtId);

    enum Status {
        GRANTED,
        REUSED,
        LIMITED
    }

    record Acquisition(Status status, String jwtId, long retryAfterSeconds) {

        public static Acquisition granted(String jwtId) {
            return new Acquisition(Status.GRANTED, jwtId, 0);
        }

        public static Acquisition reused(String jwtId) {
            return new Acquisition(Status.REUSED, jwtId, 0);
        }

        public static Acquisition limited(long retryAfterSeconds) {
            return new Acquisition(Status.LIMITED, null, Math.max(retryAfterSeconds, 1));
        }
    }
}
