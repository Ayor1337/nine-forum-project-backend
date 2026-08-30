package com.ayor.entity.dto;

import com.ayor.entity.Base64Upload;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO {

    @NotNull(message = "内容不能为空")
    private String content;

    @NotNull(message = "未知的发送")
    private Integer threadId;

    @JsonProperty("reply_to")
    private Integer replyTo;

    private List<String> imageUrls;

    /**
     * 本次新增上传的 Base64 图片；已有图片通过 imageUrls 保留。
     */
    private List<@Valid Base64Upload> images;

}
