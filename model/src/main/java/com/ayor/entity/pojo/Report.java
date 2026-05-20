package com.ayor.entity.pojo;

import com.ayor.type.ReportStatus;
import com.ayor.type.ReportTargetType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("report")
public class Report {

    @TableId(type = IdType.AUTO)
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
