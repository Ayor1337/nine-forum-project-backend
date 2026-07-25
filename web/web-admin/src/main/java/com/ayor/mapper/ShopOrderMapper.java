package com.ayor.mapper;

import com.ayor.entity.pojo.ShopOrder;
import com.ayor.entity.vo.ShopOrderVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ShopOrderMapper extends BaseMapper<ShopOrder> {

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM shop_order so
            LEFT JOIN account a ON so.account_id = a.account_id
            <where>
                <if test="accountId != null">
                    AND so.account_id = #{accountId}
                </if>
                <if test="username != null">
                    AND a.username = #{username}
                </if>
                <if test="itemId != null">
                    AND so.item_id = #{itemId}
                </if>
            </where>
            </script>
            """)
    Long countOrders(@Param("accountId") Integer accountId,
                     @Param("username") String username,
                     @Param("itemId") Integer itemId);

    @Select("""
            <script>
            SELECT so.order_id,
                   so.account_id,
                   a.username,
                   a.nickname,
                   so.item_id,
                   si.name AS item_name,
                   so.price,
                   so.quantity,
                   so.status,
                   so.create_time
            FROM shop_order so
            LEFT JOIN account a ON so.account_id = a.account_id
            LEFT JOIN shop_item si ON so.item_id = si.item_id
            <where>
                <if test="accountId != null">
                    AND so.account_id = #{accountId}
                </if>
                <if test="username != null">
                    AND a.username = #{username}
                </if>
                <if test="itemId != null">
                    AND so.item_id = #{itemId}
                </if>
            </where>
            ORDER BY so.create_time DESC, so.order_id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            </script>
            """)
    List<ShopOrderVO> selectOrders(@Param("offset") Integer offset,
                                   @Param("pageSize") Integer pageSize,
                                   @Param("accountId") Integer accountId,
                                   @Param("username") String username,
                                   @Param("itemId") Integer itemId);
}
