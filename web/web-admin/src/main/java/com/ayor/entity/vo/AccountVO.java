package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AccountVO 数据模型")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountVO {

    @Schema(description = "用户ID")

    private Integer accountId;

    @Schema(description = "用户名")

    private String username;

    @Schema(description = "昵称")

    private String nickname;

    @Schema(description = "bio")

    private String bio;

    @Schema(description = "avatarUrl")

    private String avatarUrl;

    @Schema(description = "bannerUrl")

    private String bannerUrl;

    @Schema(description = "状态")

    private Integer status;

    @Schema(description = "isDeleted")

    private Boolean isDeleted;
}
