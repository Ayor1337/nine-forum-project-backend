package com.ayor.mapper;

import com.ayor.entity.pojo.ImageAssetFavorite;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;

/**
 * 管理端图片收藏关系持久层接口。
 */
public interface ImageAssetFavoriteMapper extends BaseMapper<ImageAssetFavorite> {

    @Delete("delete from image_asset_favorite where asset_id = #{assetId}")
    int deleteByAssetId(Integer assetId);
}
