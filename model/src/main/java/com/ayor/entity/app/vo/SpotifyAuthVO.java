package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Spotify授权信息VO
 * 用于返回授权URL和绑定状态
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpotifyAuthVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 202L;

    /**
     * Spotify授权URL
     */
    private String authUrl;

    /**
     * 是否已绑定Spotify账号
     */
    private Boolean isConnected;

    /**
     * 绑定时间
     */
    private LocalDateTime connectedAt;
}
