package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 图片资源主表实体。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("image_asset")
public class ImageAsset {

    @TableId(type = IdType.AUTO)
    private Integer assetId;

    private Integer accountId;

    private String url;

    private String objectPath;

    private String originalExt;

    private String outputExt;

    private String mimeType;

    private Long fileSize;

    private Integer width;

    private Integer height;

    private String sha256;

    private String sourceType;

    private String assetType;

    private String visibility;

    private String status;

    private Integer favoriteCount;

    private Integer useCount;

    private Date createTime;

    private Date updateTime;
}
