package com.ayor.entity.app.dto;

import com.ayor.entity.Base64Upload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicDTO {

    private Integer topicId;

    @NotNull(message = "标题不能为空")
    @Size(min = 1, max = 10, message = "标题长度必须在1-10之间")
    private String title;

    @NotNull(message = "封面不能为空")
    private Base64Upload cover;

    @Size(max = 20, message = "描述长度不能超过20")
    private String description;

    @NotNull(message = "未知主题")
    private Integer themeId;
}
