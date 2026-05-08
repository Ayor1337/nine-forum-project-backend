package com.ayor.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 根据平台图片地址添加表情包的请求体。
 */
@Data
public class StickerByUrlDTO {

    @NotBlank(message = "图片地址不能为空")
    private String url;
}
