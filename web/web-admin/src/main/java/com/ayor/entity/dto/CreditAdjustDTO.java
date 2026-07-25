package com.ayor.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Credit 调整参数")
public class CreditAdjustDTO {

    @NotNull(message = "账号ID不能为空")
    @Schema(description = "目标用户账号ID")
    private Integer accountId;

    @NotNull(message = "调整数量不能为空")
    @Schema(description = "调整数量：正数发放，负数扣减")
    private Long amount;

    @NotBlank(message = "备注不能为空")
    @Size(max = 255, message = "备注不能超过255个字符")
    @Schema(description = "调整备注")
    private String remark;
}
