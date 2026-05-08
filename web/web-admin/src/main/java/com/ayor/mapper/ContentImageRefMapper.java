package com.ayor.mapper;

import com.ayor.entity.pojo.ContentImageRef;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;

/**
 * 管理端内容图片引用关系持久层接口。
 */
public interface ContentImageRefMapper extends BaseMapper<ContentImageRef> {

    @Delete("delete from content_image_ref where asset_id = #{assetId}")
    int deleteByAssetId(Integer assetId);
}
