package com.ayor.entity.dto;

import com.ayor.type.ReportStatus;
import com.ayor.type.AccountAction;
import lombok.Data;

@Data
public class ReportHandleDTO {

    private ReportStatus status;

    private String handleNote;

    private AccountAction accountAction;
}
