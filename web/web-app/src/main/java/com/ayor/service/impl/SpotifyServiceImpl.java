package com.ayor.service.impl;

import com.ayor.entity.app.vo.CurrentlyPlayingVO;
import com.ayor.entity.app.vo.SpotifyAuthVO;
import com.ayor.entity.pojo.SpotifyToken;
import com.ayor.entity.spotify.SpotifyCurrentlyPlayingResponse;
import com.ayor.entity.spotify.SpotifyTokenResponse;
import com.ayor.exception.SpotifyException;
import com.ayor.mapper.SpotifyTokenMapper;
import com.ayor.service.SpotifyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Spotify服务实现类
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SpotifyServiceImpl extends ServiceImpl<SpotifyTokenMapper, SpotifyToken> implements SpotifyService {

    private final WebClient webClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final SpotifyTokenMapper spotifyTokenMapper;
    private final ObjectMapper objectMapper;

    @Value("${spring.spotify.client-id}")
    private String clientId;

    @Value("${spring.spotify.client-secret}")
    private String clientSecret;

    @Value("${spring.spotify.redirect-uri}")
    private String redirectUri;

    @Value("${spring.spotify.scopes}")
    private String scopes;

    @Value("${spring.spotify.auth-url}")
    private String authUrl;

    @Value("${spring.spotify.token-url}")
    private String tokenUrl;

    @Value("${spring.spotify.api-base-url}")
    private String apiBaseUrl;

    private static final String REDIS_KEY_PREFIX = "spotify:currently_playing:";
    private static final long CACHE_TTL_SECONDS = 30;

    @Override
    public SpotifyAuthVO getAuthorizationUrl(Integer accountId) {
        SpotifyToken existingToken = spotifyTokenMapper.getByAccountId(accountId);

        String authorizationUrl = authUrl +
                "?client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + redirectUri +
                "&scope=" + scopes +
                "&state=" + accountId; // 使用accountId作为state参数

        SpotifyAuthVO vo = new SpotifyAuthVO();
        vo.setAuthUrl(authorizationUrl);
        vo.setIsConnected(existingToken != null);
        vo.setConnectedAt(existingToken != null ? existingToken.getCreateTime() : null);

        return vo;
    }

    @Override
    public String handleCallback(Integer accountId, String code) {
        try {
            // 用授权码换取token
            SpotifyTokenResponse tokenResponse = exchangeCodeForToken(code);

            if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
                return "获取Spotify令牌失败";
            }

            // 检查是否已存在绑定
            SpotifyToken existingToken = spotifyTokenMapper.getByAccountId(accountId);

            if (existingToken != null) {
                // 更新现有token
                existingToken.setAccessToken(tokenResponse.getAccessToken());
                existingToken.setRefreshToken(tokenResponse.getRefreshToken());
                existingToken.setTokenType(tokenResponse.getTokenType());
                existingToken.setScope(tokenResponse.getScope());
                existingToken.setExpiresIn(tokenResponse.getExpiresIn());
                existingToken.setExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));
                existingToken.setUpdateTime(LocalDateTime.now());
                spotifyTokenMapper.updateById(existingToken);
            } else {
                // 创建新token记录
                SpotifyToken newToken = new SpotifyToken();
                newToken.setAccountId(accountId);
                newToken.setAccessToken(tokenResponse.getAccessToken());
                newToken.setRefreshToken(tokenResponse.getRefreshToken());
                newToken.setTokenType(tokenResponse.getTokenType());
                newToken.setScope(tokenResponse.getScope());
                newToken.setExpiresIn(tokenResponse.getExpiresIn());
                newToken.setExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));
                newToken.setCreateTime(LocalDateTime.now());
                newToken.setUpdateTime(LocalDateTime.now());
                newToken.setIsDeleted(false);
                spotifyTokenMapper.insert(newToken);
            }

            log.info("用户 {} 成功绑定Spotify账号", accountId);
            return null;

        } catch (Exception e) {
            log.error("处理Spotify回调失败: {}", e.getMessage(), e);
            return "处理Spotify回调失败: " + e.getMessage();
        }
    }

    @Override
    public String disconnect(Integer accountId) {
        SpotifyToken token = spotifyTokenMapper.getByAccountId(accountId);
        if (token == null) {
            return "未绑定Spotify账号";
        }

        // 软删除
        token.setIsDeleted(true);
        token.setUpdateTime(LocalDateTime.now());
        spotifyTokenMapper.updateById(token);

        // 清除Redis缓存
        stringRedisTemplate.delete(REDIS_KEY_PREFIX + accountId);

        log.info("用户 {} 解除Spotify绑定", accountId);
        return null;
    }

    @Override
    public CurrentlyPlayingVO getCurrentlyPlaying(Integer accountId) {
        // 1. 尝试从Redis缓存读取
        System.out.println(accountId);
        String cacheKey = REDIS_KEY_PREFIX + accountId;
        String cachedJson = stringRedisTemplate.opsForValue().get(cacheKey);

        if (cachedJson != null && !cachedJson.isEmpty() && !"null".equals(cachedJson)) {
            try {
                return deserializeFromJson(cachedJson);
            } catch (Exception e) {
                log.warn("反序列化缓存失败: {}", e.getMessage());
            }
        }

        // 2. 缓存未命中,查询Spotify API
        return fetchAndCacheCurrentlyPlaying(accountId);
    }

    @Override
    public void refreshCurrentlyPlaying(Integer accountId) {
        try {
            fetchAndCacheCurrentlyPlaying(accountId);
        } catch (Exception e) {
            log.error("刷新用户 {} 的Spotify播放状态失败: {}", accountId, e.getMessage());
        }
    }

    @Override
    public void refreshAllUsersPlaying() {
        List<Integer> accountIds = spotifyTokenMapper.getAllConnectedAccountIds();
        log.info("开始刷新 {} 个用户的Spotify播放状态", accountIds.size());

        for (Integer accountId : accountIds) {
            refreshCurrentlyPlaying(accountId);
        }
    }

    @Override
    public SpotifyToken refreshAccessToken(SpotifyToken spotifyToken) {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "refresh_token");
            formData.add("refresh_token", spotifyToken.getRefreshToken());

            String basicAuth = Base64.getEncoder().encodeToString(
                    (clientId + ":" + clientSecret).getBytes()
            );

            SpotifyTokenResponse response = webClient.post()
                    .uri(tokenUrl)
                    .header("Authorization", "Basic " + basicAuth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(SpotifyTokenResponse.class)
                    .block();

            if (response == null || response.getAccessToken() == null) {
                throw new SpotifyException("刷新Token失败,响应为空");
            }

            // 更新数据库
            spotifyToken.setAccessToken(response.getAccessToken());
            spotifyToken.setExpiresIn(response.getExpiresIn());
            spotifyToken.setExpiresAt(LocalDateTime.now().plusSeconds(response.getExpiresIn()));
            spotifyToken.setUpdateTime(LocalDateTime.now());

            // 如果返回了新的refresh_token,也更新
            if (response.getRefreshToken() != null) {
                spotifyToken.setRefreshToken(response.getRefreshToken());
            }

            spotifyTokenMapper.updateById(spotifyToken);

            log.info("用户 {} 的Spotify Token刷新成功", spotifyToken.getAccountId());
            return spotifyToken;

        } catch (Exception e) {
            log.error("刷新Token失败: {}", e.getMessage(), e);
            throw new SpotifyException("刷新Token失败: " + e.getMessage());
        }
    }

    /**
     * 用授权码换取access token
     */
    private SpotifyTokenResponse exchangeCodeForToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("redirect_uri", redirectUri);

        String basicAuth = Base64.getEncoder().encodeToString(
                (clientId + ":" + clientSecret).getBytes()
        );

        return webClient.post()
                .uri(tokenUrl)
                .header("Authorization", "Basic " + basicAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(SpotifyTokenResponse.class)
                .block();
    }

    /**
     * 查询并缓存当前播放状态
     */
    private CurrentlyPlayingVO fetchAndCacheCurrentlyPlaying(Integer accountId) {
        SpotifyToken token = spotifyTokenMapper.getByAccountId(accountId);
        if (token == null) {
            return null;
        }

        // 检查token是否过期
        if (token.isExpired()) {
            token = refreshAccessToken(token);
        }

        try {
            SpotifyCurrentlyPlayingResponse response = webClient.get()
                    .uri(apiBaseUrl + "/me/player/currently-playing")
                    .header("Authorization", "Bearer " + token.getAccessToken())
                    .retrieve()
                    .bodyToMono(SpotifyCurrentlyPlayingResponse.class)
                    .block();

            if (response == null || response.getItem() == null) {
                // 用户当前未播放
                stringRedisTemplate.opsForValue().set(
                        REDIS_KEY_PREFIX + accountId,
                        "null",
                        CACHE_TTL_SECONDS,
                        TimeUnit.SECONDS
                );
                return null;
            }

            // 转换为VO
            CurrentlyPlayingVO vo = convertToVO(response);

            // 缓存到Redis
            String json = serializeToJson(vo);
            stringRedisTemplate.opsForValue().set(
                    REDIS_KEY_PREFIX + accountId,
                    json,
                    CACHE_TTL_SECONDS,
                    TimeUnit.SECONDS
            );

            return vo;

        } catch (WebClientResponseException.NotFound e) {
            // 404表示用户当前未播放
            log.debug("用户 {} 当前未播放音乐", accountId);
            stringRedisTemplate.opsForValue().set(
                    REDIS_KEY_PREFIX + accountId,
                    "null",
                    CACHE_TTL_SECONDS,
                    TimeUnit.SECONDS
            );
            return null;
        } catch (Exception e) {
            log.error("获取Spotify播放状态失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 转换Spotify API响应为VO
     */
    private CurrentlyPlayingVO convertToVO(SpotifyCurrentlyPlayingResponse response) {
        CurrentlyPlayingVO vo = new CurrentlyPlayingVO();
        vo.setIsPlaying(response.getIsPlaying());
        vo.setProgressMs(response.getProgressMs());

        if (response.getItem() != null) {
            vo.setTrackName(response.getItem().getName());
            vo.setDurationMs(response.getItem().getDurationMs());

            // 艺术家名称(多个艺术家用逗号分隔)
            if (response.getItem().getArtists() != null && !response.getItem().getArtists().isEmpty()) {
                String artistNames = response.getItem().getArtists().stream()
                        .map(SpotifyCurrentlyPlayingResponse.Artist::getName)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                vo.setArtistName(artistNames);
            }

            // 专辑信息
            if (response.getItem().getAlbum() != null) {
                vo.setAlbumName(response.getItem().getAlbum().getName());

                // 专辑封面(取第一张图片)
                if (response.getItem().getAlbum().getImages() != null &&
                        !response.getItem().getAlbum().getImages().isEmpty()) {
                    vo.setAlbumImageUrl(response.getItem().getAlbum().getImages().get(0).getUrl());
                }
            }

            // Spotify链接
            if (response.getItem().getExternalUrls() != null) {
                vo.setSpotifyUrl(response.getItem().getExternalUrls().getSpotify());
            }
        }

        vo.setCachedAt(LocalDateTime.now());
        return vo;
    }

    /**
     * 序列化VO为JSON字符串
     */
    private String serializeToJson(CurrentlyPlayingVO vo) {
        try {
            return objectMapper.writeValueAsString(vo);
        } catch (Exception e) {
            log.error("序列化CurrentlyPlayingVO失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 反序列化JSON字符串为VO
     */
    private CurrentlyPlayingVO deserializeFromJson(String json) {
        try {
            return objectMapper.readValue(json, CurrentlyPlayingVO.class);
        } catch (Exception e) {
            log.error("反序列化CurrentlyPlayingVO失败: {}", e.getMessage(), e);
            return null;
        }
    }
}
