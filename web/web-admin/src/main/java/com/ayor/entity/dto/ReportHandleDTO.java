package com.ayor.entity.dto;

import com.ayor.type.ReportStatus;
import lombok.Data;

@Data
public class ReportHandleDTO {

    private ReportStatus status;

    private String handleNote;
}
