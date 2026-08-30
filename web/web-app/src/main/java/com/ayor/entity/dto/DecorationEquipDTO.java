package com.ayor.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "装饰装备状态参数")
public class DecorationEquipDTO {

    @NotNull(message = "装备状态不能为空")
    @Schema(description = "true=装备，false=卸下")
    private Boolean equipped;
}
