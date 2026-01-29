package com.ayor.entity.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Spotify Currently Playing API响应DTO
 * 用于接收Spotify当前播放状态接口的响应数据
 */
@Data
public class SpotifyCurrentlyPlayingResponse {

    /**
     * 是否正在播放
     */
    @JsonProperty("is_playing")
    private Boolean isPlaying;

    /**
     * 当前播放进度(毫秒)
     */
    @JsonProperty("progress_ms")
    private Long progressMs;

    /**
     * 歌曲信息
     */
    @JsonProperty("item")
    private Item item;

    @Data
    public static class Item {
        /**
         * 歌曲名称
         */
        @JsonProperty("name")
        private String name;

        /**
         * 歌曲时长(毫秒)
         */
        @JsonProperty("duration_ms")
        private Long durationMs;

        /**
         * 艺术家列表
         */
        @JsonProperty("artists")
        private List<Artist> artists;

        /**
         * 专辑信息
         */
        @JsonProperty("album")
        private Album album;

        /**
         * Spotify外部链接
         */
        @JsonProperty("external_urls")
        private ExternalUrls externalUrls;
    }

    @Data
    public static class Artist {
        /**
         * 艺术家名称
         */
        @JsonProperty("name")
        private String name;
    }

    @Data
    public static class Album {
        /**
         * 专辑名称
         */
        @JsonProperty("name")
        private String name;

        /**
         * 专辑封面图片列表
         */
        @JsonProperty("images")
        private List<Image> images;
    }

    @Data
    public static class Image {
        /**
         * 图片URL
         */
        @JsonProperty("url")
        private String url;

        /**
         * 图片宽度
         */
        @JsonProperty("width")
        private Integer width;

        /**
         * 图片高度
         */
        @JsonProperty("height")
        private Integer height;
    }

    @Data
    public static class ExternalUrls {
        /**
         * Spotify链接
         */
        @JsonProperty("spotify")
        private String spotify;
    }
}
