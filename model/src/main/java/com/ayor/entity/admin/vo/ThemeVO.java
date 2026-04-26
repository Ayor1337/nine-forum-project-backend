package com.ayor.entity.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThemeVO {

    private Integer themeId;

    private String title;

    private Boolean isDeleted;

}
