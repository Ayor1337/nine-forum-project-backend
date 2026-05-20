package com.ayor.entity.dto;

import com.ayor.type.ContentReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContentReportDTO {

    @NotNull
    private ContentReportType type;

    @NotBlank
    @Size(min = 10, max = 500)
    private String description;
}
