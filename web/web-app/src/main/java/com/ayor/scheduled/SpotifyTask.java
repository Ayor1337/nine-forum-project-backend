package com.ayor.scheduled;

import com.ayor.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Spotify定时任务
 * 定期刷新所有已绑定用户的当前播放状态到Redis缓存
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class SpotifyTask {

    private final SpotifyService spotifyService;

    /**
     * 定时刷新所有用户的Spotify播放状态
     * 每30秒执行一次
     */
    @Scheduled(fixedRate = 30000)
    public void refreshAllUsersCurrentlyPlaying() {
        log.debug("开始定时刷新Spotify播放状态");
        try {
            spotifyService.refreshAllUsersPlaying();
        } catch (Exception e) {
            log.error("定时刷新Spotify播放状态失败: {}", e.getMessage(), e);
        }
    }
}
