package com.ayor.mapper;

import com.ayor.entity.pojo.Decoration;
import com.ayor.entity.vo.DecorationVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DecorationMapper extends BaseMapper<Decoration> {

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM decoration
            <where>
                AND is_deleted = 0
                <if test="name != null">
                    AND name LIKE CONCAT('%', #{name}, '%')
                </if>
                <if test="type != null">
                    AND type = #{type}
                </if>
                <if test="status != null">
                    AND status = #{status}
                </if>
            </where>
            </script>
            """)
    Long countDecorations(@Param("name") String name,
                          @Param("type") String type,
                          @Param("status") Integer status);

    @Select("""
            <script>
            SELECT decoration_id,
                   decoration_key,
                   name,
                   description,
                   type,
                   status,
                   published_at,
                   create_time,
                   update_time
            FROM decoration
            <where>
                AND is_deleted = 0
                <if test="name != null">
                    AND name LIKE CONCAT('%', #{name}, '%')
                </if>
                <if test="type != null">
                    AND type = #{type}
                </if>
                <if test="status != null">
                    AND status = #{status}
                </if>
            </where>
            ORDER BY create_time DESC, decoration_id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            </script>
            """)
    List<DecorationVO> selectDecorations(@Param("offset") Integer offset,
                                         @Param("pageSize") Integer pageSize,
                                         @Param("name") String name,
                                         @Param("type") String type,
                                         @Param("status") Integer status);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM decoration
            WHERE decoration_key = #{decorationKey}
            <if test="excludeDecorationId != null">
                AND decoration_id != #{excludeDecorationId}
            </if>
            </script>
            """)
    Long countByDecorationKey(@Param("decorationKey") String decorationKey,
                              @Param("excludeDecorationId") Integer excludeDecorationId);
}
