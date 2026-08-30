package com.ayor.entity.stomp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationTypingMessage {

    private Integer conversationId;

    private Integer fromUserId;

    private Boolean typing;

    private Date time;
}
