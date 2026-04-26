package com.ayor.entity.message;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserSystemMessage<T> extends BroadcastMessage<T> {

    private Integer sendTo;

    private String title;

    public UserSystemMessage(T message, String title, Integer sendTo) {
        super(message);
        this.title = title;
        this.sendTo = sendTo;
    }

}
