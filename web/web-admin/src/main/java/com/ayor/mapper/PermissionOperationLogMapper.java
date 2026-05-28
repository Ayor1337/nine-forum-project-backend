package com.ayor.mapper;

import com.ayor.entity.pojo.PermissionOperationLog;
import com.ayor.entity.vo.PermissionOperationLogVO;
import com.ayor.typehandler.OperationLogParamsTypeHandler;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PermissionOperationLogMapper extends BaseMapper<PermissionOperationLog> {

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM permission_operation_log l
            LEFT JOIN account a ON l.user_id = a.account_id
            <where>
                <if test="action != null">
                    AND l.action = #{action}
                </if>
                <if test="username != null">
                    AND a.username = #{username}
                </if>
                <if test="targetType != null">
                    AND l.target_type = #{targetType}
                </if>
                <if test="targetId != null">
                    AND l.target_id = #{targetId}
                </if>
            </where>
            </script>
            """)
    Long countPermissionOperationLogs(@Param("action") String action,
                                      @Param("username") String username,
                                      @Param("targetType") String targetType,
                                      @Param("targetId") Long targetId);

    @Select("""
            <script>
            SELECT l.log_id,
                   l.user_id AS userId,
                   a.username,
                   l.action,
                   l.target_type,
                   l.target_id,
                   l.method,
                   l.params,
                   l.duration_ms,
                   l.create_time
            FROM permission_operation_log l
            LEFT JOIN account a ON l.user_id = a.account_id
            <where>
                <if test="action != null">
                    AND l.action = #{action}
                </if>
                <if test="username != null">
                    AND a.username = #{username}
                </if>
                <if test="targetType != null">
                    AND l.target_type = #{targetType}
                </if>
                <if test="targetId != null">
                    AND l.target_id = #{targetId}
                </if>
            </where>
            <choose>
                <when test="sortAsc">
                    ORDER BY l.create_time ASC, l.log_id ASC
                </when>
                <otherwise>
                    ORDER BY l.create_time DESC, l.log_id DESC
                </otherwise>
            </choose>
            LIMIT #{pageSize} OFFSET #{offset}
            </script>
            """)
    @Results({
            @Result(column = "params", property = "params", typeHandler = OperationLogParamsTypeHandler.class)
    })
    List<PermissionOperationLogVO> selectPermissionOperationLogs(@Param("offset") Integer offset,
                                                                @Param("pageSize") Integer pageSize,
                                                                @Param("action") String action,
                                                                @Param("username") String username,
                                                                @Param("targetType") String targetType,
                                                                @Param("targetId") Long targetId,
                                                                @Param("sortAsc") boolean sortAsc);

    @Select("""
            SELECT DISTINCT a.username
            FROM permission_operation_log l
            LEFT JOIN account a ON l.user_id = a.account_id
            WHERE a.username IS NOT NULL
            ORDER BY a.username ASC
            """)
    List<String> selectUsernameOptions();
}
