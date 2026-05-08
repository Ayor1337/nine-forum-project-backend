package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 图片资源收藏关系实体。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("image_asset_favorite")
public class ImageAssetFavorite {

    @TableId(type = IdType.AUTO)
    private Integer favoriteId;

    private Integer accountId;

    private Integer assetId;

    private Date createTime;
}
