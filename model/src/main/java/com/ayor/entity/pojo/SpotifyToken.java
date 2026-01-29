package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Spotify OAuth Token实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("spotify_token")
public class SpotifyToken {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 关联的用户ID
     */
    private Integer accountId;

    /**
     * Spotify访问令牌
     */
    private String accessToken;

    /**
     * Spotify刷新令牌
     */
    private String refreshToken;

    /**
     * 令牌类型 (通常为Bearer)
     */
    private String tokenType;

    /**
     * 授权范围
     */
    private String scope;

    /**
     * Token过期时间(秒)
     */
    private Integer expiresIn;

    /**
     * Token过期时间点
     */
    private LocalDateTime expiresAt;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 软删除标志
     */
    private Boolean isDeleted;

    /**
     * 判断token是否已过期
     * 提前5分钟判定为过期,预留刷新时间
     *
     * @return true表示已过期或即将过期,需要刷新
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return true;
        }
        // 提前5分钟判定为过期
        Instant expiryInstant = expiresAt.atZone(ZoneId.systemDefault()).toInstant().minusSeconds(300);
        return new Date().after(Date.from(expiryInstant));
    }
}
