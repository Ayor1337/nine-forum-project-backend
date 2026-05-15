package com.ayor.mapper;

import com.ayor.entity.pojo.Conversation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

public interface ConversationMapper extends BaseMapper<Conversation> {

    @Select("select exists(select * from conversation where conversation_id = #{conversationId})")
    boolean existsConversationById(Integer conversationId);

    @Select("""
            select exists(
                select *
                from conversation
                where is_deleted = 0
                  and (
                    (alpha_account_id = #{accountId} and beta_account_id = #{targetAccountId})
                    or
                    (alpha_account_id = #{targetAccountId} and beta_account_id = #{accountId})
                  )
            )
            """)
    boolean existsConversationByUsers(Integer accountId, Integer targetAccountId);

    Integer getChatPartnerId(Integer accountId, Integer conversationId);

}
