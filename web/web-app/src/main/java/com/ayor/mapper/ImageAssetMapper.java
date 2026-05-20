package com.ayor.mapper;

import com.ayor.entity.pojo.ImageAsset;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 图片资源持久层接口。
 */
public interface ImageAssetMapper extends BaseMapper<ImageAsset> {

    @Select("select * from image_asset where url = #{url} limit 1")
    ImageAsset findByUrl(String url);

    @Select("""
            select ia.* from image_asset ia
            inner join image_asset_favorite iaf on iaf.asset_id = ia.asset_id
            where iaf.account_id = #{accountId} and ia.status = 'ACTIVE' and ia.asset_type = 'STICKER'
            order by iaf.create_time desc
            limit #{limit} offset #{offset}
            """)
    List<ImageAsset> selectActiveStickers(Integer accountId, long limit, long offset);

    @Select("""
            select count(*) from image_asset ia
            inner join image_asset_favorite iaf on iaf.asset_id = ia.asset_id
            where iaf.account_id = #{accountId} and ia.status = 'ACTIVE' and ia.asset_type = 'STICKER'
            """)
    Long countActiveStickers(Integer accountId);

    @Update("""
            update image_asset
            set favorite_count = (
                select count(*) from image_asset_favorite where asset_id = #{assetId}
            ),
            update_time = now()
            where asset_id = #{assetId}
            """)
    void refreshAddedCount(Integer assetId);

    @Update("""
            update image_asset
            set use_count = (
                select count(*) from content_image_ref where asset_id = #{assetId}
            ),
            update_time = now()
            where asset_id = #{assetId}
            """)
    void refreshUseCount(Integer assetId);
}
