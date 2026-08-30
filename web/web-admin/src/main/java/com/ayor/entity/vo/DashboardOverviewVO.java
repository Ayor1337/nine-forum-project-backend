package com.ayor.entity.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DashboardOverviewVO 数据模型")
@Data
public class DashboardOverviewVO {

    @Schema(description = "kpi")

    private KpiVO kpi;

    @Schema(description = "trends")

    private TrendsVO trends;

    @Schema(description = "health")

    private HealthVO health;
}
