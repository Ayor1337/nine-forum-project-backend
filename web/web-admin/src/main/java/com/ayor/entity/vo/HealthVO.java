package com.ayor.entity.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "HealthVO 数据模型")
@Data
public class HealthVO {

    @Schema(description = "systemStatus")

    private String systemStatus;

    @Schema(description = "systemStatusDetail")

    private String systemStatusDetail;

    @Schema(description = "avgReportResponseMinutes")

    private Integer avgReportResponseMinutes;

    @Schema(description = "operationAlertCount")

    private Integer operationAlertCount;

    @Schema(description = "operationAlertDetail")

    private String operationAlertDetail;
}
