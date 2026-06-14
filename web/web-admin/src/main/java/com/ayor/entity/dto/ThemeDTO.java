package com.ayor.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ThemeDTO 数据模型")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThemeDTO {

    @Schema(description = "主题ID")

    private Integer themeId;

    @Schema(description = "标题")

    private String title;

    @Schema(description = "isDeleted")

    private Boolean isDeleted;
}
