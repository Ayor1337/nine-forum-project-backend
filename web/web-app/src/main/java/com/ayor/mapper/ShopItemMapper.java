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
            SELECT item_id,
                   name,
                   description,
                   item_type,
                   price,
                   stock,
                   purchase_limit,
                   status
            FROM shop_item
            WHERE status = 1 AND is_deleted = 0
            ORDER BY create_time DESC, item_id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            """)
    List<ShopItemVO> selectOnSaleItems(@Param("offset") Integer offset,
                                       @Param("pageSize") Integer pageSize);

    @Update("""
            UPDATE shop_item
            SET stock = stock - 1, update_time = NOW()
            WHERE item_id = #{itemId}
              AND status = 1
              AND is_deleted = 0
              AND (stock = -1 OR stock >= 1)
            """)
    int decreaseStock(@Param("itemId") Integer itemId);
}
