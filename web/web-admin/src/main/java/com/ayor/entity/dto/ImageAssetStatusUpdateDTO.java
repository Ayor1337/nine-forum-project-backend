package com.ayor.entity.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 管理端图片资源状态变更请求体。
 */
@Schema(description = "ImageAssetStatusUpdateDTO 数据模型")
@Data
public class ImageAssetStatusUpdateDTO {

    @Schema(description = "状态")

    private String status;
}
