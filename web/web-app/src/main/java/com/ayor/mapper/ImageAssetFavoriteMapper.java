package com.ayor.mapper;

import com.ayor.entity.pojo.ImageAssetFavorite;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

/**
 * 用户表情包库关系持久层接口。
 */
public interface ImageAssetFavoriteMapper extends BaseMapper<ImageAssetFavorite> {

    @Select("select * from image_asset_favorite where account_id = #{accountId} and asset_id = #{assetId} limit 1")
    ImageAssetFavorite findMembership(Integer accountId, Integer assetId);

    @Delete("delete from image_asset_favorite where asset_id = #{assetId}")
    int deleteByAssetId(Integer assetId);
}
