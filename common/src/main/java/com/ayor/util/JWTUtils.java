package com.ayor.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class JWTUtils {

    @Resource
    StringRedisTemplate template;

    @Value("${spring.security.jwt.secret-key}")
    String key;

    @Value("${spring.security.jwt.expire}")
    int expire;

    public record LoginJwt(String token, String jwtId, String sessionId, Date expireTime) {
    }

    /**
     * 将指定 JWT 加入黑名单，使其失效。
     *
     * @param token 原始令牌
     * @return 处理成功返回 true，否则返回 false
     */
    public boolean invalidateJWT(String token) {
        String convertedToken = convertToken(token);
        if (convertedToken == null)
            return false;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT decodedJWT = jwtVerifier.verify(convertedToken);
            String id = decodedJWT.getId();
            return deleteToken(id, decodedJWT.getExpiresAt());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * 将指定令牌标记为黑名单，并设置剩余过期时间。
     *
     * @param uuid 令牌 ID
     * @param time 令牌过期时间
     * @return 标记成功返回 true，重复标记返回 false
     */
    public boolean deleteToken(String uuid, Date time) {
        if (this.isInvalidToken(uuid)) {
            return false;
        }
        Date now = new Date();
        long expire = Math.max(time.getTime() - now.getTime(), 0);
        template.opsForValue().set(CONST.JWT_BLACK_LIST + uuid, "", expire, TimeUnit.MILLISECONDS);
        return true;
    }

    /**
     * 判断普通 JWT 是否已失效。
     *
     * @param uuid 令牌 ID
     * @return 已失效返回 true
     */
    private boolean isInvalidToken(String uuid) {
        return Boolean.TRUE.equals(template.hasKey(CONST.JWT_BLACK_LIST + uuid));
    }

    /**
     * 判断邮箱验证 JWT 是否已失效。
     *
     * @param uuid 令牌 ID
     * @return 已失效返回 true
     */
    private boolean isInvalidEmailToken(String uuid) {
        return Boolean.FALSE.equals(template.hasKey(CONST.JWT_EMAIL_VERIFY + uuid));
    }

    /**
     * 创建登录态 JWT。
     *
     * @param userDetails 用户信息
     * @param id 用户 ID
     * @param username 用户名
     * @return JWT 字符串
     */
    public String createJwt(UserDetails userDetails, int id, String username) {
        return createLoginJwt(userDetails, id, username, null).token();
    }

    /**
     * 创建带会话 ID 的登录态 JWT，并返回 token 元数据。
     */
    public LoginJwt createLoginJwt(UserDetails userDetails, int id, String username, String sessionId) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        Date expireTime = this.expiredTime();
        String jwtId = UUID.randomUUID().toString();
        com.auth0.jwt.JWTCreator.Builder builder = JWT.create()
                .withJWTId(jwtId)
                .withClaim("id", id)
                .withClaim("name", username)
                .withClaim("authorities", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(expireTime)
                .withIssuedAt(new Date());
        if (sessionId != null) {
            builder.withClaim("sid", sessionId);
        }
        return new LoginJwt(builder.sign(algorithm), jwtId, sessionId, expireTime);
    }

    /**
     * 创建邮箱验证 JWT。
     *
     * @param email 邮箱地址
     * @return JWT 字符串
     */
    public String createJwt(String email) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        Date expire = this.expiredHourTime();
        String uuid = UUID.randomUUID().toString();
        String token = JWT.create()
                .withJWTId(uuid)
                .withClaim("email", email)
                .withExpiresAt(expire)
                .withIssuedAt(new Date())
                .sign(algorithm);
        template.opsForValue().set(CONST.JWT_EMAIL_VERIFY + uuid, "", expire.getTime(), TimeUnit.MILLISECONDS);
        return token;
    }



    /**
     * 校验并解析普通 JWT。
     *
     * @param token 原始令牌
     * @return 解析成功返回 JWT 对象，否则返回 null
     */
    public DecodedJWT resolveJwt(String token) {
        String convertedToken = convertToken(token);
        if (convertedToken == null)
            return null;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT verify = jwtVerifier.verify(convertedToken);
            if (this.isInvalidToken(verify.getId())) {
                return null;
            }
            String sessionId = verify.getClaim("sid").asString();
            if (sessionId != null && Boolean.FALSE.equals(template.hasKey(CONST.LOGIN_SESSION_ACTIVE + sessionId))) {
                return null;
            }
            Date expiresAt = verify.getExpiresAt();
            return new Date().after(expiresAt) ? null : verify;
        } catch (JWTVerificationException e) {
            return null;
        }

    }

    /**
     * 校验并解析邮箱验证 JWT。
     *
     * @param token JWT 字符串
     * @return 解析成功返回 JWT 对象，否则返回 null
     */
    public DecodedJWT resolveEmailJwt(String token) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT verify = jwtVerifier.verify(token);
            if (this.isInvalidEmailToken(verify.getId())) {
                return null;
            }
            Date expiresAt = verify.getExpiresAt();
            return new Date().after(expiresAt) ? null : verify;
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    /**
     * 去除 Bearer 前缀，提取原始令牌。
     *
     * @param token 请求头中的令牌
     * @return 原始令牌，格式不合法时返回 null
     */
    public String convertToken(String token) {
        if (token == null || !token.startsWith("Bearer "))
            return null;
        return token.substring("Bearer ".length());
    }

    /**
     * 计算普通 JWT 的过期时间。
     *
     * @return 过期时间
     */
    public Date expiredTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expire * 24);
        return calendar.getTime();
    }

    /**
     * 计算邮箱验证 JWT 的过期时间。
     *
     * @return 过期时间
     */
    public Date expiredHourTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 3);
        return calendar.getTime();
    }

    /**
     * 将 JWT 解析为 Spring Security 用户对象。
     *
     * @param jwt 已解析的 JWT
     * @return 用户对象，入参为空时返回 null
     */
    public UserDetails toUser(DecodedJWT jwt) {
        if (jwt == null)
            return null;
        Map<String, Claim> claims = jwt.getClaims();
        return User
                .withUsername(String.valueOf(claims.get("id").asInt()))
                .password("*****")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

    /**
     * 从当前请求中提取用户名。
     *
     * @param request HTTP 请求
     * @return 用户名，缺失时返回 null
     */
    public String toUsername(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null)
            return null;

        DecodedJWT decodedJWT = resolveJwt(token);
        return Optional
                .ofNullable(decodedJWT.getClaim("name"))
                .map(Claim::asString)
                .orElse(null);
    }


    /**
     * 从 JWT 中提取用户 ID。
     *
     * @param decodedJWT 已解析的 JWT
     * @return 用户 ID，入参为空时返回 null
     */
    public Integer toId(DecodedJWT decodedJWT) {
        if (decodedJWT == null)
            return null;
        Map<String, Claim> claims = decodedJWT.getClaims();
        return claims.get("id").asInt();

    }
}
