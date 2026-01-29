package com.ayor.service;

import com.ayor.entity.app.vo.CurrentlyPlayingVO;
import com.ayor.entity.app.vo.SpotifyAuthVO;
import com.ayor.entity.pojo.SpotifyToken;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * Spotify服务接口
 * 处理Spotify OAuth认证和播放状态查询
 */
public interface SpotifyService extends IService<SpotifyToken> {

    /**
     * 生成Spotify授权URL
     *
     * @param accountId 用户ID
     * @return 授权信息VO,包含authUrl和绑定状态
     */
    SpotifyAuthVO getAuthorizationUrl(Integer accountId);

    /**
     * 处理OAuth回调,用授权码换取access token
     *
     * @param accountId 用户ID
     * @param code      Spotify返回的授权码
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String handleCallback(Integer accountId, String code);

    /**
     * 解除Spotify绑定
     *
     * @param accountId 用户ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String disconnect(Integer accountId);

    /**
     * 获取用户当前播放状态
     * 优先从Redis缓存读取,未命中则调用Spotify API并缓存
     *
     * @param accountId 用户ID
     * @return 播放状态VO,未播放或未绑定返回null
     */
    CurrentlyPlayingVO getCurrentlyPlaying(Integer accountId);

    /**
     * 刷新指定用户的当前播放状态到Redis缓存
     * 由定时任务调用
     *
     * @param accountId 用户ID
     */
    void refreshCurrentlyPlaying(Integer accountId);

    /**
     * 刷新所有已绑定用户的播放状态
     * 由定时任务调用
     */
    void refreshAllUsersPlaying();

    /**
     * 刷新access token
     * Token过期时自动调用
     *
     * @param spotifyToken Spotify Token实体
     * @return 刷新后的新token实体
     */
    SpotifyToken refreshAccessToken(SpotifyToken spotifyToken);
}
