package com.ayor.mapper;

import com.ayor.entity.pojo.CreditTransaction;
import com.ayor.entity.vo.CreditTransactionVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CreditTransactionMapper extends BaseMapper<CreditTransaction> {

    @Select("""
            SELECT COUNT(*)
            FROM credit_transaction t
            WHERE t.account_id = #{accountId}
            """)
    Long countTransactionsByAccountId(@Param("accountId") Integer accountId);

    @Select("""
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
            WHERE t.account_id = #{accountId}
            ORDER BY t.create_time DESC, t.transaction_id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            """)
    List<CreditTransactionVO> selectTransactionsByAccountId(@Param("accountId") Integer accountId,
                                                            @Param("offset") Integer offset,
                                                            @Param("pageSize") Integer pageSize);
}
