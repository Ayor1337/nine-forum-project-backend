package com.ayor.entity.dto;

import com.ayor.type.FeedbackType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "意见反馈提交参数")
public class FeedbackCreateDTO {

    @NotNull(message = "反馈类型不能为空")
    @Schema(description = "反馈类型")
    private FeedbackType type;

    @NotBlank(message = "反馈内容不能为空")
    @Size(min = 10, max = 1000, message = "反馈内容长度必须在10到1000个字符之间")
    @Schema(description = "反馈内容")
    private String content;
}
