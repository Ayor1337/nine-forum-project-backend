package com.ayor.entity.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "KpiVO 数据模型")
@Data
public class KpiVO {

    @Schema(description = "totalUsers")

    private Integer totalUsers;

    @Schema(description = "totalUsersDeltaPercent")

    private Float totalUsersDeltaPercent;

    @Schema(description = "todayActive")

    private Integer todayActive;

    @Schema(description = "todayActivePeakHour")

    private Integer todayActivePeakHour;

    @Schema(description = "totalPosts")

    private Integer totalPosts;

    @Schema(description = "todayNewPosts")

    private Integer todayNewPosts;

    @Schema(description = "pendingReports")

    private Integer pendingReports;

    @Schema(description = "highPriorityReports")

    private Integer highPriorityReports;
}
