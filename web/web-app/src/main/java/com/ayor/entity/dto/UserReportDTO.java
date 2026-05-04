package com.ayor.entity.dto;

import com.ayor.type.UserReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserReportDTO {

    @NotNull
    private UserReportType type;

    @NotBlank
    @Size(min = 10, max = 500)
    private String description;
}
