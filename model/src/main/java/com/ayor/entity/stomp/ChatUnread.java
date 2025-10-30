package com.ayor.entity.stomp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatUnread {

    private Integer conversationId;

    private Integer fromUserId;

    private Long unread;

    public static ChatUnread emptyUnread(Integer conversationId, Integer fromUserId) {
        return new ChatUnread(conversationId, fromUserId, 0L);
    }

}
