package com.ayor.entity.pojo;

import com.ayor.type.DmPermission;
import com.ayor.type.VisibilityScope;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户隐私设置实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_privacy_setting")
public class UserPrivacySetting {

    @TableId
    private Integer accountId;

    private VisibilityScope profileVisibility;

    private VisibilityScope likedThreadsVisibility;

    private VisibilityScope collectedThreadsVisibility;

    private VisibilityScope followListVisibility;

    private VisibilityScope followerListVisibility;

    private DmPermission dmPermission;

    private Date createTime;

    private Date updateTime;
}
