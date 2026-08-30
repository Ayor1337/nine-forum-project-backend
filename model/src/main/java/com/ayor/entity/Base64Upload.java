package com.ayor.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Base64 文件上传对象")
public class Base64Upload {
    @NotBlank(message = "图片内容不能为空")
    @Size(max = ImageUploadLimits.MAX_BASE64_TEXT_CHARS, message = "图片体积过大")
    @Schema(description = "Base64 图片内容，可包含 data URL 前缀", maxLength = ImageUploadLimits.MAX_BASE64_TEXT_CHARS)
    private String base64;

    @NotBlank(message = "文件名不能为空")
    @Size(max = ImageUploadLimits.MAX_FILE_NAME_CHARS, message = "文件名过长")
    @Schema(description = "含扩展名的文件名", maxLength = ImageUploadLimits.MAX_FILE_NAME_CHARS)
    private String fileName;
}
