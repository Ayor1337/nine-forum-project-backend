package com.ayor.mapper;

import com.ayor.entity.pojo.Conversation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

public interface ConversationMapper extends BaseMapper<Conversation> {

    @Select("""
            select *
            from conversation
            where (alpha_account_id = #{accountId} and beta_account_id = #{targetAccountId})
               or (alpha_account_id = #{targetAccountId} and beta_account_id = #{accountId})
            limit 1
            """)
    Conversation selectConversationByUsers(Integer accountId, Integer targetAccountId);

}
