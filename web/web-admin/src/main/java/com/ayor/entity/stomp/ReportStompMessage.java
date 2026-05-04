package com.ayor.entity.stomp;

import com.ayor.type.ReportStatus;
import com.ayor.type.ReportTargetType;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ReportStompMessage {

    private Integer reportId;

    private Integer reporterAccountId;

    private Integer reportedAccountId;

    private ReportTargetType targetType;

    private Integer targetId;

    private String reportType;

    private String reportedUsernameSnapshot;

    private String targetSummarySnapshot;

    private ReportStatus status;

    private Date createTime;
}
