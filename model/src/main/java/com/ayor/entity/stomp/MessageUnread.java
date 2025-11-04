package com.ayor.entity.stomp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageUnread {

    private Long unread;

    public static MessageUnread emptyUnread() {
        return new MessageUnread(0L);
    }
}
