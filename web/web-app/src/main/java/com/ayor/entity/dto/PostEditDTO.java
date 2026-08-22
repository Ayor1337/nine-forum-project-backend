package com.ayor.entity.dto;

import com.ayor.entity.Base64Upload;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostEditDTO {

    @NotNull(message = "内容不能为空")
    private String content;

    private List<String> imageUrls;

    /**
     * 本次新增上传的 Base64 图片；已有图片通过 imageUrls 保留。
     */
    private List<Base64Upload> images;

    public PostEditDTO(String content) {
        this.content = content;
    }

}
