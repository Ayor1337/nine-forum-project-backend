package com.ayor.entity.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreadDTO {

    @NotNull(message = "标题不能为空")
    @Size(min = 1, max = 50, message = "标题长度必须在1-50之间")
    private String title;

    @NotNull(message = "内容不能为空")
    private String content;

    @NotNull(message = "主题不能为空")
    private Integer topicId;

}
