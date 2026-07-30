package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAvatarVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 142L;

    private String avatarUrl;

    private String avatarFrameKey;

    private String avatarFrameName;

    /**
     * 已装备头像框的已发布配置（JSON 文本，未绑定装扮时为空，前端回退 avatarFrameKey 渲染）
     */
    private String avatarFrameConfig;

    private String badgeKey;

    private String badgeName;

    /**
     * 已装备徽章的已发布配置（JSON 文本，未绑定装扮时为空，前端回退 badgeKey 渲染）
     */
    private String badgeConfig;

}
