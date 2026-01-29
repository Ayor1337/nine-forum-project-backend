package com.ayor.entity.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Spotify Token API响应DTO
 * 用于接收Spotify OAuth Token接口的响应数据
 */
@Data
public class SpotifyTokenResponse {

    /**
     * 访问令牌
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * 令牌类型
     */
    @JsonProperty("token_type")
    private String tokenType;

    /**
     * 授权范围
     */
    @JsonProperty("scope")
    private String scope;

    /**
     * 过期时间(秒)
     */
    @JsonProperty("expires_in")
    private Integer expiresIn;

    /**
     * 刷新令牌
     */
    @JsonProperty("refresh_token")
    private String refreshToken;
}
