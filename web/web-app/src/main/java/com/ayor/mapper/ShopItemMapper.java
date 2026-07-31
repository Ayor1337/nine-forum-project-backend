package com.ayor.mapper;

import com.ayor.entity.pojo.ShopItem;
import com.ayor.entity.vo.ShopItemVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ShopItemMapper extends BaseMapper<ShopItem> {

    @Select("""
            SELECT COUNT(*)
            FROM shop_item
            WHERE status = 1 AND is_deleted = 0
            """)
    Long countOnSaleItems();

    @Select("""
            SELECT si.item_id,
                   si.name,
                   si.item_key,
                   si.description,
                   si.item_type,
                   si.decoration_id,
                   d.published_config AS decoration_config,
                   si.price,
                   si.stock,
                   si.purchase_limit,
                   si.status
            FROM shop_item si
            LEFT JOIN decoration d ON si.decoration_id = d.decoration_id
            WHERE si.status = 1 AND si.is_deleted = 0
            ORDER BY si.create_time DESC, si.item_id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            """)
    List<ShopItemVO> selectOnSaleItems(@Param("offset") Integer offset,
                                       @Param("pageSize") Integer pageSize);

    @Update("""
            UPDATE shop_item
            SET stock = IF(stock = -1, -1, stock - 1), update_time = NOW()
            WHERE item_id = #{itemId}
              AND status = 1
              AND is_deleted = 0
              AND (stock = -1 OR stock >= 1)
            """)
    int decreaseStock(@Param("itemId") Integer itemId);
}
