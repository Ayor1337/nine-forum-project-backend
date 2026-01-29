package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Spotify当前播放状态VO
 * 用于返回给前端展示用户正在收听的音乐
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentlyPlayingVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 201L;

    /**
     * 是否正在播放
     */
    private Boolean isPlaying;

    /**
     * 歌曲名称
     */
    private String trackName;

    /**
     * 艺术家名称
     */
    private String artistName;

    /**
     * 专辑名称
     */
    private String albumName;

    /**
     * 专辑封面图片URL
     */
    private String albumImageUrl;

    /**
     * Spotify歌曲链接
     */
    private String spotifyUrl;

    /**
     * 当前播放进度(毫秒)
     */
    private Long progressMs;

    /**
     * 歌曲总时长(毫秒)
     */
    private Long durationMs;

    /**
     * 缓存时间
     */
    private LocalDateTime cachedAt;
}
