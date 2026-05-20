package com.ayor.entity.vo;

import com.ayor.type.DmPermission;
import com.ayor.type.VisibilityScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * 用户隐私设置视图对象。
 */
public class UserPrivacySettingVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 118L;

    private Integer accountId;

    private VisibilityScope profileVisibility;

    private VisibilityScope likedThreadsVisibility;

    private VisibilityScope collectedThreadsVisibility;

    private VisibilityScope followListVisibility;

    private VisibilityScope followerListVisibility;

    private VisibilityScope birthdayVisibility;

    private DmPermission dmPermission;
}
