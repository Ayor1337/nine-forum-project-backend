package com.ayor.entity.stomp;

import com.ayor.type.ReportStatus;
import com.ayor.type.ReportTargetType;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ReportStompMessage 数据模型")
@Data
@Builder
public class ReportStompMessage {

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

    @Schema(description = "reportedUsernameSnapshot")

    private String reportedUsernameSnapshot;

    @Schema(description = "targetSummarySnapshot")

    private String targetSummarySnapshot;

    @Schema(description = "状态")

    private ReportStatus status;

    @Schema(description = "创建时间")

    private Date createTime;
}
