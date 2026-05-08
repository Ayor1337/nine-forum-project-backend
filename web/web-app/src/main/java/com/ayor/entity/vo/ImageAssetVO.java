package com.ayor.entity.vo;

import lombok.Data;

import java.util.Date;

/**
 * 用户侧图片资源视图对象。
 */
@Data
public class ImageAssetVO {

    private Integer assetId;

    private Integer accountId;

    private String url;

    private String assetType;

    private Long fileSize;

    private Integer width;

    private Integer height;

    private String outputExt;

    private String status;

    private Integer favoriteCount;

    private Integer useCount;

    private Boolean favorited;

    private Boolean available;

    private Date createTime;
}
