package com.ayor.mapper;

import com.ayor.entity.pojo.CreditTransaction;
import com.ayor.entity.vo.CreditTransactionVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CreditTransactionMapper extends BaseMapper<CreditTransaction> {

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM credit_transaction t
            LEFT JOIN account a ON t.account_id = a.account_id
            <where>
                <if test="accountId != null">
                    AND t.account_id = #{accountId}
                </if>
                <if test="username != null">
                    AND a.username = #{username}
                </if>
                <if test="changeType != null">
                    AND t.change_type = #{changeType}
                </if>
            </where>
            </script>
            """)
    Long countTransactions(@Param("accountId") Integer accountId,
                           @Param("username") String username,
                           @Param("changeType") String changeType);

    @Select("""
            <script>
            SELECT t.transaction_id,
                   t.account_id,
                   a.username,
                   a.nickname,
                   t.delta,
                   t.balance_after,
                   t.change_type,
                   t.remark,
                   t.operator_id,
                   o.nickname AS operator_nickname,
                   t.create_time
            FROM credit_transaction t
            LEFT JOIN account a ON t.account_id = a.account_id
            LEFT JOIN account o ON t.operator_id = o.account_id
            <where>
                <if test="accountId != null">
                    AND t.account_id = #{accountId}
                </if>
                <if test="username != null">
                    AND a.username = #{username}
                </if>
                <if test="changeType != null">
                    AND t.change_type = #{changeType}
                </if>
            </where>
            <choose>
                <when test="sortAsc">
                    ORDER BY t.create_time ASC, t.transaction_id ASC
                </when>
                <otherwise>
                    ORDER BY t.create_time DESC, t.transaction_id DESC
                </otherwise>
            </choose>
            LIMIT #{pageSize} OFFSET #{offset}
            </script>
            """)
    List<CreditTransactionVO> selectTransactions(@Param("offset") Integer offset,
                                                 @Param("pageSize") Integer pageSize,
                                                 @Param("accountId") Integer accountId,
                                                 @Param("username") String username,
                                                 @Param("changeType") String changeType,
                                                 @Param("sortAsc") boolean sortAsc);
}
