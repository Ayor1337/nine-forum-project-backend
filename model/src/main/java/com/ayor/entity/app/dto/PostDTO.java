package com.ayor.entity.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO {

    @NotNull(message = "内容不能为空")
    private String content;

    @NotNull(message = "未知的发送")
    private Integer threadId;

}
