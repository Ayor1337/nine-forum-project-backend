package com.ayor.entity.app.vo;

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

}
