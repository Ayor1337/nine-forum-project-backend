package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 107L;

    private Integer conversationId;

    private UserInfoVO userInfo;

    private Date updateTime;

    private Integer lastMessageId;

    private String lastMessageContent;

    private Date lastMessageTime;

    private Integer lastMessageSenderId;

    private Boolean pinned;

    private Boolean partnerOnline;

}
