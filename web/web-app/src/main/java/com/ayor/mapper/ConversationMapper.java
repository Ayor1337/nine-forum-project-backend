package com.ayor.mapper;

import com.ayor.entity.pojo.Conversation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

public interface ConversationMapper extends BaseMapper<Conversation> {

    @Select("select exists(select * from conversation where conversation_id = #{conversationId})")
    boolean existsConversationById(Integer conversationId);

    Integer getChatPartnerId(Integer accountId, Integer conversationId);

}
