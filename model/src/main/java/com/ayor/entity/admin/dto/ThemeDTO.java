package com.ayor.entity.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThemeDTO {

    private Integer themeId;

    private String title;

    private Boolean isDeleted;
}
