package com.ayor.mapper;

import com.ayor.entity.pojo.ShopItem;
import com.ayor.entity.vo.ShopItemVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ShopItemMapper extends BaseMapper<ShopItem> {

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM shop_item
            <where>
                AND is_deleted = 0
                <if test="name != null">
                    AND name LIKE CONCAT('%', #{name}, '%')
                </if>
                <if test="itemType != null">
                    AND item_type = #{itemType}
                </if>
                <if test="status != null">
                    AND status = #{status}
                </if>
            </where>
            </script>
            """)
    Long countItems(@Param("name") String name,
                    @Param("itemType") String itemType,
                    @Param("status") Integer status);

    @Select("""
            <script>
            SELECT item_id,
                   name,
                   item_key,
                   description,
                   item_type,
                   decoration_id,
                   price,
                   stock,
                   purchase_limit,
                   status
            FROM shop_item
            <where>
                AND is_deleted = 0
                <if test="name != null">
                    AND name LIKE CONCAT('%', #{name}, '%')
                </if>
                <if test="itemType != null">
                    AND item_type = #{itemType}
                </if>
                <if test="status != null">
                    AND status = #{status}
                </if>
            </where>
            ORDER BY create_time DESC, item_id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            </script>
            """)
    List<ShopItemVO> selectItems(@Param("offset") Integer offset,
                                 @Param("pageSize") Integer pageSize,
                                 @Param("name") String name,
                                 @Param("itemType") String itemType,
                                 @Param("status") Integer status);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM shop_item
            WHERE item_key = #{itemKey}
            <if test="excludeItemId != null">
                AND item_id != #{excludeItemId}
            </if>
            </script>
            """)
    Long countByItemKey(@Param("itemKey") String itemKey,
                        @Param("excludeItemId") Integer excludeItemId);
}
