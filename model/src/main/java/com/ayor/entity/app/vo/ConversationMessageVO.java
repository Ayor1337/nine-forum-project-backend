package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationMessageVO {

    private Integer conversationMessageId;

    private String content;

    private Integer accountId;

    private String avatarUrl;

    private Date createTime;

    private Date updateTime;

    private Boolean isEdit;

}
