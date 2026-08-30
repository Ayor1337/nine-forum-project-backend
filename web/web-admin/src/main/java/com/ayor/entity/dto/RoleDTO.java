package com.ayor.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "RoleDTO 数据模型")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDTO {

    @Schema(description = "角色ID")

    private Integer roleId;

    @Schema(description = "角色名称")

    private String roleName;

    @Schema(description = "roleNick")

    private String roleNick;

    @Schema(description = "priority")

    private Integer priority;

    @Schema(description = "话题ID")

    private Integer topicId;
}
