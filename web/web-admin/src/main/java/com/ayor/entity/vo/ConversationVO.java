package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationVO {

    private Integer conversationId;

    private Integer alphaAccountId;

    private Integer betaAccountId;

    private Date createTime;

    private Date updateTime;

    private Boolean isDeleted;

    private Integer hidden;
}
