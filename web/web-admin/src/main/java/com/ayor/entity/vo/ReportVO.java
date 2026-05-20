package com.ayor.entity.vo;

import com.ayor.type.ReportStatus;
import com.ayor.type.ReportTargetType;
import lombok.Data;

import java.util.Date;

@Data
public class ReportVO {

    private Integer reportId;

    private Integer reporterAccountId;

    private Integer reportedAccountId;

    private ReportTargetType targetType;

    private Integer targetId;

    private String reportType;

    private String description;

    private ReportStatus status;

    private Integer handlerAccountId;

    private String handleNote;

    private Date handledAt;

    private String reportedUsernameSnapshot;

    private String targetSummarySnapshot;

    private Date createTime;

    private Date updateTime;
}
