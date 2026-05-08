package com.ayor.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 按图片 URL 收藏表情包的请求体。
 */
@Data
public class ImageAssetFavoriteByUrlDTO {

    @NotBlank(message = "图片地址不能为空")
    private String url;
}
