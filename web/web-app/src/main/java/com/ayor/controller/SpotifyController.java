package com.ayor.controller;

import com.ayor.entity.app.vo.CurrentlyPlayingVO;
import com.ayor.entity.app.vo.SpotifyAuthVO;
import com.ayor.result.Result;
import com.ayor.service.SpotifyService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Spotify API控制器
 * 处理Spotify OAuth授权和播放状态查询
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/spotify")
public class SpotifyController {

    private final SpotifyService spotifyService;
    private final SecurityUtils securityUtils;

    /**
     * 获取Spotify授权URL
     * GET /api/spotify/auth
     *
     * @return 授权信息VO,包含authUrl和绑定状态
     */
    @GetMapping("/auth")
    public Result<SpotifyAuthVO> getAuthUrl() {
        Integer userId = securityUtils.getSecurityUserId();
        SpotifyAuthVO authVO = spotifyService.getAuthorizationUrl(userId);
        return Result.ok(authVO);
    }

    /**
     * 处理Spotify OAuth回调
     * GET /api/spotify/callback?code=xxx&state=userId
     *
     * @param code  授权码
     * @param state 用户ID(CSRF防护)
     * @return 重定向到前端个人资料页
     */
    @GetMapping("/callback")
    public String handleCallback(@RequestParam("code") String code,
                                  @RequestParam("state") String state) {
        try {
            Integer accountId = Integer.parseInt(state);
            Integer currentUserId = securityUtils.getSecurityUserId();

            // 验证state参数防止CSRF
            if (!accountId.equals(currentUserId)) {
                return "redirect:http://localhost:9966/profile?spotify_error=invalid_state";
            }

            String result = spotifyService.handleCallback(accountId, code);

            if (result == null) {
                // 成功,重定向到个人资料页
                return "redirect:http://localhost:9966/profile?spotify_success=true";
            } else {
                // 失败,重定向并携带错误信息
                return "redirect:http://localhost:9966/profile?spotify_error=" + result;
            }

        } catch (Exception e) {
            return "redirect:http://localhost:9966/profile?spotify_error=" + e.getMessage();
        }
    }

    /**
     * 解除Spotify绑定
     * DELETE /api/spotify/disconnect
     *
     * @return 操作结果
     */
    @DeleteMapping("/disconnect")
    public Result<Void> disconnect() {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> spotifyService.disconnect(userId));
    }

    /**
     * 获取当前用户的Spotify播放状态
     * GET /api/spotify/currently-playing
     *
     * @return 播放状态VO,未播放或未绑定返回null
     */
    @GetMapping("/currently-playing")
    public Result<CurrentlyPlayingVO> getCurrentlyPlaying() {
        Integer userId = securityUtils.getSecurityUserId();
        CurrentlyPlayingVO vo = spotifyService.getCurrentlyPlaying(userId);
        return Result.dataMessageHandler(() -> vo, "获取播放状态失败");
    }

    /**
     * 获取指定用户的Spotify播放状态(公开端点)
     * GET /api/spotify/currently-playing/by_user_id?user_id=123
     *
     * @param userId 目标用户ID
     * @return 播放状态VO,未播放或未绑定返回null
     */
    @GetMapping("/currently-playing/by_user_id")
    public Result<CurrentlyPlayingVO> getCurrentlyPlayingByUserId(@RequestParam("user_id") Integer userId) {
        CurrentlyPlayingVO vo = spotifyService.getCurrentlyPlaying(userId);
        return Result.dataMessageHandler(() -> vo, "获取播放状态失败");
    }

    /**
     * 手动刷新当前用户的播放状态(调试用)
     * POST /api/spotify/refresh
     *
     * @return 操作结果
     */
    @PostMapping("/refresh")
    public Result<Void> manualRefresh() {
        Integer userId = securityUtils.getSecurityUserId();
        spotifyService.refreshCurrentlyPlaying(userId);
        return Result.ok();
    }
}
