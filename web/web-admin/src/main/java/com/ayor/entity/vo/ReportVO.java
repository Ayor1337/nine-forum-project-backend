package com.ayor.entity.vo;

import com.ayor.type.ReportStatus;
import com.ayor.type.ReportTargetType;
import lombok.Data;

import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ReportVO 数据模型")
@Data
public class ReportVO {

    @Schema(description = "举报ID")

    private Integer reportId;

    @Schema(description = "reporterAccountId")

    private Integer reporterAccountId;

    @Schema(description = "reportedAccountId")

    private Integer reportedAccountId;

    @Schema(description = "targetType")

    private ReportTargetType targetType;

    @Schema(description = "targetId")

    private Integer targetId;

    @Schema(description = "reportType")

    private String reportType;

    @Schema(description = "描述")

    private String description;

    @Schema(description = "状态")

    private ReportStatus status;

    @Schema(description = "handlerAccountId")

    private Integer handlerAccountId;

    @Schema(description = "handleNote")

    private String handleNote;

    @Schema(description = "handledAt")

    private Date handledAt;

    @Schema(description = "reportedUsernameSnapshot")

    private String reportedUsernameSnapshot;

    @Schema(description = "targetSummarySnapshot")

    private String targetSummarySnapshot;

    @Schema(description = "创建时间")

    private Date createTime;

    @Schema(description = "更新时间")

    private Date updateTime;
}
