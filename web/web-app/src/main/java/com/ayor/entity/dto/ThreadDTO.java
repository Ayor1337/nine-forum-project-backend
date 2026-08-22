package com.ayor.entity.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    /**
     * 标签 ID，可选；不传或为 null 表示不设置标签（编辑时表示清除标签）
     */
    private Integer tagId;

    private List<String> imageUrls;

}
