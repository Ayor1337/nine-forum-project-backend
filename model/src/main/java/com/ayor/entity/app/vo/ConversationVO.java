package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationVO {

    private Integer conversationId;

    private UserInfoVO userInfo;

    private Date updateTime;

}
