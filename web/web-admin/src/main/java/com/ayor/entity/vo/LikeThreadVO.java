package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "LikeThreadVO 数据模型")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikeThreadVO {

    @Schema(description = "likeId")

    private Integer likeId;

    @Schema(description = "用户ID")

    private Integer accountId;

    @Schema(description = "帖子ID")

    private Integer threadId;
}
