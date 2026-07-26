package com.ayor.mapper;

import com.ayor.entity.pojo.UserItem;
import com.ayor.entity.vo.UserItemVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface UserItemMapper extends BaseMapper<UserItem> {

    @Select("""
            SELECT COUNT(*)
            FROM user_item
            WHERE account_id = #{accountId}
            """)
    Long countByAccountId(@Param("accountId") Integer accountId);

    @Select("""
            SELECT ui.user_item_id,
                   ui.item_id,
                   si.name,
                   si.item_key,
                   si.item_type,
                   ui.quantity,
                   ui.is_equipped,
                   ui.acquire_time
            FROM user_item ui
            LEFT JOIN shop_item si ON ui.item_id = si.item_id
            WHERE ui.account_id = #{accountId}
            ORDER BY ui.acquire_time DESC, ui.user_item_id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            """)
    List<UserItemVO> selectByAccountId(@Param("accountId") Integer accountId,
                                       @Param("offset") Integer offset,
                                       @Param("pageSize") Integer pageSize);

    @Select("""
            SELECT ui.user_item_id,
                   ui.item_id,
                   si.name,
                   si.item_key,
                   si.item_type,
                   ui.quantity,
                   ui.is_equipped,
                   ui.acquire_time
            FROM user_item ui
            LEFT JOIN shop_item si ON ui.item_id = si.item_id
            WHERE ui.account_id = #{accountId} AND ui.is_equipped = 1
            ORDER BY ui.acquire_time DESC, ui.user_item_id DESC
            """)
    List<UserItemVO> selectEquippedByAccountId(@Param("accountId") Integer accountId);

    @Select("""
            SELECT si.name,
                   si.item_key
            FROM user_item ui
            JOIN shop_item si ON ui.item_id = si.item_id
            WHERE ui.account_id = #{accountId}
              AND ui.is_equipped = 1
              AND si.item_type = 'avatar_frame'
            LIMIT 1
            """)
    UserItemVO selectEquippedAvatarFrame(@Param("accountId") Integer accountId);

    @Select("""
            SELECT si.name,
                   si.item_key
            FROM user_item ui
            JOIN shop_item si ON ui.item_id = si.item_id
            WHERE ui.account_id = #{accountId}
              AND ui.is_equipped = 1
              AND si.item_type = 'badge'
            LIMIT 1
            """)
    UserItemVO selectEquippedBadge(@Param("accountId") Integer accountId);

    @Update("""
            UPDATE user_item ui
            JOIN shop_item si ON ui.item_id = si.item_id
            SET ui.is_equipped = 0
            WHERE ui.account_id = #{accountId}
              AND ui.is_equipped = 1
              AND si.item_type = #{itemType}
            """)
    int unequipByType(@Param("accountId") Integer accountId, @Param("itemType") String itemType);
}
