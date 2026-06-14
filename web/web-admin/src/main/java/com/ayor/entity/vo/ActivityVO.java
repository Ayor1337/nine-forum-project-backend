package com.ayor.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ActivityVO 数据模型")
@Data
public class ActivityVO {

    @Schema(description = "主键ID")

    private Long id;

    @Schema(description = "createdAt")

    private LocalDateTime createdAt;

    @Schema(description = "userId")

    private Long userId;

    @Schema(description = "用户名")

    private String username;

    @Schema(description = "action")

    private String action;

    @Schema(description = "target")

    private String target;

    @Schema(description = "targetId")

    private Long targetId;

    @Schema(description = "类型")

    private String type;
}
