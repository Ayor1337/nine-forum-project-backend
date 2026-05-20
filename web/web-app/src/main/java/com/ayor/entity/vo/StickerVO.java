package com.ayor.entity.vo;

import lombok.Data;

import java.util.Date;

/**
 * 用户侧表情包视图对象。
 */
@Data
public class StickerVO {

    private Integer assetId;

    private Integer accountId;

    private String url;

    private String assetType;

    private Long fileSize;

    private Integer width;

    private Integer height;

    private String outputExt;

    private String status;

    private Integer addedCount;

    private Integer useCount;

    private Boolean added;

    private Boolean available;

    private Date createTime;
}
