package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("db_conversation_message")
public class ConversationMessage {

    @TableId(type = IdType.AUTO)
    private Integer conversationMessageId;

    private Integer conversationId;

    private String content;

    private Integer accountId;

    private Date createTime;

    private Date updateTime;

    private Boolean isDeleted;

    private Boolean isEdit;

}
