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

    public boolean deleteToken(String uuid, Date time) {
        if (this.isInvalidToken(uuid)) {
            return false;
        }
        Date now = new Date();
        long expire = Math.max(time.getTime() - now.getTime(), 0);
        template.opsForValue().set(CONST.JWT_BLACK_LIST + uuid, "", expire, TimeUnit.MILLISECONDS);
        return true;
    }

    private boolean isInvalidToken(String uuid) {
        return Boolean.TRUE.equals(template.hasKey(CONST.JWT_BLACK_LIST + uuid));
    }

    private boolean isInvalidEmailToken(String uuid) {
        return Boolean.FALSE.equals(template.hasKey(CONST.JWT_EMAIL_VERIFY + uuid));
    }

    public String createJwt(UserDetails userDetails, int id, String username) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        Date expire = this.expiredTime();
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("id", id)
                .withClaim("name", username)
                .withClaim("authorities", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(expire)
                .withIssuedAt(new Date())
                .sign(algorithm);
    }

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
            Date expiresAt = verify.getExpiresAt();
            return new Date().after(expiresAt) ? null : verify;
        } catch (JWTVerificationException e) {
            return null;
        }

    }

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

    public String convertToken(String token) {
        if (token == null || !token.startsWith("Bearer "))
            return null;
        return token.substring("Bearer ".length());
    }

    public Date expiredTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expire * 24);
        return calendar.getTime();
    }

    public Date expiredHourTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 3);
        return calendar.getTime();
    }

    public UserDetails toUser(DecodedJWT jwt) {
        if (jwt == null)
            return null;
        Map<String, Claim> claims = jwt.getClaims();
        return User
                .withUsername(claims.get("id").toString())
                .password("*****")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

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


    public Integer toID(DecodedJWT decodedJWT) {
        if (decodedJWT == null)
            return null;
        Map<String, Claim> claims = decodedJWT.getClaims();
        return claims.get("id").asInt();

    }
}
