package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "PermissionOperationLogVO 数据模型")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionOperationLogVO {

    @Schema(description = "logId")

    private Long logId;

    @Schema(description = "userId")

    private Integer userId;

    @Schema(description = "用户名")

    private String username;

    @Schema(description = "action")

    private String action;

    @Schema(description = "targetType")

    private String targetType;

    @Schema(description = "targetId")

    private Long targetId;

    @Schema(description = "method")

    private String method;

    private Map<String, Object> params;

    @Schema(description = "durationMs")

    private Long durationMs;

    @Schema(description = "创建时间")

    private Date createTime;
}
