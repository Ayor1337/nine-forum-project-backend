package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationMessageVO {

    private Integer conversationMessageId;

    private Integer conversationId;

    private String content;

    private Integer accountId;

    private Date createTime;

    private Date updateTime;

    private Boolean isDeleted;

    private Boolean isEdit;
}
