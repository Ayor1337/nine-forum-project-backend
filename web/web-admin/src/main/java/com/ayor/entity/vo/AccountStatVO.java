package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AccountStatVO 数据模型")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountStatVO {

    @Schema(description = "userStatId")

    private Integer userStatId;

    @Schema(description = "threadCount")

    private Integer threadCount;

    @Schema(description = "postCount")

    private Integer postCount;

    @Schema(description = "replyCount")

    private Integer replyCount;

    @Schema(description = "likedCount")

    private Integer likedCount;

    @Schema(description = "collectedCount")

    private Integer collectedCount;

    @Schema(description = "followingCount")

    private Integer followingCount;

    @Schema(description = "followerCount")

    private Integer followerCount;

    @Schema(description = "用户ID")

    private Integer accountId;
}
