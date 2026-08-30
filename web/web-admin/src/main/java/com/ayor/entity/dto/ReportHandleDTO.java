package com.ayor.entity.dto;

import com.ayor.type.ReportStatus;
import com.ayor.type.AccountAction;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ReportHandleDTO 数据模型")
@Data
public class ReportHandleDTO {

    @Schema(description = "状态")

    private ReportStatus status;

    @Schema(description = "handleNote")

    private String handleNote;

    @Schema(description = "accountAction")

    private AccountAction accountAction;
}
