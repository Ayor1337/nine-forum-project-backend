package com.ayor.entity.vo;

import lombok.Data;

import java.util.Date;

/**
 * 管理端图片资源视图对象。
 */
@Data
public class ImageAssetAdminVO {

    private Integer assetId;

    private Integer accountId;

    private String url;

    private String assetType;

    private String sourceType;

    private String status;

    private Long fileSize;

    private Integer width;

    private Integer height;

    private Integer favoriteCount;

    private Integer useCount;

    private Date createTime;
}
