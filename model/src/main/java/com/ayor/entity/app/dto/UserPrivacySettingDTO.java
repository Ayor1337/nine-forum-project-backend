package com.ayor.entity.app.dto;

import com.ayor.type.DmPermission;
import com.ayor.type.VisibilityScope;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * 用户隐私设置请求体。
 */
public class UserPrivacySettingDTO {

    @NotNull
    private VisibilityScope profileVisibility;

    @NotNull
    private VisibilityScope likedThreadsVisibility;

    @NotNull
    private VisibilityScope collectedThreadsVisibility;

    @NotNull
    private VisibilityScope followListVisibility;

    @NotNull
    private VisibilityScope followerListVisibility;

    @NotNull
    private DmPermission dmPermission;
}
