package com.ayor.entity.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AccountDTO 数据模型")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTO {

    @NotNull
    @Schema(description = "用户ID")
    private Integer accountId;

    @Schema(description = "状态")

    private Integer status;

    @Schema(description = "角色ID")

    private Integer roleId;

    @Schema(description = "isDeleted")

    private Boolean isDeleted;
}
