package com.ayor.entity.message;

import com.ayor.type.UserViolationType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@ToString
@Schema(description = "用户违规消息")
public class UserViolationMessage<T> extends BroadcastMessage<T>{

    private Integer sendTo;

    private String title;

    private UserViolationType type;

    public UserViolationMessage(T message, String title, Integer sendTo, UserViolationType type) {
        super(message);
        this.title = title;
        this.sendTo = sendTo;
        this.type = type;
    }


}
