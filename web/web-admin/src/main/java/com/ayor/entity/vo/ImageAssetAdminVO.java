package com.ayor.entity.vo;

import lombok.Data;

import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 管理端图片资源视图对象。
 */
@Schema(description = "ImageAssetAdminVO 数据模型")
@Data
public class ImageAssetAdminVO {

    @Schema(description = "assetId")

    private Integer assetId;

    @Schema(description = "用户ID")

    private Integer accountId;

    @Schema(description = "资源地址")

    private String url;

    @Schema(description = "assetType")

    private String assetType;

    @Schema(description = "sourceType")

    private String sourceType;

    @Schema(description = "状态")

    private String status;

    @Schema(description = "fileSize")

    private Long fileSize;

    @Schema(description = "width")

    private Integer width;

    @Schema(description = "height")

    private Integer height;

    @Schema(description = "favoriteCount")

    private Integer favoriteCount;

    @Schema(description = "useCount")

    private Integer useCount;

    @Schema(description = "创建时间")

    private Date createTime;
}
