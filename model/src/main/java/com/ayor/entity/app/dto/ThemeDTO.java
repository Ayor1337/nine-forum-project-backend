package com.ayor.entity.app.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThemeDTO {

    @Size(min = 1, max = 10, message = "标题长度必须在1-10之间")
    private String title;
}
