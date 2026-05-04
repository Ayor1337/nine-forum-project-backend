package com.ayor.entity.message;

import com.ayor.type.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCreatedMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer reporterAccountId;

    private Integer reportedAccountId;

    private ReportTargetType targetType;

    private Integer targetId;

    private String reportType;

    private String description;

    private String reportedUsernameSnapshot;

    private String targetSummarySnapshot;
}
