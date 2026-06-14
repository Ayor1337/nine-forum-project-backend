package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ThemeVO 数据模型")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThemeVO {

    @Schema(description = "主题ID")

    private Integer themeId;

    @Schema(description = "标题")

    private String title;

    @Schema(description = "isDeleted")

    private Boolean isDeleted;

}
