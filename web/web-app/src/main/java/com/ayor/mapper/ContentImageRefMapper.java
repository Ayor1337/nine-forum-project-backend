package com.ayor.mapper;

import com.ayor.entity.pojo.ContentImageRef;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 内容图片引用关系持久层接口。
 */
public interface ContentImageRefMapper extends BaseMapper<ContentImageRef> {

    @Select("select asset_id from content_image_ref where content_type = #{contentType} and content_id = #{contentId}")
    List<Integer> selectAssetIdsByContent(String contentType, Integer contentId);

    @Delete("delete from content_image_ref where content_type = #{contentType} and content_id = #{contentId}")
    int deleteByContent(String contentType, Integer contentId);

    @Delete("delete from content_image_ref where asset_id = #{assetId}")
    int deleteByAssetId(Integer assetId);

    @Insert("""
            insert ignore into content_image_ref(asset_id, content_type, content_id, create_time)
            values(#{assetId}, #{contentType}, #{contentId}, #{createTime})
            """)
    int insertIgnore(ContentImageRef ref);
}
