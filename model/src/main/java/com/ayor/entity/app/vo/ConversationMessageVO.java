package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationMessageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 106L;

    private Integer conversationMessageId;

    private String content;

    private Integer accountId;

    private String avatarUrl;

    private Date createTime;

    private Date updateTime;

    private Boolean isEdit;

}
