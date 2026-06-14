package com.ayor.entity.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "TrendsVO 数据模型")
@Data
public class TrendsVO {

    @Schema(description = "todayPosts")

    private Integer todayPosts;

    @Schema(description = "todayPostsRatio")

    private Float todayPostsRatio;

    @Schema(description = "todayPostsSummary")

    private String todayPostsSummary;

    @Schema(description = "todayReplies")

    private Integer todayReplies;

    @Schema(description = "todayRepliesRatio")

    private Float todayRepliesRatio;

    @Schema(description = "todayRepliesSummary")

    private String todayRepliesSummary;

    @Schema(description = "todayReports")

    private Integer todayReports;

    @Schema(description = "todayReportsRatio")

    private Float todayReportsRatio;

    @Schema(description = "todayReportsSummary")

    private String todayReportsSummary;
}
