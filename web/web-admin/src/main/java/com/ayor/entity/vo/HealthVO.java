package com.ayor.entity.vo;

import lombok.Data;

@Data
public class HealthVO {

    private String systemStatus;

    private String systemStatusDetail;

    private Integer avgReportResponseMinutes;

    private Integer operationAlertCount;

    private String operationAlertDetail;
}
