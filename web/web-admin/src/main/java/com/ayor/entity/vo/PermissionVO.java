package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "PermissionVO 数据模型")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionVO {

    @Schema(description = "权限ID")

    private Integer permissionId;

    @Schema(description = "角色ID")

    private Integer roleId;

    @Schema(description = "权限标识")

    private String permission;
}
