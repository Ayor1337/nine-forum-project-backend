package com.ayor.entity.dto;

import com.ayor.type.FeedbackStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "意见反馈处理参数")
public class FeedbackHandleDTO {

    @NotNull(message = "处理状态不能为空")
    @Schema(description = "目标处理状态")
    private FeedbackStatus status;

    @Size(max = 500, message = "处理备注不能超过500个字符")
    @Schema(description = "处理备注")
    private String handleNote;
}
